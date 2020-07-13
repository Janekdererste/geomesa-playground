package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Collections;

public class LegSchema implements IntervalSchema {

    public static final String START_LINK_ID = "startLink";
    public static final String END_LINK_ID = "endLink";
    public static final String MODE = "mode";
    public static final String TYPE = "type";

    public enum LegType {NETWORK, TELEPORTED, PT_PASSENGER, PT_VEHICLE}

    private static final SimpleFeatureType schema = createSchema();

    public static SimpleFeatureType getSchema() {

        ConfigurationUtils.setScheme(schema, "daily,xz2-2bit", Collections.emptyMap());
        schema.getUserData().put("geomesa.fid.uuid", "true");
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
                .addString(MODE).end()
                .addString(TYPE).end()
                .build("legs");
    }
}
