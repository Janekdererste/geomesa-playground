package org.matsim.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.IOException;

@Slf4j
public class IngestDataFromLocalDisk {

    private static final String WGS_84 = "EPSG:4326";

    public static void main(String[] arguments) throws IOException {

        var args = new Args();
        JCommander.newBuilder().addObject(args).build().parse(arguments);

        var store = new GeomesaFileSystemStore(args.storeRoot);
        ingestNetwork(store, args.networkFile, args.sourceCRS);
        ingestEvents(store, args.eventsFile, args.networkFile, args.sourceCRS);
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

    public static void ingestNetwork(GeomesaFileSystemStore store, String networkPath, String sourceCRS) throws IOException {

        var network = loadNetworkAndTransform(networkPath, sourceCRS);

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
            }
        }
        log.info("Finished writing network into DataStore");
    }

    static void ingestEvents(GeomesaFileSystemStore store, String eventsPath, String networkPath, String sourceCrs) throws IOException {

        log.info("Start ingesting events.");
        var network = loadNetworkAndTransform(networkPath, sourceCrs);

        try (var writer = store.getTrajectoryWriter()) {

            var handler = new TrajectoryToGeomesaHandler(writer, network);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(handler);
            log.info("Start parsing events");
            new MatsimEventsReader(manager).readFile(eventsPath);
            log.info("Finished parsing events");
        }
    }

    private static Network loadNetworkAndTransform(String networkPath, String sourceCRS) {

        var transfromation = TransformationFactory.getCoordinateTransformation(sourceCRS, WGS_84);
        var network = NetworkUtils.readNetwork(networkPath);

        log.info("Transforming network from " + sourceCRS + " to " + WGS_84);
        network.getNodes().values().parallelStream().forEach(node -> {

            var transformedCoord = transfromation.transform(node.getCoord());
            node.setCoord(transformedCoord);
        });
        return network;
    }
}