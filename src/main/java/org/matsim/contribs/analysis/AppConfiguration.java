package org.matsim.contribs.analysis;

import io.dropwizard.Configuration;
import lombok.Getter;

import javax.validation.constraints.NotEmpty;

@Getter
public class AppConfiguration extends Configuration {

    @NotEmpty
    private final String storeRoot = "";

    @NotEmpty
    private final String eventsFile = "";

    @NotEmpty
    private final String networkFile = "";
}
