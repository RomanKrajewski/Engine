package com.terraforged.world.rivermap.river;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.source.Line;

import java.util.ArrayList;
import java.util.List;

public class RiverPath {

    private List<Line> path;
    private List<Float> heights;

    public RiverPath(List<Integer> x, List<Integer> z, List<Float> heights, Module radius){
        path = new ArrayList<>();
        this.heights = heights;
        for (int i = 1; i < x.size();i++) {
            path.add(new Line(x.get(i-1), z.get(i-1), x.get(i), z.get(i), radius, Source.ZERO, Source.ZERO, 0.0f));
        }
    }

    public float[] getValues(float x, float y) {
        float maxVal = 0.0f;
        int minDistIndex = 0;
        for (int i = 0; i < path.size(); i++) {
            float dist = path.get(i).getValue(x,y);
            if(dist > maxVal){
                maxVal = dist;
                minDistIndex = i;
            }
        }
        return new float[] {maxVal, heights.get(minDistIndex)};
    }


    public float maxValue() {
        return 1f;
    }

    public float minValue() {
        return 0f;
    }
}
