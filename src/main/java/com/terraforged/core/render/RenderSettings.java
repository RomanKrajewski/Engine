package com.terraforged.core.render;

import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;

public class RenderSettings {

    public int width;
    public int height;
    public int resolution;
    public float zoom = 1F;
    public final Levels levels;
    public final Terrains terrain;
    public RenderMode renderMode = RenderMode.BIOME_TYPE;

    public RenderSettings(GeneratorContext context) {
        this.levels = context.levels;
        this.terrain = context.terrain;
    }
}
