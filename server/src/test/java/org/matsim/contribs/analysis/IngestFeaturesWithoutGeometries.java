package org.matsim.contribs.analysis;

import lombok.extern.slf4j.Slf4j;
import org.geotools.data.FeatureWriter;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.testcases.MatsimTestUtils;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
public class IngestFeaturesWithoutGeometries {

    @Rule
    public MatsimTestUtils testUtils = new MatsimTestUtils();

    @Test
    public void test() throws IOException {

        var storeRoot = testUtils.getOutputDirectory() + "store";
        var store = new GeomesaFileSystemStore(storeRoot);

        try (var writer = store.getBla()) {

            var toWrite = writer.next();

            toWrite.setAttribute(GeomesaFileSystemStore.GeometryLessSchema.ANY_FIELD, "test");
            writer.write();

            var other = writer.next();
            other.setAttribute(GeomesaFileSystemStore.GeometryLessSchema.ANY_FIELD, "blup");

            writer.write();

        }

        var ff = new FilterFactoryImpl();
        var idFilter = ff.id(Set.of(new FeatureIdImpl("1")));
        try (var reader = store.getBlaReader(idFilter)) {

            while (reader.hasNext()) {
                var feature = reader.next();
                log.info("feature is: " + feature.toString());
            }

        }

    }

    private void writeLeg(FeatureWriter<SimpleFeatureType, SimpleFeature> writer) {

        try {
            var toWrite = writer.next();
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.AGENT_ID, "id");

            // assume we have at least two values for time
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.ENTER_TIME, Date.from(Instant.ofEpochSecond(1)));
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.EXIT_TIME, Date.from(Instant.ofEpochSecond(2)));

            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.IS_TELEPORTED, true);
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.MODE, "bla-mode");
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.VEHICLE_ID, "bla-id");

            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.LINK_IDS, List.of("ids"));
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.TIMES, List.of(1.0, 2.0));
            toWrite.setAttribute(GeomesaFileSystemStore.TrajectorySchema.GEOMETRY, "LINESTRING (0 0,10 10)");

            writer.write();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
