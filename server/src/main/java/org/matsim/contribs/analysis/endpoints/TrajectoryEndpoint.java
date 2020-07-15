package org.matsim.contribs.analysis.endpoints;

import lombok.RequiredArgsConstructor;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Geometry;
import org.matsim.contribs.analysis.Transformation;
import org.matsim.contribs.analysis.api.LinkTrip;
import org.matsim.contribs.analysis.api.SimpleCoordinate;
import org.matsim.contribs.analysis.store.LinkTripSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Path("/trajectory")
@RequiredArgsConstructor
public class TrajectoryEndpoint {

    private static final FilterFactory2 ff = new FilterFactoryImpl();

    private final MatsimDataStore store;

    @GET
    public Collection<LinkTrip> getTrajectories(@QueryParam("fromTime") long fromTime, @QueryParam("toTime") long toTime) {

        var from = new Date(fromTime * 1000);
        var to = new Date(toTime * 1000);

        var endFilter = ff.after(ff.property(LinkTripSchema.END_TIME), ff.literal(from));
        var startFilter = ff.before(ff.property(LinkTripSchema.START_TIME), ff.literal(to));
        var filter = ff.and(endFilter, startFilter);

        // again results should be streamed for performance reasons, but this does for now
        List<LinkTrip> result = new ArrayList<>();

        store.forEachLinkTrip(filter, feature -> {
            try {

                var geometry = (Geometry) feature.getDefaultGeometry();
                var mercatorGeometry = JTS.transform(geometry, Transformation.TRANSFORM);

                var coordinates = mercatorGeometry.getCoordinates();
                var startTime = (Date) feature.getAttribute(LinkTripSchema.START_TIME);
                var endTime = (Date) feature.getAttribute(LinkTripSchema.END_TIME);

                var wireFormat = new LinkTrip(
                        new SimpleCoordinate(coordinates[0]),
                        new SimpleCoordinate(coordinates[1]),
                        startTime.getTime() / 1000, endTime.getTime() / 1000
                );

                result.add(wireFormat);
            } catch (TransformException e) {
                e.printStackTrace();
            }
        });

        return result;
    }
}
