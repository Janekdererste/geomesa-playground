package org.matsim.contribs.analysis;

import lombok.extern.slf4j.Slf4j;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.IOException;

@Slf4j
public class IngestDataFromLocalDisk {

    private static final String WGS_84 = "EPSG:4326";

    public static void ingestNetwork(GeomesaFileSystemStore store, String networkPath, String sourceCRS) throws IOException {

        var transfromation = TransformationFactory.getCoordinateTransformation(sourceCRS, WGS_84);
        var network = NetworkUtils.readNetwork(networkPath);

        log.info("Transforming network from " + sourceCRS + " to " + WGS_84);
        network.getNodes().values().parallelStream().forEach(node -> {

            var transformedCoord = transfromation.transform(node.getCoord());
            node.setCoord(transformedCoord);
        });

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
}
