package org.matsim.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class IngestDataFromLocalDisk {

    private static final String WGS_84 = "EPSG:4326";

    private final GeomesaFileSystemStore store;
    private final SetInformation setInfo = new SetInformation();
    private final String networkFile;
    private final String eventsFile;
    private final String sourceCRS;

    public IngestDataFromLocalDisk(GeomesaFileSystemStore store, String networkFile, String eventsFile, String sourceCRS) {
        this.store = store;
        this.eventsFile = eventsFile;
        this.sourceCRS = sourceCRS;
        this.networkFile = networkFile;
    }

    public static void main(String[] arguments) throws IOException {

        var args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(arguments);

        var store = new GeomesaFileSystemStore(args.storeRoot);
        var ingester = new IngestDataFromLocalDisk(store, args.networkFile, args.eventsFile, args.sourceCRS);
        ingester.ingest();

    }

    static class Args {

        @Parameter(names = "-e", required = true)
        private String eventsFile;

        @Parameter(names = "-n", required = true)
        private String networkFile;

        @Parameter(names = "-s", required = true)
        private String storeRoot;

        @Parameter(names = "-crs", required = true)
        private String sourceCRS;
    }

    static Network loadNetworkAndTransform(String networkPath, String sourceCRS) {

        var transfromation = TransformationFactory.getCoordinateTransformation(sourceCRS, WGS_84);
        var network = NetworkUtils.readNetwork(networkPath);

        log.info("Transforming network from " + sourceCRS + " to " + WGS_84);
        network.getNodes().values().parallelStream().forEach(node -> {

            var transformedCoord = transfromation.transform(node.getCoord());
            node.setCoord(transformedCoord);
        });
        return network;
    }

    public void ingest() throws IOException {

        var network = loadNetworkAndTransform(networkFile, sourceCRS);

        ingestNetwork(network);
        ingestEvents(network);
        writeSetInfo();
    }

    void writeSetInfo() throws IOException {
        Path infoPath = Paths.get(store.getStoreRoot()).resolve("SetInfo.json");
        var mapper = new ObjectMapper();
        mapper.writeValue(new File(infoPath.toString()), setInfo);
    }

    void ingestNetwork(Network network) throws IOException {

        log.info("Start writing network into DataStore");
        try (var writer = store.getNetworkWriter()) {
            for (var link : network.getLinks().values()) {

                var lineString = "LINESTRING ("
                        + link.getFromNode().getCoord().getX() + " "
                        + link.getFromNode().getCoord().getY() + ","
                        + link.getToNode().getCoord().getX() + " "
                        + link.getToNode().getCoord().getY() + ")";

                var toWrite = writer.next();
                toWrite.setAttribute(GeomesaFileSystemStore.NetworkSchema.GEOMETRY, lineString);
                toWrite.setAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID, link.getId());

                writer.write();

                // adjust bounding box of set info
                setInfo.getBbox().adjust(link.getFromNode().getCoord());
                setInfo.getBbox().adjust(link.getToNode().getCoord());
            }
        }
        log.info("Finished writing network into DataStore");
        log.info("Bounding box of network is: " + setInfo.getBbox().toString());
    }

    void ingestEvents(Network network) throws IOException {

        log.info("Start ingesting events.");

        try (var writer = store.getTrajectoryWriter()) {

            var handler = new TrajectoryToGeomesaHandler(writer, network);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(handler);
            manager.addHandler((BasicEventHandler) event -> setInfo.adjustStartAndEndTime(event.getTime()));
            log.info("Start parsing events");
            new MatsimEventsReader(manager).readFile(eventsFile);
            log.info("Finished parsing events");
            log.info("Start time is: " + setInfo.getStartTime() + ", end time is: " + setInfo.getEndTime());
        }
    }
}