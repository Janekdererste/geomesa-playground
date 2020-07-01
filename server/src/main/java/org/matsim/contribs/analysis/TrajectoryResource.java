package org.matsim.contribs.analysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Path("/trajectory")
@RequiredArgsConstructor
public class TrajectoryResource {

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
    public Collection<SimpleTrajectory> getTrajectoriesAsJson() throws IOException {

        // get all trajectories for now. This obviously is only suitable for small scenarios
        var trajectoryFeatures = store.getTrajectoryFeatureCollection(Filter.INCLUDE);
        return trajectoryFeatures.stream()
                .map(feature -> {

                    try {
                        // transform the network into pseudo mercartor because that's what the client understands
                        var geometry = (Geometry) feature.getDefaultGeometry();
                        var mercartorGeometry = JTS.transform(geometry, transformation);

                        var coordinates = mercartorGeometry.getCoordinates();
                        var times = (List<Double>) feature.getAttribute(GeomesaFileSystemStore.TrajectorySchema.TIMES);

                        return new SimpleTrajectory(
                                Arrays.stream(coordinates).map(NetworkResource.SimpleCoordinate::new).collect(Collectors.toList()),
                                times
                        );
                    } catch (TransformException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    @Getter
    static class SimpleTrajectory {

        // exchange for another representation
        private final List<NetworkResource.SimpleCoordinate> coords;
        private final List<Double> times;


    }
}
