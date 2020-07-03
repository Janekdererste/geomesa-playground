package org.matsim.contribs.analysis;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunScenario {

    @Parameter(names = "-c")
    private String configPath = "";

    public static void main(String[] args) {

        var runner = new RunScenario();
        JCommander.newBuilder().addObject(runner).build().parse(args);
        runner.run();
    }

    private void run() {

        var config = ConfigUtils.loadConfig(configPath);
        config.controler().setFirstIteration(0);
        config.controler().setLastIteration(0);
        var scenario = ScenarioUtils.loadScenario(config);
        var controler = new Controler(scenario);
        controler.run();

    }
}
