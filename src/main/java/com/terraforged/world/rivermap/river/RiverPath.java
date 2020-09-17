package com.terraforged.world.rivermap.river;

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.source.Line;
import com.terraforged.n2d.util.NoiseUtil;

import java.util.ArrayList;
import java.util.List;

public class RiverPath {

    private List<RiverLine> path;
    private List<Float> heights;

    public RiverPath(List<Integer> x, List<Integer> z, List<Float> heights, float radius2){
        path = new ArrayList<>();
        this.heights = heights;
        for (int i = 1; i < x.size();i++) {
            path.add(new RiverLine(x.get(i-1), z.get(i-1), x.get(i), z.get(i), radius2));
        }
    }

    public float[] getValues(float x, float y) {
        float[] maxValAndAccordingT = {0.0f, 0.0f};
        int minDistIndex = 0;
        for (int i = 0; i < path.size(); i++) {
            float[] distAndT = path.get(i).getAlphaAndT(x,y);
            if(distAndT != null && distAndT[0] > maxValAndAccordingT[0]){
                maxValAndAccordingT = distAndT;
                minDistIndex = i;
            }
        }
        if(maxValAndAccordingT[0] == 0.0f){
            return new float[] {0.0f, 0.0f, 0.0f, 0.0f};
        }
        return new float[] {maxValAndAccordingT[0],
                heights.get(minDistIndex),
                heights.get(Math.min(minDistIndex+1, heights.size())),
                maxValAndAccordingT[1]};
    }


    public float maxValue() {
        return 1f;
    }

    public float minValue() {
        return 0f;
    }
}
