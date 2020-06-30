package org.matsim.contribs.analysis;

import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.IOException;

public class IngestDataFromLocalDisk {

    private static final String WGS_84 = "EPSG:4326";

    public static void ingestNetwork(GeomesaFileSystemStore store, String networkPath, String sourceCRS) throws IOException {

        var transfromation = TransformationFactory.getCoordinateTransformation(sourceCRS, WGS_84);
        var network = NetworkUtils.readNetwork(networkPath);

        network.getNodes().values().parallelStream().forEach(node -> {

            var transformedCoord = transfromation.transform(node.getCoord());
            node.setCoord(transformedCoord);
        });

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
    }
}
