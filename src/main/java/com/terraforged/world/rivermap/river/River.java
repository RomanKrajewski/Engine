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

package com.terraforged.world.rivermap.river;

import com.terraforged.core.cell.Cell;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.func.CurveFunc;
import com.terraforged.n2d.func.SCurve;
import com.terraforged.n2d.source.Line;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import com.terraforged.world.terrain.populator.TerrainPopulator;

import java.util.Random;

public class River extends TerrainPopulator implements Comparable<River> {

    public static final int VALLEY_WIDTH = 275;
    private static final float DEPTH_FADE_STRENGTH = 0.5F;
    private static final float MIN_WIDTH2 = 1.5F;

    public final boolean main;
    private final boolean connecting;

    private final float waterLine;
    private final float bedHeight;
    private final float extraBedDepth;
    private final float minBankHeight;
    private final float maxBankHeight;
    private final float bankAlphaMin;
    private final float bankAlphaMax;
    private final float bankAlphaRange;
    private final Module bankVariance;

    private final Line bed;
    private final Line banks;
    private final Line valley;
    private final CurveFunc valleyCurve;
    public final RiverConfig config;
    public final RiverBounds bounds;

    private final Terrains terrains;

    private final float depthFadeBias;
    private final float continentValleyModifier;
    private final float continentRiverModifier;

    public River(RiverBounds bounds, RiverConfig config, Settings settings, Terrains terrains, Levels levels) {
        super(terrains.river, Source.ZERO, Source.ZERO);
        Module in = Source.constant(settings.fadeIn);
        Module out = Source.constant(settings.fadeOut);
        Module bedWidth = Source.constant(config.bedWidth * config.bedWidth);
        Module bankWidth = Source.constant(config.bankWidth * config.bankWidth);
        Module valleyWidth = Source.constant(settings.valleySize * settings.valleySize);
        this.bounds = bounds;
        this.config = config;
        this.main = config.main;
        this.terrains = terrains;
        this.connecting = settings.connecting;
        this.waterLine = levels.water;
        this.bedHeight = config.bedHeight;
        this.extraBedDepth = levels.scale(1);
        this.minBankHeight = config.minBankHeight;
        this.maxBankHeight = config.maxBankHeight;
        this.valleyCurve = settings.valleyCurve;
        this.continentValleyModifier = settings.continentValleyModifier;
        this.continentRiverModifier = settings.continentRiverModifier;
        this.bankAlphaMin = minBankHeight;
        this.bankAlphaMax = Math.min(1, minBankHeight + 0.35F);
        this.bankAlphaRange = bankAlphaMax - bankAlphaMin;
        this.bankVariance = Source.perlin(1234, 150, 1);
        this.depthFadeBias = 1 - DEPTH_FADE_STRENGTH;
        this.bed = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), bedWidth, in, out, 0.1F);
        this.banks = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), bankWidth, in, out, 0.1F);
        this.valley = Source.line(bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(), valleyWidth, Source.ZERO, Source.ZERO, 0.33F);
    }

    @Override
    public int compareTo(River o) {
        return Integer.compare(config.order, o.config.order);
    }

    @Override
    public void apply(Cell cell, float x, float z) {
        if (cell.value <= bedHeight) {
            return;
        }

        float valleyAlpha = valley.getValue(x, z);
        if (valleyAlpha == 0) {
            return;
        }

        valleyAlpha = valleyCurve.apply(valleyAlpha);

        float continent = NoiseUtil.map(cell.continentEdge, 0.2F, 0.8F, 0.6F);
        float valleyMod = 1 - (continent * continentValleyModifier);

        // riverMask decreases the closer to the river the position gets
        cell.riverMask *= (1 - valleyAlpha);
        float bankHeight = getBankHeight(cell, x, z);
        if (!carveValley(cell, valleyAlpha * valleyMod, bankHeight)) {
            return;
        }

        // is a branching river and x,z is past the connecting point
        if (connecting && banks.clipEnd(x, z)) {
            return;
        }

        // width modifier widens the river the further from its start the coords are
        // mouth modifier widens the river even more the closer to the ocean the coords are
        float mouthModifier = getMouthModifier(cell);
        float widthModifier = banks.getWidthModifier(x, z);
        float banksAlpha = banks.getValue(x, z, MIN_WIDTH2, widthModifier / mouthModifier);
        if (banksAlpha == 0) {
            return;
        }

        // modifies the steepness of river banks the further inland the position is
        float riverMod = 1 - (continent * continentRiverModifier);
        float depthAlpha = NoiseUtil.clamp(depthFadeBias + (DEPTH_FADE_STRENGTH * widthModifier), 0, 1);
        float bedHeight = NoiseUtil.lerp(bankHeight, this.bedHeight, depthAlpha);
        if (!carveBanks(cell, banksAlpha * riverMod, bedHeight)) {
            return;
        }

        float bedAlpha = bed.getValue(x, z);
        if (bedAlpha == 0 || cell.value <= bedHeight) {
            return;
        }

        carveBed(cell, bedHeight, bedAlpha);
    }

    private float getBankHeight(Cell cell, float x, float z) {
        // scale bank height based on elevation of the terrain (higher terrain == taller banks)
        float bankHeightAlpha = NoiseUtil.map(cell.value, bankAlphaMin, bankAlphaMax, bankAlphaRange);
        // use perlin noise to add a little extra variance to the bank height
        float bankHeightVariance = bankVariance.getValue(x, z);
        // lerp between the min and max heights
        return NoiseUtil.lerp(minBankHeight, maxBankHeight, bankHeightAlpha * bankHeightVariance);
    }

    private float getBedHeight(float bankHeight, float depthAlpha) {
        // scale depth of river by with it's width (wider == deeper)
        // depthAlpha changes the river depth up ${DEPTH_FADE_STRENGTH} %
        return NoiseUtil.lerp(bankHeight, this.bedHeight, depthAlpha);
    }

    private boolean carveValley(Cell cell, float valleyAlpha, float bankHeight) {
        // lerp the position's height to the riverbank height
        if (cell.value > bankHeight) {
            cell.value = NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha);
        }
        return true;
    }

    private boolean carveBanks(Cell cell, float banksAlpha, float bedHeight) {
        // lerp the position's height to the riverbed height (ie the riverbank slopes)
        if (cell.value > bedHeight) {
            banksAlpha = NoiseUtil.clamp(banksAlpha, 0, 1);
            cell.value = NoiseUtil.lerp(cell.value, bedHeight, banksAlpha);
            // tag after lerping the cell height value
            tag(cell, bedHeight);
            return true;
        } else {
            tag(cell, bedHeight);
            return false;
        }
    }

    private void carveBed(Cell cell, float bedHeight, float bedAlpha) {
        cell.erosionMask = true;
        tag(cell, bedHeight);

        if (cell.value < bedHeight) {
            float extraBedHeight = NoiseUtil.lerp(bedHeight - extraBedDepth, bedHeight, bedAlpha);
            cell.value = getExtraBedHeight(cell.value, bedHeight, extraBedHeight);
        } else {
            cell.value = NoiseUtil.lerp(cell.value, bedHeight, bedAlpha);
        }
    }

    private void tag(Cell cell, float bedHeight) {
        // don't tag as river if the current one overrides it and the cell value is above water level
        // or below the riverbed height at this position
        if (cell.terrain.overridesRiver() && (cell.value < bedHeight || cell.value > waterLine)) {
            return;
        }

        cell.terrain = terrains.river;
    }

    private float getMouthModifier(Cell cell) {
        float modifier = NoiseUtil.map(cell.continentEdge, 0F, 0.5F, 0.5F);
        return modifier * modifier;
    }

    private static float getExtraBedHeight(float height, float bedHeight, float extraBedHeight) {
        if (height < extraBedHeight) {
            return extraBedHeight;
        }
        float alpha = (height - extraBedHeight) / (bedHeight - extraBedHeight);
        return NoiseUtil.lerp(extraBedHeight, bedHeight, alpha);
    }

    public static class Settings {

        public float valleySize = VALLEY_WIDTH;
        public double fadeIn = 0.7F;
        public double fadeOut = 0F;
        public boolean connecting = false;
        public float continentValleyModifier = 0F;
        public float continentRiverModifier = 0F;
        public CurveFunc valleyCurve = new SCurve(2, -0.5F);
    }

    public static CurveFunc getValleyType(Random random) {
        int value = random.nextInt(100);
        if (value < 5) {
            // 'collapsed valley'
            return new SCurve(0.4F, 1F);
        }

        if (value < 30) {
            // 'close' valley
            return new SCurve(4, 5);
        }

        if (value < 50) {
            // shallow 'close' valley
            return new SCurve(3, 0.25F);
        }

        // normal valley
        return new SCurve(2, -0.5F);
    }
}
