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
import java.util.function.Consumer;

public class MatsimDataStore {

    private final DataStore dataStore;

    @Getter
    private final String storeRoot;

    public MatsimDataStore(String storeRoot) throws IOException {

        this.storeRoot = storeRoot;
        Map<String, Serializable> params = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var newStore = new FileSystemDataStoreFactory().createDataStore(params);

        newStore.createSchema(ActivitySchema.getSchema());
        newStore.createSchema(LinkTripSchema.getSchema());
        newStore.createSchema(LegSchema.getSchema());

        this.dataStore = newStore;
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getActivityWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(ActivitySchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getLegWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(LegSchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureWriter<SimpleFeatureType, SimpleFeature> getLinkTripWriter() throws IOException {
        return dataStore.getFeatureWriterAppend(LinkTripSchema.getTypeName(), Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getActivityReader(Filter filter) throws IOException {

        var query = new Query(ActivitySchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public void forEachActivity(Filter filter, Consumer<SimpleFeature> action) {
        forEach(ActivitySchema.getTypeName(), filter, action);
    }

    public void forEachLeg(Filter filter, Consumer<SimpleFeature> action) {
        forEach(LegSchema.getTypeName(), filter, action);
    }

    public void forEachLinkTrip(Filter filter, Consumer<SimpleFeature> action) {
        forEach(LinkTripSchema.getTypeName(), filter, action);
    }

    private void forEach(String schema, Filter filter, Consumer<SimpleFeature> action) {
        var query = new Query(schema, filter);
        try (var reader = dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT)) {
            while (reader.hasNext()) {
                action.accept(reader.next());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getLegReader(Filter filter) throws IOException {

        var query = new Query(LegSchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }

    public FeatureReader<SimpleFeatureType, SimpleFeature> getLinkTripReader(Filter filter) throws IOException {

        var query = new Query(LinkTripSchema.getTypeName(), filter);
        return dataStore.getFeatureReader(query, Transaction.AUTO_COMMIT);
    }


}
