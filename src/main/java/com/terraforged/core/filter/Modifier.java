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

package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.util.NoiseUtil;

public interface Modifier {

    float getValueModifier(float value);

    default float modify(Cell cell, float value) {
        float strengthModifier = 1F;

        // reduce erosion strength towards the edge of terrain regions (that have an erosion modifier)
        if (cell.terrain.erosionModifier() != 1F) {
            float alpha = NoiseUtil.map(cell.terrainRegionEdge, 0F, 0.15F, 0.15F);
            strengthModifier = NoiseUtil.lerp(1F, cell.terrain.erosionModifier(), alpha);
        }

        // reduce erosion strength approaching rivers to prevent the bed getting filled up with sediment
        if (cell.riverMask < 0.1F) {
            strengthModifier *= NoiseUtil.map(cell.riverMask, 0.002F, 0.1F, 0.098F);
        }

        return getValueModifier(cell.value) * strengthModifier * value;
    }

    default Modifier invert() {
        return v -> 1 - getValueModifier(v);
    }

    static Modifier range(float minValue, float maxValue) {
        return new Modifier() {

            private final float min = minValue;
            private final float max = maxValue;
            private final float range = maxValue - minValue;

            @Override
            public float getValueModifier(float value) {
                if (value > max) {
                    return 1F;
                }
                if (value < min) {
                    return 0F;
                }
                return (value - min) / range;
            }
        };
    }
}
