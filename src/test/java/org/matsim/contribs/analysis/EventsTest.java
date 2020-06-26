package org.matsim.contribs.analysis;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.ControlerUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.examples.ExamplesUtils;
import org.matsim.testcases.MatsimTestUtils;

public class EventsTest {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void testSingleAgent() {

        var configPath = ExamplesUtils.getTestScenarioURL("equil").toString();
        var config = ConfigUtils.loadConfig(configPath + "config_plans1.xml");
        config.controler().setOutputDirectory(testUtils.getOutputDirectory());
        config.controler().setFirstIteration(0);
        config.controler().setLastIteration(1);
        var scenario = ScenarioUtils.loadScenario(config);

        var controler = new Controler(scenario);
        controler.run();
    }
}
