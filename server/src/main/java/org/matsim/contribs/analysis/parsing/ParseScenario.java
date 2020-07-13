package org.matsim.contribs.analysis.parsing;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.RequiredArgsConstructor;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.IOException;

@RequiredArgsConstructor
public class ParseScenario {

    private static final String WGS_84 = "EPSG:4326";

    private final MatsimDataStore store;
    private final String networkFile;
    private final String eventsFile;
    private final String sourceCRS;

    public static void main(String[] arguments) throws IOException {

        var args = new ParseScenario.Args();
        JCommander.newBuilder().addObject(args).build().parse(arguments);

        var store = new MatsimDataStore(args.storeRoot);
        var parser = new ParseScenario(store, args.networkFile, args.eventsFile, args.sourceCRS);
        parser.parse();
    }

    private void parse() {

        var transformation = TransformationFactory.getCoordinateTransformation(sourceCRS, WGS_84);
        var network = NetworkUtils.readNetwork(networkFile);

        // transform network to wgs84
        network.getNodes().values().parallelStream()
                .forEach(node -> {
                    var coord = transformation.transform(node.getCoord());
                    node.setCoord(coord);
                });

        try (var legWriter = store.getLegWriter(); var linkTripWriter = store.getLinkTripWriter(); var activityWriter = store.getActivityWriter()) {

            var movementHandler = new MovementHandler(linkTripWriter, legWriter, network);
            var activityHandler = new ActivityHandler(activityWriter, network, null);
            var manager = EventsUtils.createEventsManager();
            manager.addHandler(movementHandler);
            manager.addHandler(activityHandler);
            manager.initProcessing();
            new MatsimEventsReader(manager).readFile(eventsFile);
            manager.finishProcessing();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
