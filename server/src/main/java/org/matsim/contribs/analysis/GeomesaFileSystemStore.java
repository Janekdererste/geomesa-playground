package org.matsim.contribs.analysis;

import lombok.Getter;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureStore;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.geotools.SchemaBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import scala.reflect.ClassTag$;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class GeomesaFileSystemStore {

    private final DataStore store;
    @Getter
    private final String storeRoot;

    public GeomesaFileSystemStore(String storeRoot) throws IOException {

        this.storeRoot = storeRoot;
        Map<String, Serializable> params = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var newStore = new FileSystemDataStoreFactory().createDataStore(params);
        newStore.createSchema(NetworkSchema.getSchema());
        newStore.createSchema(TrajectorySchema.getSchema());
        newStore.createSchema(GeometryLessSchema.getSchema());
        // eventually add other schemas
        this.store = newStore;
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getNetworkWriter() throws IOException {
        return store.getFeatureWriterAppend(NetworkSchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getTrajectoryWriter() throws IOException {
        return store.getFeatureWriterAppend(TrajectorySchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getBla() throws IOException {
        return store.getFeatureWriterAppend(GeometryLessSchema.getSchema().getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getBlaReader(Filter filter) throws IOException {

        var query = new Query(GeometryLessSchema.getSchema().getTypeName(), filter);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getNetworkReader(Filter filter) throws IOException {

        var query = new Query(NetworkSchema.getTypeName(), filter);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public void wipe() throws IOException {

        ((SimpleFeatureStore) store.getFeatureSource(NetworkSchema.getTypeName())).removeFeatures(Filter.INCLUDE);
        ((SimpleFeatureStore) store.getFeatureSource(TrajectorySchema.getTypeName())).removeFeatures(Filter.INCLUDE);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getTrajectoryReader(Filter filter) throws IOException {

        var query = new Query(TrajectorySchema.getTypeName(), filter);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public Collection<SimpleFeature> getTrajectoryFeatureCollection(Filter filter) throws IOException {

        List<SimpleFeature> result = new ArrayList<>();
        try (var reader = getTrajectoryReader(filter)) {
            while (reader.hasNext()) {
                result.add(reader.next());
            }
        }

        return result;
    }

    public Collection<SimpleFeature> getNetworkFeatureCollection(Filter filter) throws IOException {

        List<SimpleFeature> result = new ArrayList<>();
        try (var reader = getNetworkReader(filter)) {
            while (reader.hasNext()) {
                result.add(reader.next());
            }
        }

        return result;
    }

    public void dispose() {
        this.store.dispose();
    }

    public static class NetworkSchema {

        public static final String GEOMETRY = "geom";
        public static final String LINK_ID = "linkId";

        private static final SimpleFeatureType schema = createShema();

        public static SimpleFeatureType getSchema() {
            // this basically puts everything into one bucket. maybe change this depending on the network size when
            // perfomance must be tweaked
            // the user data is deleted every time a store creates its schemas. So, put it in every time schema is requested
            ConfigurationUtils.setScheme(schema, "xz2-2bit", Collections.emptyMap());
            return schema;
        }

        public static String getTypeName() {
            return schema.getTypeName();
        }

        private static SimpleFeatureType createShema() {

            return SchemaBuilder.builder()
                    .addLineString(GEOMETRY, true).end()
                    .addString(LINK_ID).withIndex().end()
                    .build("network");
        }
    }

    public static class TrajectorySchema {

        public static final String GEOMETRY = "geom";
        public static final String ENTER_TIME = "enterTime";
        public static final String EXIT_TIME = "exitTime";
        public static final String TIMES = "times";
        public static final String LINK_IDS = "linkIds";
        public static final String MODE = "mode";
        public static final String IS_TELEPORTED = "isTeleported";
        public static final String VEHICLE_ID = "vehicleId";
        public static final String AGENT_ID = "agentId";

        private static final SimpleFeatureType schema = createSchema();

        public static SimpleFeatureType getSchema() {
            // sort data into daily buckets. Also use 2bit geohash for file partitions as a starter - probably room for improvement.
            ConfigurationUtils.setScheme(schema, "hourly,xz2-2bit", Collections.emptyMap());
            schema.getUserData().put("geomesa.z3.interval", "hour");
            return schema;
        }

        public static String getTypeName() {
            return schema.getTypeName();
        }

        private static SimpleFeatureType createSchema() {

            return SchemaBuilder.builder()
                    .addLineString(GEOMETRY, true).end()
                    .addDate(ENTER_TIME, true).end()
                    .addDate(EXIT_TIME, false).end() // only one date can be part of the index. Time range queries have to be executed as secondary condition
                    .addList(TIMES, ClassTag$.MODULE$.apply(Double.class)).end() // this could probably be a float, since webgl only processes floats
                    .addList(LINK_IDS, ClassTag$.MODULE$.apply(String.class)).end()
                    .addString(MODE).end() // have the mode, so that it is possible to have different symbols
                    .addBoolean(IS_TELEPORTED).end()
                    .addString(VEHICLE_ID).end()
                    .addString(AGENT_ID).withIndex().end() // set agent id as index, so that events trajectories can be filtered by agent
                    .build("trajectory");
        }
    }

    public static class GeometryLessSchema {

        public static final String ANY_FIELD = "bla";

        private static final SimpleFeatureType schema = createSchema();

        private static SimpleFeatureType getSchema() {

            ConfigurationUtils.setScheme(schema, "attribute", Map.of("partitioned-attribute", "bla"));
            return schema;
        }

        private static SimpleFeatureType createSchema() {

            return SchemaBuilder.builder()
                    .addString(ANY_FIELD).withIndex().end()
                    .build("blup");
        }
    }

    static class PointDurationSchema {

    }
}
