package com.terraforged.world.rivermap.river;

import com.terraforged.n2d.util.NoiseUtil;

public class RiverLine {

    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private final float dx;
    private final float dy;
    private final float length2;
    private float radius2;

    public RiverLine(float x1, float y1, float x2, float y2, float radius2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.dx = x2 - x1;
        this.dy = y2 - y1;
        this.radius2 = radius2;
        this.length2 = this.dx * this.dx + this.dy * this.dy;
    }

    public float[] getAlphaAndT(float x, float y) {
        float[] dist2AndT = this.getDistance2(x, y);
        if (dist2AndT[0] > this.radius2) {
            return null;
        } else {
            return new float[]{(1.0F - dist2AndT[0] / this.radius2), dist2AndT[1]} ;
        }
    }

    private float[] getDistance2(float x, float y) {
        float t = (x - this.x1) * this.dx + (y - this.y1) * this.dy;
        float s = NoiseUtil.clamp(t / this.length2, 0.0F, 1.0F);
        float ix = this.x1 + s * this.dx;
        float iy = this.y1 + s * this.dy;
        return new float[] {dist2(x, y, ix, iy), s};
    }

    public static float dist2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }
}
