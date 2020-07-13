package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Collections;

public class LegSchema implements IntervalSchema {

    private static final String START_LINK_ID = "startLink";
    private static final String END_LINK_ID = "endLink";
    private static final String VEHICLE_ID = "vehicleId";
    private static final String MODE = "mode";
    private static final String TYPE = "type";

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
                .addString(START_LINK_ID).end()
                .addString(END_LINK_ID).end()
                .addString(VEHICLE_ID).end()
                .addString(MODE).end()
                .addString(TYPE).end()
                .build("legs");
    }
}
