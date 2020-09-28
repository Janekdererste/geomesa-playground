package org.matsim.contribs.analysis.endpoints;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.contribs.analysis.parsing.ParseScenario;
import org.matsim.contribs.analysis.store.MatsimDataStore;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.testcases.MatsimTestUtils;
import org.opengis.referencing.operation.TransformException;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class PlanEndpointTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void test() throws IOException, TransformException {

        var store = new MatsimDataStore(testUtils.getOutputDirectory() + "store");

        // parse a simple scenario
        var parser = new ParseScenario(store, "C:\\Users\\Janekdererste\\Desktop\\equil-scenario\\output-100-agent\\output_network.xml.gz", "C:\\Users\\Janekdererste\\Desktop\\equil-scenario\\output-100-agent\\output_events.xml.gz", "test", TransformationFactory.getCoordinateTransformation("EPSG:3857", "EPSG:4326"));
        parser.parse();

        var endpoint = new PlanEndpoint(store);
        var result = endpoint.getPlan("1");

        assertNotNull(result);
    }

}