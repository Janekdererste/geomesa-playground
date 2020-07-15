package org.matsim.contribs.analysis.endpoints;

import lombok.RequiredArgsConstructor;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Geometry;
import org.matsim.contribs.analysis.Transformation;
import org.matsim.contribs.analysis.api.SimpleCoordinate;
import org.matsim.contribs.analysis.api.SimpleLink;
import org.matsim.contribs.analysis.store.LinkSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Path("/network")
@RequiredArgsConstructor
public class NetworkEndpoint {

    private static final FilterFactory2 ff = new FilterFactoryImpl();

    private final MatsimDataStore store;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<SimpleLink> getTrajectoryAsJson(@QueryParam("modes") String modes) {

        // duh!, don't use 'equal' but 'equals'!!!
        var filter = ff.equals(ff.property(LinkSchema.ALLOWED_MODES), ff.literal(modes));

        // collect things in a list and then send it.
        // later it would make sense to just stream features as we read them in
        List<SimpleLink> result = new ArrayList<>();

        store.forEachLink(filter, feature -> {

            try {
                var geometry = (Geometry) feature.getDefaultGeometry();
                var mercatorGeometry = JTS.transform(geometry, Transformation.TRANSFORM);

                var coordinates = mercatorGeometry.getCoordinates();
                var linkId = (String) feature.getAttribute(LinkSchema.LINK_ID);

                var wireFormat = new SimpleLink(new SimpleCoordinate(coordinates[0]), new SimpleCoordinate(coordinates[1]), linkId);
                result.add(wireFormat);
            } catch (TransformException e) {

                // this should throw something indicating a 500 error
                throw new RuntimeException(e);
            }
        });

        return result;
    }
}
