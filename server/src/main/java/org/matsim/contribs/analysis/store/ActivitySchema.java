package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Collections;

public class ActivitySchema implements IntervalSchema {

    public static final String TYPE = "type";
    public static final String FACILITY_ID = "facilityId";
    public static final String LINK_ID = "linkId";

    private static final SimpleFeatureType schema = createSchema();

    static SimpleFeatureType getSchema() {

        ConfigurationUtils.setScheme(schema, "daily", Collections.emptyMap());
        schema.getUserData().put("geomesa.z3.interval", "day");
        return schema;
    }

    public static String getTypeName() {
        return schema.getTypeName();
    }

    private static SimpleFeatureType createSchema() {

        return IntervalSchema.createBuilderWithDefaultValues()
                .addPoint(GEOMETRY, true).end()
                .addString(TYPE).end()
                .addString(FACILITY_ID).end()
                .addString(LINK_ID).end()
                .build("activities");
    }
}
