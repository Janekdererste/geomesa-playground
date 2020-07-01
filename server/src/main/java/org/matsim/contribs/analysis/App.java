package org.matsim.contribs.analysis;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App extends Application<AppConfiguration> {

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        super.initialize(bootstrap);
    }

    @Override
    public void run(AppConfiguration appConfiguration, Environment environment) throws Exception {

        log.info("Creating store at: " + appConfiguration.getStoreRoot());
        var store = new GeomesaFileSystemStore(appConfiguration.getStoreRoot());

        log.info("Deleting previously ingested stuff");
        store.wipe();
        IngestDataFromLocalDisk.ingestNetwork(store, appConfiguration.getNetworkFile(), "EPSG:3857");

        log.info("Registering resources");
        var networkResource = new NetworkResource(store);
        environment.jersey().register(networkResource);
    }
}
