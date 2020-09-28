package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.geotools.SchemaBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.Collections;

public class LinkSchema {

    public static final String GEOMETRY = "geom";
    public static final String LINK_ID = "linkId";
    public static final String FROM_NODE_ID = "fromNodeId";
    public static final String TO_NODE_ID = "toNodeId";
    public static final String ALLOWED_MODES = "allowedModes";
    public static final String CAPACITY = "capacity";
    public static final String FREESPEED = "freespeed";
    public static final String LENGTH = "length";
    public static final String SET_ID = "setId";

    private static final SimpleFeatureType schema = createSchema();

    static SimpleFeatureType getSchema() {

        ConfigurationUtils.setScheme(schema, "xz2-12bit", Collections.emptyMap());
        return schema;
    }

    static String getTypeName() {
        return schema.getTypeName();
    }

    private static SimpleFeatureType createSchema() {

        //TODO think about also storing link attributes as json property
        return SchemaBuilder.builder()
                .addLineString(GEOMETRY, true).end()
                .addString(LINK_ID).end() // will probably be indexed
                .addString(FROM_NODE_ID).end()
                .addString(TO_NODE_ID).end()
                .addString(ALLOWED_MODES).withIndex().end()
                .addDouble(CAPACITY).end()
                .addDouble(FREESPEED).end()
                .addDouble(LENGTH).end()
                .addString(SET_ID).withIndex().end()
                .build("links");
    }
}
