package org.matsim.contribs.analysis.endpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.Query;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.matsim.contribs.analysis.Transformation;
import org.matsim.contribs.analysis.api.ApiPlan;
import org.matsim.contribs.analysis.api.SimpleCoordinate;
import org.matsim.contribs.analysis.store.ActivitySchema;
import org.matsim.contribs.analysis.store.LegSchema;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

@Path("/plan")
@RequiredArgsConstructor
@Slf4j
public class PlanEndpoint {

    private static final FilterFactory2 ff = new FilterFactoryImpl();

    private final MatsimDataStore store;

    @GET
    @Path("{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiPlan getPlan(@PathParam("personId") String personId) throws TransformException {

        var personFilter = ff.equals(ff.property(ActivitySchema.PERSON_ID), ff.literal(personId));
        var sortBy = ff.sort(ActivitySchema.END_TIME, SortOrder.ASCENDING);

        var activityQuery = new Query(ActivitySchema.getTypeName(), personFilter);
        activityQuery.setSortBy(new SortBy[]{sortBy});

        Queue<SimpleFeature> activities = new LinkedList<>();
        store.forEachActivity(activityQuery, activities::add);

        var legQuery = new Query(LegSchema.getTypeName(), personFilter);
        legQuery.setSortBy(new SortBy[]{sortBy});
        Queue<SimpleFeature> legs = new LinkedList<>();
        store.forEachLeg(legQuery, legs::add);

        var plan = new ApiPlan();

        // usually plans start and end with activities
        while (legs.size() > 0) {

            var activity = convertToActivity(Objects.requireNonNull(activities.poll()));
            plan.addElement(activity);

            var leg = convertToLeg(Objects.requireNonNull(legs.poll()));
            plan.addElement(leg);
        }

        // add the finishing activity
        plan.addElement(convertToActivity(Objects.requireNonNull(activities.poll())));

        return plan;
    }

    /**
     * This will most probably move somewhere central, when it is used in multiple places
     */
    private ApiPlan.ApiActivity convertToActivity(SimpleFeature feature) throws TransformException {

        return new ApiPlan.ApiActivity(
                ((Date) feature.getAttribute(ActivitySchema.START_TIME)).getTime() / 1000.0,
                ((Date) feature.getAttribute(ActivitySchema.END_TIME)).getTime() / 1000.0,
                new SimpleCoordinate(extractAndTransformCoordinates(feature)[0]),
                (String) feature.getAttribute(ActivitySchema.TYPE),
                (String) feature.getAttribute(ActivitySchema.FACILITY_ID),
                (String) feature.getAttribute(ActivitySchema.LINK_ID)
        );
    }

    private ApiPlan.ApiLeg convertToLeg(SimpleFeature feature) throws TransformException {

        var coords = extractAndTransformCoordinates(feature);

        return new ApiPlan.ApiLeg(
                ((Date) feature.getAttribute(LegSchema.START_TIME)).getTime() / 1000.0,
                ((Date) feature.getAttribute(LegSchema.END_TIME)).getTime() / 1000.,
                new SimpleCoordinate(coords[0]),
                new SimpleCoordinate(coords[1]),
                (String) feature.getAttribute(LegSchema.MODE),
                (String) feature.getAttribute(LegSchema.TYPE)
        );
    }

    private Coordinate[] extractAndTransformCoordinates(SimpleFeature feature) throws TransformException {
        var geometry = (Geometry) feature.getDefaultGeometry();
        var mercartorGeometry = JTS.transform(geometry, Transformation.TRANSFORM);
        return mercartorGeometry.getCoordinates();
    }
}
