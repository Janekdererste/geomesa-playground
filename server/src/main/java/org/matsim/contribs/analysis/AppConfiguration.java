package org.matsim.contribs.analysis;

import io.dropwizard.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AppConfiguration extends Configuration {

    @NotEmpty
    private final String storeRoot = "";
}
