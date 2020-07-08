package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.utils.geotools.SchemaBuilder;

public interface IntervalSchema {

    String GEOMETRY = "geom";
    String START_TIME = "startTime";
    String END_TIME = "endTime";
    String PERSON_ID = "personId";

    static SchemaBuilder createBuilderWithDefaultValues() {
        return SchemaBuilder.builder()
                .addDate(START_TIME, false).end()
                .addDate(END_TIME, true).end()
                .addString(PERSON_ID).end();
    }
}
