package org.matsim.contribs.analysis;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;

public abstract class Transformation {

    public static final MathTransform TRANSFORM = createTransformation();

    private static MathTransform createTransformation() {

        try {
            // this will yield lon/lat instead of the default lat/lon which would result in x and y- coordinates being swapped
            // don't know what's the correct way, but doing it like this because I can wrap my head around it better this way.
            var sourceCRS = CRS.decode("EPSG:4326", true);
            var targetCRS = CRS.decode("EPSG:3857");

            return CRS.findMathTransform(sourceCRS, targetCRS);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }
}
