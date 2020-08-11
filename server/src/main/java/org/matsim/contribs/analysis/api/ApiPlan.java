package org.matsim.contribs.analysis.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

interface ApiPlanElement {

}

@RequiredArgsConstructor
@Getter
public class ApiPlan {

    private final List<ApiPlanElement> elements = new ArrayList<>();

    public void addElement(ApiPlanElement element) {
        elements.add(element);
    }

    @RequiredArgsConstructor
    @Getter
    public static class ApiActivity implements ApiPlanElement {

        private final double startTime;
        private final double endTime;
        private final SimpleCoordinate coordinate;
        private final String type;
        private final String facilityId;
        private final String linkId;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ApiLeg implements ApiPlanElement {

        private final double startTime;
        private final double endTime;
        private final SimpleCoordinate fromCoordinate;
        private final SimpleCoordinate toCoordinate;
        private final String mode;
        private final String type;
    }
}
