package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Collections;

public class LinkTripSchema implements IntervalSchema {

    public static final String LINK_ID = "linkId";
    public static final String LEG_ID = "legId";
    public static final String VEHICLE_ID = "vehicleId";

    private static final SimpleFeatureType schema = createSchema();

    public static SimpleFeatureType getSchema() {

        ConfigurationUtils.setScheme(schema, "daily,xz2-2bit", Collections.emptyMap());
        schema.getUserData().put("geomesa.z3.interval", "day");
        return schema;
    }

    public static String getTypeName() {
        return schema.getTypeName();
    }

    private static SimpleFeatureType createSchema() {

        return IntervalSchema.createBuilderWithDefaultValues()
                .addLineString(GEOMETRY, true).end()
                .addUuid(LEG_ID).withIndex().end()
                .addString(LINK_ID).end()
                .build("linkMovements");
    }
}
