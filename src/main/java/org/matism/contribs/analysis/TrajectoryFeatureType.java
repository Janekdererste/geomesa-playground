package org.matism.contribs.analysis;

import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.geotools.SchemaBuilder;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.opengis.feature.simple.SimpleFeatureType;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.util.Collections;

public class TrajectoryFeatureType {

    public static final String geometry = "geometry";
    public static final String enterTime = "enterTime";
    public static final String exitTime = "exitTime";
    public static final String times = "times";
    public static final String linkIds = "linkIds";
    public static final String mode = "mode";
    public static final String isTeleported = "isTeleported";
    public static final String agentId = "agentId";

    private static SimpleFeatureType cachedSchema;

    static SimpleFeatureType createFeatureType() {

        if (cachedSchema == null) {
            var schema = SchemaBuilder.builder()
                    .addLineString(geometry, true).end()
                    .addDate(enterTime, true).end()
                    .addDate(exitTime, false).end() // only one date can be part of the index. Time range queries have to be executed as secondary condition
                    .addList(times, ClassTag$.MODULE$.apply(Double.class)).end()
                    .addList(linkIds, ClassTag$.MODULE$.apply(String.class)).end()
                    .addString(mode).end() // have the mode, so that it is possible to have different symbols
                    .addBoolean(isTeleported).end()
                    .addString(agentId).withIndex().end() // set agent id as index, so that events trajectories can be filtered by agent
                    .build("trajectory");

            // tell the file storage to partion in hours and 12bits geohashes as a default for now
            ConfigurationUtils.setScheme(schema, "hourly,xz2-12bit", Collections.emptyMap());
            cachedSchema = schema;
        }
        return cachedSchema;
    }
}
