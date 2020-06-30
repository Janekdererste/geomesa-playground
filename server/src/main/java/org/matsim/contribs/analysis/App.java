package org.matsim.contribs.analysis;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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

        var store = new GeomesaFileSystemStore(appConfiguration.getStoreRoot());
        var networkResource = new NetworkResource(store);
        environment.jersey().register(networkResource);
    }
}
