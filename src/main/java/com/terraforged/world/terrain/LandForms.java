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

package com.terraforged.world.terrain;

import com.terraforged.core.settings.TerrainSettings;
import com.terraforged.core.Seed;
import com.terraforged.world.heightmap.Levels;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.EdgeFunc;

public class LandForms {

    private static final int PLAINS_H = 250;
    private static final double PLAINS_V = 0.24;

    public static final int PLATEAU_H = 500;
    public static final double PLATEAU_V = 0.475;

    private static final int HILLS_H = 500;
    private static final double HILLS1_V = 0.6;
    private static final double HILLS2_V = 0.55;

    private static final int MOUNTAINS_H = 410;
    private static final double MOUNTAINS_V = 0.7;

    private static final int MOUNTAINS2_H = 400;
    private static final double MOUNTAINS2_V = 0.645;

    private final TerrainSettings settings;
    private final float terrainHorizontalScale;
    private final float terrainVerticalScale;
    private final float groundLevel;
    private final float seaLevel;

    public LandForms(TerrainSettings settings, Levels levels) {
        this.settings = settings;
        terrainHorizontalScale = settings.general.globalHorizontalScale;
        terrainVerticalScale = settings.general.globalVerticalScale;
        groundLevel = levels.ground;
        seaLevel = levels.water;
    }

    public Module deepOcean(int seed) {
        Module hills = Source.perlin(++seed, 150, 3)
                .scale(seaLevel * 0.7)
                .bias(Source.perlin(++seed, 200, 1).scale(seaLevel * 0.2F));

        Module canyons = Source.perlin(++seed, 150, 4)
                .powCurve(0.2)
                .invert()
                .scale(seaLevel * 0.7)
                .bias(Source.perlin(++seed, 170, 1).scale(seaLevel * 0.15F));

        return Source.perlin(++seed, 500, 1)
                .blend(hills, canyons, 0.6, 0.65)
                .warp(++seed, 50, 2, 50);
    }

    public Module steppe(Seed seed) {
        int scaleH = Math.round(PLAINS_H * terrainHorizontalScale * settings.steppe.horizontalScale);

        double erosionAmount = 0.45;

        Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75).perlin().alpha(erosionAmount);
        Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3).perlin();
        Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3).perlin();
        Module module = Source.perlin(seed.next(), scaleH, 1)
                .mult(erosion)
                .warp(warpX, warpY, Source.constant(scaleH / 4F))
                .warp(seed.next(), 256, 1, 200);

        return settings.steppe.apply(groundLevel, 0.125 * terrainVerticalScale, module);
    }

    public Module plains(Seed seed) {
        int scaleH = Math.round(PLAINS_H * terrainHorizontalScale * settings.plains.horizontalScale);

        double erosionAmount = 0.45;

        Module erosion = Source.build(seed.next(), scaleH * 2, 3).lacunarity(3.75).perlin().alpha(erosionAmount);
        Module warpX = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5).perlin();
        Module warpY = Source.build(seed.next(), scaleH / 4, 3).lacunarity(3.5).perlin();

        Module module = Source.perlin(seed.next(), scaleH, 1)
                .mult(erosion)
                .warp(warpX, warpY, Source.constant(scaleH / 4F))
                .warp(seed.next(), 256, 1, 256);

        return settings.plains.apply(groundLevel, PLAINS_V * terrainVerticalScale, module);
    }

    public Module plateau(Seed seed) {
        Module valley = Source.ridge(seed.next(), 500, 1).invert()
                .warp(seed.next(), 100, 1, 150)
                .warp(seed.next(), 20, 1, 15);

        Module top = Source.build(seed.next(), 150, 3).lacunarity(2.45).ridge()
                .warp(seed.next(), 300, 1, 150)
                .warp(seed.next(), 40, 2, 20)
                .scale(0.15)
                .mult(valley.clamp(0.02, 0.1).map(0, 1));

        Module surface = Source.perlin(seed.next(), 20, 3).scale(0.05)
                .warp(seed.next(), 40, 2, 20);

        return settings.plateau.apply(groundLevel, PLATEAU_V * terrainVerticalScale,
                valley
                        .mult(Source.cubic(seed.next(), 500, 1).scale(0.6).bias(0.3))
                        .add(top)
                        .terrace(
                                Source.perlin(seed.next(), 20, 1).scale(0.3).bias(0.2),
                                Source.perlin(seed.next(), 20, 2).scale(0.1).bias(0.2),
                                4,
                                0.4
                        )
                        .add(surface));
    }

    public Module hills1(Seed seed) {
        return settings.hills.apply(groundLevel, HILLS1_V * terrainVerticalScale, Source.perlin(seed.next(), 200, 3)
                .mult(Source.billow(seed.next(), 400, 3).alpha(0.5))
                .warp(seed.next(), 30, 3, 20)
                .warp(seed.next(), 400, 3, 200));
    }

    public Module hills2(Seed seed) {
        return settings.hills.apply(groundLevel, HILLS2_V * terrainVerticalScale, Source.cubic(seed.next(), 128, 2)
                .mult(Source.perlin(seed.next(), 32, 4).alpha(0.075))
                .warp(seed.next(), 30, 3, 20)
                .warp(seed.next(), 400, 3, 200)
                .mult(Source.ridge(seed.next(), 512, 2).alpha(0.8)));
    }

    public Module dales(Seed seed) {
        Module hills1 = Source.build(seed.next(), 300, 4).gain(0.8).lacunarity(4).billow().powCurve(0.5).scale(0.75);
        Module hills2 = Source.build(seed.next(), 350, 3).gain(0.8).lacunarity(4).billow().pow(1.25);
        Module combined = Source.perlin(seed.next(), 400, 1).clamp(0.3, 0.6).map(0, 1).blend(
                hills1,
                hills2,
                0.4,
                0.75
        );
        Module hills = combined
                .pow(1.125)
                .warp(seed.next(), 300, 1, 100);

        return settings.dales.apply(groundLevel, 0.4, hills);
    }

    public Module mountains(Seed seed) {
        int scaleH = Math.round(MOUNTAINS_H * terrainHorizontalScale * settings.mountains.horizontalScale);

        Module module = Source.build(seed.next(), scaleH, 4).gain(1.15).lacunarity(2.35).ridge()
                .mult(Source.perlin(seed.next(), 24, 4).alpha(0.075))
                .warp(seed.next(), 350, 1, 150);

        return settings.mountains.apply(groundLevel, MOUNTAINS_V * terrainVerticalScale, module);
    }

    public Module mountains2(Seed seed) {
        double scale = MOUNTAINS2_V * terrainVerticalScale;
        Module cell = Source.cellEdge(seed.next(), 360, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0, 1)
                .warp(seed.next(), 200, 2, 100);
        Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
        Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
        Module mountains = cell.clamp(0, 1).mult(blur).mult(surface).pow(1.1);
        return settings.mountains.apply(groundLevel, scale, mountains);
    }

    public Module mountains3(Seed seed) {
        double scale = MOUNTAINS2_V * terrainVerticalScale;

        Module cell = Source.cellEdge(seed.next(), MOUNTAINS2_H, EdgeFunc.DISTANCE_2).scale(1.2).clamp(0, 1)
                .warp(seed.next(), 200, 2, 100);
        Module blur = Source.perlin(seed.next(), 10, 1).alpha(0.025);
        Module surface = Source.ridge(seed.next(), 125, 4).alpha(0.37);
        Module mountains = cell.clamp(0, 1).mult(blur).mult(surface).pow(1.1);

        Module terraced = mountains.terrace(
                Source.perlin(seed.next(), 50, 1).scale(0.5),
                Source.perlin(seed.next(), 100, 1).clamp(0.5, 0.95).map(0, 1),
                Source.constant(0.45),
                0.2F,
                0.45F,
                24,
                1
        );

        return settings.mountains.apply(groundLevel, scale, terraced);
    }

    public Module badlands(Seed seed) {
        Module mask = Source.perlin(seed.next(), 800, 1).clamp(0.35, 0.65).map(0, 1);
        Module hills = Source.ridge(seed.next(), 500, 4)
                .warp(seed.next(), 400, 2, 100)
                .mult(mask);

        double modulation = 0.4;
        double alpha = 1 - modulation;
        Module mod1 = hills.warp(seed.next(), 100, 1, 50).scale(modulation);

        Module lowFreq = hills.steps(4).scale(alpha).add(mod1);
        Module highFreq = hills.steps(10).scale(alpha).add(mod1);
        Module detail = lowFreq.add(highFreq);

        Module mod2 = hills.mult(Source.perlin(seed.next(), 400, 3).scale(modulation));
        Module shape = hills.terrace(0.1, 1, 4, 0.01)
                .scale(alpha)
                .add(mod2)
                .scale(alpha);

        return settings.badlands.apply(groundLevel, 0.7, shape.mult(detail.alpha(0.5)));
    }

    public Module torridonian(Seed seed) {
        Module plains = Source.perlin(seed.next(), 100, 3)
                .warp(seed.next(), 300, 1, 150)
                .warp(seed.next(), 20, 1, 40)
                .scale(0.15);

        Module hills = Source.perlin(seed.next(), 150, 4)
                .warp(seed.next(), 300, 1, 200)
                .warp(seed.next(), 20, 2, 20)
                .boost();

        Module test = Source.perlin(seed.next(), 200, 3)
                .blend(plains, hills, 0.6, 0.6)
                .terrace(
                        Source.perlin(seed.next(), 120, 1).scale(0.25),
                        Source.perlin(seed.next(), 200, 1).scale(0.5).bias(0.5),
                        Source.constant(0.5),
                        0,
                        0.3,
                        6,
                        1
                ).boost();

        return settings.torridonian.apply(groundLevel, 0.5, test);
    }
}
