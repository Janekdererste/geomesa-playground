package org.matism.contribs.analysis;

import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.geotools.SchemaBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class GeomesaFileSystemStore {

    private final DataStore store;

    public GeomesaFileSystemStore(String storeRoot) throws IOException {

        Map<String, Serializable> params = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var newStore = new FileSystemDataStoreFactory().createDataStore(params);
        newStore.createSchema(NetworkSchema.getSchema());
        // eventually add other schemas
        this.store = newStore;
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getNetworkWriter() throws IOException {
        return store.getFeatureWriterAppend(NetworkSchema.getSchema().getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getNetworkReader(Filter filter) throws IOException {

        var query = new Query(NetworkSchema.getTypeName(), filter);
        return store.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public SimpleFeatureCollection getNetworkFeatures(Filter filter) throws IOException {

        var query = new Query(NetworkSchema.getTypeName(), filter);
        var source = store.getFeatureSource(NetworkSchema.getTypeName());
        return source.getFeatures(query);
    }

    public void dispose() {
        this.store.dispose();
    }

    public static class NetworkSchema {

        public static final String GEOMETRY = "geom";
        public static final String LINK_ID = "linkId";

        private static final SimpleFeatureType schema = createShema();

        public static SimpleFeatureType getSchema() {
            return schema;
        }

        public static String getTypeName() {
            return schema.getTypeName();
        }

        private static SimpleFeatureType createShema() {

            var schema = SchemaBuilder.builder()
                    .addLineString(GEOMETRY, true).end()
                    .addString(LINK_ID).withIndex().end()
                    .build("network");

            // this basically puts everything into one bucket. maybe change this depending on the network size when
            // perfomance must be tweaked
            ConfigurationUtils.setScheme(schema, "xz2-2bit", Collections.emptyMap());
            return schema;
        }
    }

    static class TrajectorySchema {

    }

    static class PointDurationSchema {

    }
}
