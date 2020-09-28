package org.matsim.contribs.analysis.parsing;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contribs.analysis.SetInformation;
import org.matsim.contribs.analysis.store.LinkSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
public class ParseScenario {

    private static final String WGS_84 = "EPSG:4326";
    private static final GeometryFactory gFactory = new GeometryFactory();

    private final MatsimDataStore store;
    private final SetInformation setInfo = new SetInformation();
    private final String networkFile;
    private final String eventsFile;
    private final String setName;
    private final CoordinateTransformation transformation;


    public static void main(String[] arguments) throws IOException {

        var args = new ParseScenario.Args();
        JCommander.newBuilder().addObject(args).build().parse(arguments);

        var store = new MatsimDataStore(args.storeRoot);
        log.info("Transforming from " + args.sourceCRS + " to " + WGS_84);
        var transformation = TransformationFactory.getCoordinateTransformation(args.sourceCRS, WGS_84);
        var parser = new ParseScenario(store, args.networkFile, args.eventsFile, args.setName, transformation);
        parser.parse();
    }

    public void parse() {

        var network = loadNetworkAndTransform(networkFile);

        try (var linkWriter = store.getLinkWriter()) {

            for (Link link : network.getLinks().values()) {

                var geometry = gFactory.createLineString(new Coordinate[]{
                        MGC.coord2Coordinate(link.getFromNode().getCoord()), MGC.coord2Coordinate(link.getToNode().getCoord())
                });
                var modesAsString = String.join(" ", link.getAllowedModes());

                setInfo.getModesInNetwork().add(modesAsString);

                var toWrite = linkWriter.next();
                toWrite.setAttribute(LinkSchema.GEOMETRY, geometry);
                toWrite.setAttribute(LinkSchema.LINK_ID, link.getId().toString());
                toWrite.setAttribute(LinkSchema.FROM_NODE_ID, link.getFromNode().getId().toString());
                toWrite.setAttribute(LinkSchema.TO_NODE_ID, link.getToNode().getId().toString());
                toWrite.setAttribute(LinkSchema.ALLOWED_MODES, modesAsString);
                toWrite.setAttribute(LinkSchema.CAPACITY, link.getCapacity());
                toWrite.setAttribute(LinkSchema.FREESPEED, link.getFreespeed());
                toWrite.setAttribute(LinkSchema.LENGTH, link.getLength());

                // store link attributes as json property in the future

                linkWriter.write();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (var legWriter = store.getLegWriter(); var linkTripWriter = store.getLinkTripWriter(); var activityWriter = store.getActivityWriter()) {

            var movementHandler = new MovementHandler(linkTripWriter, legWriter, network);
            var activityHandler = new ActivityHandler(activityWriter, network, null, transformation);
            var timeHandler = new BasicEventHandler() {

                @Override
                public void handleEvent(Event event) {
                    setInfo.adjustStartAndEndTime(event.getTime());
                }
            };
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(movementHandler);
            manager.addHandler(activityHandler);
            manager.addHandler(timeHandler);
            manager.initProcessing();
            new MatsimEventsReader(manager).readFile(eventsFile);
            manager.finishProcessing();

            // the activity handler must merge overnight activities
            activityHandler.writeUnfinishedActivities();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            writeSetInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Network loadNetworkAndTransform(String networkPath) {

        var network = NetworkUtils.readNetwork(networkPath);

        network.getNodes().values().parallelStream().forEach(node -> {

            var transformedCoord = transformation.transform(node.getCoord());
            node.setCoord(transformedCoord);

            setInfo.getBbox().adjust(transformedCoord);
        });
        return network;
    }

    private void writeSetInfo() throws IOException {
        Path infoPath = Paths.get(store.getStoreRoot()).resolve(this.setName + "-set-info.json");

        log.info("Writing set info to: " + infoPath);
        var mapper = new ObjectMapper();
        mapper.writeValue(new File(infoPath.toString()), setInfo);
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

        @Parameter(names = "-sn", required = true)
        private String setName;
    }
}
