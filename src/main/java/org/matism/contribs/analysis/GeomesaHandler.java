package org.matism.contribs.analysis;

import lombok.SneakyThrows;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureWriter;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;

public class GeomesaHandler implements LinkEnterEventHandler, LinkLeaveEventHandler {

    private final FeatureWriter<SimpleFeatureType, SimpleFeature> writer;
    private final Network network;
    private final CoordinateTransformation transformation;

    GeomesaHandler(FeatureWriter<SimpleFeatureType, SimpleFeature> writer, Network network, CoordinateTransformation transformation) {
        this.writer = writer;
        this.network = network;
        this.transformation = transformation;
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {


        var link = network.getLinks().get(linkEnterEvent.getLinkId());
        var coord = transformation.transform(link.getFromNode().getCoord());

        try {
            write(coord, linkEnterEvent.getTime(), LinkEnterEvent.EVENT_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {


        var link = network.getLinks().get(linkLeaveEvent.getLinkId());
        var coord = transformation.transform(link.getToNode().getCoord());

        try {
            write(coord, linkLeaveEvent.getTime(), LinkLeaveEvent.EVENT_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(Coord coord, double time, String type) throws IOException {

        var toWrite = writer.next();
        toWrite.setAttribute("geom", "POINT (" + coord.getX() + " " + coord.getY() + ")");
        toWrite.setAttribute("time", time);
        toWrite.setAttribute("type", type);
        writer.write();

    }
}
