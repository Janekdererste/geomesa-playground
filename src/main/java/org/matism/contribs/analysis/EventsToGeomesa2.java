package org.matism.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.log4j.Log4j2;
import org.geotools.data.Transaction;
import org.locationtech.geomesa.fs.data.FileSystemDataStoreFactory;
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils;
import org.locationtech.geomesa.utils.interop.SimpleFeatureTypes;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Log4j2
public class EventsToGeomesa2 {

    @Parameter(names = "-e", required = true)
    private String events = "";

    @Parameter(names = "-n", required = true)
    private String networkFile = "";

    @Parameter(names = "-store", required = true)
    private String storeRoot = "";

    // this could be somewhat dynamic later on as well
    private static final CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:25832", "EPSG:4326");

    public static void main(String[] args) throws IOException {

        var converter = new EventsToGeomesa2();
        JCommander.newBuilder().addObject(converter).build().parse(args);
        converter.convert();
    }

    private void convert() throws IOException {

        Map<String, Serializable> storeParams = Map.of("fs.path", storeRoot, "fs.encoding", "parquet");
        var store = new FileSystemDataStoreFactory().createDataStore(storeParams);
        var schemaType = createSchema();
        // this will probably need a time component as well
        ConfigurationUtils.setScheme(schemaType, "z2-2bit", Collections.emptyMap());
        store.createSchema(schemaType);

        var network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        var handler = new GeomesaHandler(store.getFeatureWriterAppend(schemaType.getTypeName(), Transaction.AUTO_COMMIT), network, transformation);
        var manager = EventsUtils.createEventsManager();
        manager.addHandler(handler);
        new MatsimEventsReader(manager).readFile(events);

        store.dispose();
    }

    private SimpleFeatureType createSchema() {

        String schema = "time:Double:index=true," +
                "type:String," +
                "*geom:Point:srid=4326";
        return SimpleFeatureTypes.createType("events", schema);
    }
}
