package org.matsim.contribs.analysis.parsing;

import lombok.RequiredArgsConstructor;
import org.matsim.api.core.v01.events.*;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Stack;

@RequiredArgsConstructor
public class RawEventsReader extends MatsimXmlParser {

    private static final String EVENT = "event";
    private static final String TYPE = "type";
    private static final String TIME = "time";

    private final ActivityHandler activityHandler;
    private final MovementHandler movementHandler;

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {

        // the following assumes that all the event atts are present
        if (EVENT.equals(name)) {

            var type = atts.getValue(TYPE);
            var time = Double.parseDouble(atts.getValue(TIME));

            switch (type) {
                case ActivityStartEvent.EVENT_TYPE:
                    activityHandler.handleStart(time, atts);
                case ActivityEndEvent.EVENT_TYPE:
                    activityHandler.handleEnd(time, atts);
                case PersonDepartureEvent
                        .EVENT_TYPE:
                    movementHandler.handleDeparture(time, atts);
                case PersonArrivalEvent
                        .EVENT_TYPE:
                    movementHandler.handleArrival(time, atts);
                case PersonEntersVehicleEvent
                        .EVENT_TYPE:
                    movementHandler.handleEntersVehicle(time, atts);
                case LinkEnterEvent.EVENT_TYPE:
                    movementHandler.handleLinkEnter(time, atts);
                case LinkLeaveEvent.EVENT_TYPE:
                    movementHandler.handleLinkLeave(time, atts);
                case TransitDriverStartsEvent.EVENT_TYPE:
                    movementHandler.handleTransitDriverStarts(time, atts);


            }
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
        // don't need to do anything here, since everything is handled in startTag
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // ignore characters to prevent OutOfMemoryExceptions
        /* the events-file only contains empty tags with attributes,
         * but without the dtd or schema, all whitespace between tags is handled
         * by characters and added up by super.characters, consuming huge
         * amount of memory when large events-files are read in.
         */
    }
}
