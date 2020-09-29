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

    private static final float DEPTH_FADE_STRENGTH = 0.5F;
    private static final float MIN_WIDTH2 = 1.5F;

    public final boolean main;
    private final float extraBedDepth;
    private final Module bankVariance;

    private final CurveFunc valleyCurve;
    public final RiverConfig config;

    private final Terrains terrains;

    private final float continentValleyModifier;
    private final float continentRiverModifier;

    private final RiverPath valley;
    private final RiverPath banks;
    private final RiverPath bed;

    private final Levels levels;

    public River(RiverPath valley, RiverPath banks, RiverPath bed, RiverConfig config, Settings settings, Terrains terrains, Levels levels) {
        super(terrains.river, Source.ZERO, Source.ZERO);
        this.levels = levels;
        this.config = config;
        this.main = config.main;
        this.terrains = terrains;
        this.valley = valley;
        this.banks= banks;
        this.bed = bed;
        this.extraBedDepth = levels.scale(1);
        this.valleyCurve = settings.valleyCurve;
        this.continentValleyModifier = settings.continentValleyModifier;
        this.continentRiverModifier = settings.continentRiverModifier;
        this.bankVariance = Source.perlin(1234, 150, 1);
    }

    @Override
    public int compareTo(River o) {
        return Integer.compare(config.order, o.config.order);
    }

    @Override
    public void apply(Cell cell, float x, float z) {

        float [] alphaHeightSig = valley.getValues(x,z);
        float alpha = alphaHeightSig[0];

        float valleyAlpha = alpha;
        if (valleyAlpha == 0) {
            return;
        }

        float waterLevelA = alphaHeightSig[1];
        float waterLevelB = alphaHeightSig[2];
        float waterLevelT = alphaHeightSig[3];

        float heightDifference = Math.abs(waterLevelA - waterLevelB);
        float waterLevel;
        float flowingWaterLevel;
        float bedHeight;
        if(heightDifference != 0f){
            float sigHeight = NoiseUtil.curve(waterLevelT, 40);
            waterLevel = NoiseUtil.lerp(waterLevelA, waterLevelB,
                    sigHeight);
            bedHeight = waterLevel - levels.scale(config.bedDepth);
            flowingWaterLevel = getFlowingWaterLevel(waterLevel, waterLevelA, sigHeight, bedHeight);
        }else {
            waterLevel = waterLevelA;
            bedHeight = waterLevel - levels.scale(config.bedDepth);
            flowingWaterLevel = waterLevelA;
        }

        float minBankHeight = levels.scale(config.minBankHeight) + waterLevel;
        float maxBankHeight = levels.scale(config.maxBankHeight) + waterLevel;
        float bankAlphaMax = Math.min(1, minBankHeight + 0.35F);


        valleyAlpha = valleyCurve.apply(valleyAlpha);

        float continent = NoiseUtil.map(cell.continentEdge, 0.25F, 0.85F, 0.6F);
        float valleyMod = 1 - (continent * continentValleyModifier);

        // riverMask decreases the closer to the river the position gets
        cell.riverMask *= (1 - valleyAlpha);

        //higher terrain = taller banks
        float bankHeightAlpha = NoiseUtil.map(cell.value, minBankHeight, bankAlphaMax, bankAlphaMax - minBankHeight);
        // use perlin noise to add a little extra variance to the bank height
        float bankHeightVariance = bankVariance.getValue(x, z);
        // lerp between the min and max heights
        float bankHeight = NoiseUtil.lerp(minBankHeight, maxBankHeight, bankHeightAlpha * bankHeightVariance);


        // lerp the position's height to the riverbank height
        cell.value = NoiseUtil.lerp(cell.value, bankHeight, valleyAlpha * valleyMod);


        float banksAlpha = banks.getValues(x, z)[0];
        if (banksAlpha == 0) {
            return;
        }

        cell.terrain = terrains.river;

        float riverMod = 1 - (continent * continentRiverModifier);

        banksAlpha = NoiseUtil.clamp(banksAlpha, 0, 1);
        cell.value = NoiseUtil.lerp(cell.value, bedHeight, banksAlpha);


        setCellWaterLevel(cell, flowingWaterLevel);
    }

    private float getFlowingWaterLevel(float lerpedWaterLevel, float waterLevelA, float sigHeight, float bedHeight) {

        float waterLevelSigmoidDeriv = sigHeight* (1-sigHeight);
        float waterHeightFactor = (0.05f - waterLevelSigmoidDeriv)/0.05f;
        float returnValue = bedHeight + waterHeightFactor * (lerpedWaterLevel-bedHeight);
        return Math.max(returnValue, waterLevelA);

    }

    private void setCellWaterLevel(Cell cell, float waterLine) {
        if(cell.value < waterLine) {
            cell.waterLevel = waterLine;
        }
            cell.erosionMask = true;
        }



    private float getMouthModifier(Cell cell) {
        float modifier = NoiseUtil.map(cell.continentEdge, 0F, 0.5F, 0.5F);
        return modifier * modifier;
    }

    public static class Settings {

        public float valleySize = 150;
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
