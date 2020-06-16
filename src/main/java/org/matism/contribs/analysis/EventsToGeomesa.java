package org.matism.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.log4j.Log4j2;
import org.apache.parquet.hadoop.util.ConfigurationUtil;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.locationtech.geomesa.fs.data.FileSystemDataStore;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class EventsToGeomesa {

    // command line args taken from the example tutorial
    private static Map<String, Serializable> params = Map.of("fs.path", "C:\\Users\\Janekdererste\\Desktop\\geo-mesa-test",
            "fs.encoding", "parquet");

    @Parameter(names = "-e", required = false)
    private String events = "";

    public static void main(String[] args) throws IOException, CQLException {

        var runner = new EventsToGeomesa();
        JCommander.newBuilder().addObject(runner).build().parse(args);
        runner.run();
    }

    private void run() throws IOException, CQLException {

        // create a file system geo mesa repository
        // hm, this is maybe something one would want to
        log.info("create data storage");
        var dataStore = new FileSystemDataStoreFactory().createDataStore(params);

        // define a feature type in our case this could be a point and the event type maybe
        log.info("create type 'event'");
        var type = createType();

        // the tutorial also sets a primary key, lets see, whether we can do without

        // specify a scheme. We are using 'z2' which is 2d-spatial index
        log.info("set scheme");
        ConfigurationUtils.setScheme(type, "z2-2bit", Collections.emptyMap());

        // create the schema in the data store to store events
        log.info("create schema in data store");
        dataStore.createSchema(type);

        // create some features
        log.info("generate features");
        var features = createFeatures(type);

        // write the features into the data store
        log.info("writing features");
        try(var writer = dataStore.getFeatureWriterAppend(type.getTypeName(), Transaction.AUTO_COMMIT)) {
            for (SimpleFeature feature : features) {

                var toWrite = writer.next();

                // copy the data into the writers feature
                toWrite.setAttributes(feature.getAttributes());

                // the tutorial also generates an id but we omit it here and let the database
                // generate a uuid automatically

                // also copy the user data
                toWrite.getUserData().putAll(feature.getUserData());

                //write
                log.info("write feature: " + toWrite.getID());
                writer.write();
            }
        }

        // now query the inserted data
        log.info("querying features for event type");
        try(var reader = dataStore.getFeatureReader(createEventTypeQuery(type), Transaction.AUTO_COMMIT)) {

            var counter = 0;
            while(reader.hasNext()) {
                var feature = reader.next();
                StringBuilder message = new StringBuilder(feature.getID() + ": ");
                for (Object attribute : feature.getAttributes()) {
                    message.append(attribute.toString()).append(", ");
                }
                log.info("fetched: " + message);

                counter++;
            }

            log.info("fetched " + counter + " features");
        }

        log.info("querying features for bbox");
        try (var reader = dataStore.getFeatureReader(createBoundingBoxQuery(type), Transaction.AUTO_COMMIT)) {

            var counter = 0;
            while (reader.hasNext()) {
                var feature = reader.next();
                StringBuilder message = new StringBuilder(feature.getID() + ": ");
                for (Object attribute : feature.getAttributes()) {
                    message.append(attribute.toString()).append(", ");
                }
                log.info("fetched: " + message);
                counter++;
            }

            log.info("fetched " + counter + " features");
        }

        // this would go into some finally block I guess
        dataStore.dispose();

    }

    private SimpleFeatureType createType() {

        String builder = "time:Double," +
                "type:String:index=true," +
                "*geom:Point:srid=4326";
        return SimpleFeatureTypes.createType("event", builder);

    }

    private List<SimpleFeature> createFeatures(SimpleFeatureType type) {

        var builder = new SimpleFeatureBuilder(type);
        builder.set("time", 1.);
        builder.set("type", "LinkEnterEvent");
        builder.set("geom", "POINT (" + 10 + " " + 10 + ")");
        var feature = builder.buildFeature(UUID.randomUUID().toString());

        builder.set("type", "LinkLeaveEvent");
        builder.set("geom", "POINT (" + 100 + " " + 30 + ")");
        builder.set("time", 2.);
        var feature2 = builder.buildFeature(UUID.randomUUID().toString());
        return List.of(feature, feature2);
    }

    private Query createBoundingBoxQuery(SimpleFeatureType type) throws CQLException {

        var bbox = "bbox(geom,0,0,20,20)";
        return new Query(type.getTypeName(), ECQL.toFilter(bbox));
    }

    private Query createEventTypeQuery(SimpleFeatureType type) throws CQLException {
        return new Query(type.getTypeName(), ECQL.toFilter("type = 'LinkLeaveEvent'"));
    }
}
