package org.matsim.contribs.analysis.store;

import org.locationtech.geomesa.utils.geotools.SchemaBuilder;

public interface IntervalSchema {

    String GEOMETRY = "geom";
    String START_TIME = "startTime";
    String END_TIME = "endTime";
    String PERSON_ID = "personId";
    String SET_ID = "setId";

    static SchemaBuilder.AbstractSchemaBuilder<SchemaBuilder.AttributeBuilder, ? extends SchemaBuilder.AbstractUserDataBuilder<?>> createBuilderWithDefaultValues() {
        return SchemaBuilder.builder()
                .addDate(START_TIME, false).end()
                .addDate(END_TIME, true).end()
                .addString(PERSON_ID).withIndex().end()
                .addString(SET_ID).withIndex().end();
    }
}
