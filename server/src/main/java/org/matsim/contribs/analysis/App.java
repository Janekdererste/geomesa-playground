package org.matsim.contribs.analysis;

import io.dropwizard.Application;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlets.CrossOriginFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.nio.file.Paths;
import java.util.EnumSet;

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

        log.info("Registering resources");
        environment.jersey().register(new NetworkResource(store));
        environment.jersey().register(new TrajectoryResource(store));
        environment.jersey().register(new InfoResource(Paths.get(appConfiguration.getStoreRoot()).resolve("SetInfo.json")));

        registerCORSFilter(environment.servlets());
    }

    private void registerCORSFilter(ServletEnvironment servlet) {

        final FilterRegistration.Dynamic cors = servlet.addFilter("CORS", CrossOriginFilter.class);
        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET, POST, PUT, OPTIONS, DELETE, PATCH");
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Authorization, Content-Type");
        cors.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        cors.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, Boolean.FALSE.toString());
    }
}
