/*
 *   
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

package com.terraforged.world.climate;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.continent.Continent;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.source.Rand;

public class Climate {

    private final float seaLevel;
    private final float lowerHeight;
    private final float midHeight = 0.45F;
    private final float upperHeight = 0.75F;

    private final float moistureModifier = 0.1F;
    private final float temperatureModifier = 0.05F;

    private final Rand rand;
    private final Module treeLine;
    private final Module offsetX;
    private final Module offsetY;

    private final ClimateModule biomeNoise;

    public Climate(Continent continent, GeneratorContext context) {
        this.biomeNoise = new ClimateModule(continent, context);

        this.treeLine = Source.perlin(context.seed.next(), context.settings.climate.biomeShape.biomeSize * 8, 1)
                .scale(context.levels.scale(50)) // 50 units worth of variance
                .bias(context.levels.water(40)) // start at-least 40 units above ground level
                .clamp(0, 1);

        this.rand = new Rand(Source.builder().seed(context.seed.next()));
        this.offsetX = context.settings.climate.biomeEdgeShape.build(context.seed.next());
        this.offsetY = context.settings.climate.biomeEdgeShape.build(context.seed.next());
        this.seaLevel = context.levels.water;
        this.lowerHeight = context.levels.ground;
    }

    public Rand getRand() {
        return rand;
    }

    public float getOffsetX(float x, float z, int distance) {
        return offsetX.getValue(x, z) * distance;
    }

    public float getOffsetZ(float x, float z, int distance) {
        return offsetY.getValue(x, z) * distance;
    }

    public float getTreeLine(float x, float z) {
        return treeLine.getValue(x, z);
    }

    public void apply(Cell cell, float x, float z) {
        apply(cell, x, z, false, true);
    }

    public void apply(Cell cell, float x, float z, boolean maskOnly, boolean mask) {
        biomeNoise.apply(cell, x, z, maskOnly, mask);
        modifyTemp(cell, x, z);
    }

    private void modifyTemp(Cell cell, float x, float z) {
        float height = cell.value;
        if (height > upperHeight) {
            cell.temperature = Math.max(0, cell.temperature - temperatureModifier);
            return;
        }

        // temperature decreases away from 'midHeight' towards 'upperHeight'
        if (height > midHeight) {
            float delta = (height - midHeight) / (upperHeight - midHeight);
            cell.temperature = Math.max(0, cell.temperature - (delta * temperatureModifier));
            return;
        }

        height = Math.max(lowerHeight, height);

        // temperature increases away from 'midHeight' towards 'lowerHeight'
        if (height >= lowerHeight) {
            float delta = 1 - ((height - lowerHeight) / (midHeight - lowerHeight));
            cell.temperature = Math.min(1, cell.temperature + (delta * temperatureModifier));
        }
    }

    private void modifyMoisture(Cell cell, float x, float z) {

    }
}
