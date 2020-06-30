package org.matsim.contribs.analysis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Path("/network")
public class NetworkResource {

    private static final MathTransform transformation = createTransformation();

    private final GeomesaFileSystemStore store;

    private static MathTransform createTransformation() {

        try {
            // this will yield lon/lat instead of the default lat/lon which would result in x and y- coordinates being swapped
            // don't know what's the correct way, but doing it like this because I can wrap my head around it better this way.
            var sourceCRS = CRS.decode("EPSG:4326", true);
            var targetCRS = CRS.decode("EPSG:3857");

            return CRS.findMathTransform(sourceCRS, targetCRS);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<SimpleLink> getNetworkAsJson() throws IOException {

        var linkFeatures = store.getNetworkFeatureCollection(Filter.INCLUDE);
        return linkFeatures.stream()
                .map(feature -> {

                    try {
                        // transform the network into pseudo mercartor because that's what the client understands
                        var geometry = (Geometry) feature.getDefaultGeometry();
                        var mercartorGeometry = JTS.transform(geometry, transformation);

                        // get the data to send over the wire
                        var coordinates = mercartorGeometry.getCoordinates();
                        var linkId = (String) feature.getAttribute(GeomesaFileSystemStore.NetworkSchema.LINK_ID);

                        // assuming we have a line string with two points
                        return new SimpleLink(coordinates[0], coordinates[1], linkId);

                    } catch (TransformException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @RequiredArgsConstructor
    @Getter
    static class SimpleLink {

        private final Coordinate from;
        private final Coordinate to;
        private final String linkId;
    }
}
