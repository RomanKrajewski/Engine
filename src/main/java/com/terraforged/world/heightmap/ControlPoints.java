/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.world.heightmap;

import com.terraforged.core.settings.WorldSettings;

public class ControlPoints {

    public final float deepOcean;
    public final float shallowOcean;
    public final float beach;
    public final float coast;
    public final float coastMarker;
    public final float inland;

    public ControlPoints(WorldSettings.ControlPoints points) {
        if (!validate(points)) {
            points = new WorldSettings.ControlPoints();
        }

        this.inland = points.inland;
        this.coast = points.coast;
        this.beach = points.beach;
        this.shallowOcean = points.shallowOcean;
        this.deepOcean = points.deepOcean;
        this.coastMarker = coast + ((inland - coast) / 2F);
    }

    public static boolean validate(WorldSettings.ControlPoints points) {
        return points.inland <= 1
                && points.inland > points.coast
                && points.coast > points.beach
                && points.beach > points.shallowOcean
                && points.shallowOcean > points.deepOcean
                && points.deepOcean >= 0;
    }
}
