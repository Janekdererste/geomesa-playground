package org.matsim.contribs.analysis.store;

import lombok.Getter;
import org.geotools.data.*;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class MatsimRunStore {

    private final DataStore dataStore;

    @Getter
    private final String storeRoot;

    public MatsimRunStore(String storeRoot) throws IOException {

        this.storeRoot = storeRoot;
        Map<String, Serializable> params = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var newStore = new FileSystemDataStoreFactory().createDataStore(params);

        newStore.createSchema(ActivitySchema.getSchema());
        newStore.createSchema(LinkMovementSchema.getSchema());
        newStore.createSchema(LegSchema.getSchema());

        this.dataStore = newStore;
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getActivityWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(ActivitySchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getLegWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(LegSchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getLinkMovementWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(LinkMovementSchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getActivityReader(Filter filter) throws IOException {

        var query = new Query(ActivitySchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getLegReader(Filter filter) throws IOException {

        var query = new Query(LegSchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getLinkMovementReader(Filter filter) throws IOException {

        var query = new Query(LinkMovementSchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }
}
