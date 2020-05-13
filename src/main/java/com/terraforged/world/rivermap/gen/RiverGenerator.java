package com.terraforged.world.rivermap.gen;

import com.terraforged.core.util.Variance;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.rivermap.Rivermap;
import com.terraforged.world.rivermap.lake.Lake;
import com.terraforged.world.rivermap.lake.LakeConfig;
import com.terraforged.world.rivermap.river.River;
import com.terraforged.world.rivermap.river.RiverBounds;
import com.terraforged.world.rivermap.river.RiverConfig;
import com.terraforged.world.terrain.Terrains;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RiverGenerator {

    //random valley width for main rivers
    private static final Variance MAIN_VALLEY = Variance.of(0.8, 0.7);

    // random valley width for a river fork
    private static final Variance FORK_VALLEY = Variance.of(0.4, 0.75);

    // random distance from continent center to start of a main river
    private static final Variance MAIN_START = Variance.of(0.1, 0.2);

    // random distance along a river before forks can generate
    private static final Variance FORK_START = Variance.of(0.1, 0.3);

    // random angle between a river and it's fork
    private static final Variance FORK_ANGLE = Variance.of(0.05, 0.1);

    // random spacing between forks along a given river
    private static final Variance MAIN_SPACING = Variance.of(0.05, 0.2);

    private static final Variance FORK_SPACING = Variance.of(0.1, 0.3);

    // random distance from continent center to position of a lake
    private static final Variance LAKE_POSITION = Variance.of(0.5, 0.4);

    private final int count;
    private final float length;
    private final LakeConfig lake;
    private final RiverConfig primary;
    private final RiverConfig secondary;
    private final RiverConfig tertiary;
    private final Terrains terrain;
    private final Heightmap heightmap;
    private final Levels levels;

    public RiverGenerator(Heightmap heightmap, GeneratorContext context) {
        this.heightmap = heightmap;
        this.levels = context.levels;

        count = context.settings.rivers.riverCount;

        length = context.settings.world.continent.continentScale;

        primary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.primaryRivers.minBankHeight, context.settings.rivers.primaryRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.primaryRivers.bankWidth)
                .bedWidth(context.settings.rivers.primaryRivers.bedWidth)
                .bedDepth(context.settings.rivers.primaryRivers.bedDepth)
                .fade(context.settings.rivers.primaryRivers.fade)
                .length(5000)
                .main(true)
                .order(0)
                .build();

        secondary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.secondaryRiver.minBankHeight, context.settings.rivers.secondaryRiver.maxBankHeight)
                .bankWidth(context.settings.rivers.secondaryRiver.bankWidth)
                .bedWidth(context.settings.rivers.secondaryRiver.bedWidth)
                .bedDepth(context.settings.rivers.secondaryRiver.bedDepth)
                .fade(context.settings.rivers.secondaryRiver.fade)
                .length(4500)
                .order(1)
                .build();

        tertiary = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.tertiaryRivers.minBankHeight, context.settings.rivers.tertiaryRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.tertiaryRivers.bankWidth)
                .bedWidth(context.settings.rivers.tertiaryRivers.bedWidth)
                .bedDepth(context.settings.rivers.tertiaryRivers.bedDepth)
                .fade(context.settings.rivers.tertiaryRivers.fade)
                .length(2500)
                .order(2)
                .build();

        lake = LakeConfig.of(context.settings.rivers.lake, context.levels);

        terrain = context.terrain;
    }

    public Rivermap compute(int x, int z, long id) {
        Random random = new Random(id);
        List<Lake> lakes = new LinkedList<>();
        List<River> rivers = new LinkedList<>();
        List<GenRiver> rootRivers = generateRoots(x, z, random, rivers, lakes);
        Collections.shuffle(rootRivers, random);
        for (GenRiver root : rootRivers) {
            generateForks(root.river, root.angle, MAIN_SPACING, secondary, random, rivers, lakes);
        }
        generateAdditionalLakes(x, z, random, rootRivers, rivers, lakes);
        rivers.sort(Collections.reverseOrder());
        return new Rivermap(x, z, (int) id, rivers, lakes);
    }

    private List<GenRiver> generateRoots(int x, int z, Random random, List<River> rivers, List<Lake> lakes) {
        MutableVeci pos = new MutableVeci(x, z);
        float start = random.nextFloat();
        float spacing = NoiseUtil.PI2 / count;
        float spaceVar = spacing * 0.75F;
        float spaceBias = -spaceVar / 2F;

        List<GenRiver> roots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // variance randomizes the spacing of the 'spokes' so that rivers aren't perfectly spaced
            // angle is just one spoke on the wheel
            float variance = random.nextFloat() * spaceVar + spaceBias;
            float angle = start + spacing * i + variance;
            // direction vector
            float dx = NoiseUtil.sin(angle);
            float dz = NoiseUtil.cos(angle);

            // varies distance from continent center that the river starts
            float startMod = 0.1F + random.nextFloat() * 0.35F;
            float length = heightmap.getContinent().getDistanceToOcean(x, z, dx, dz, pos);
            float startDist = Math.max(500, startMod * length);

            // start and end points of the river
            float x1 = x + dx * startDist;
            float z1 = z + dz * startDist;
            float x2 = x + dx * (length + 250);
            float z2 = z + dz * (length + 250);

            // varies the valley width
            float valleyWidth = River.VALLEY_WIDTH * MAIN_VALLEY.next(random);
            RiverBounds bounds = new RiverBounds((int) x1, (int) z1, (int) x2, (int) z2);

            River.Settings settings = new River.Settings();
            settings.fadeIn = primary.fade;
            settings.valleySize = valleyWidth;
            settings.valleyCurve = River.getValleyType(random);
            settings.continentRiverModifier = 0.15F * random.nextFloat();
            settings.continentValleyModifier = settings.continentRiverModifier + (0.4F * random.nextFloat());

            River river = new River(bounds, primary, settings, terrain);
            roots.add(new GenRiver(river, angle, dx, dz, length));
            rivers.add(river);

            addLake(river, random, lakes);
        }
        return roots;
    }

    private void generateForks(River parent, float parentAngle, Variance spacing, RiverConfig config, Random random, List<River> rivers, List<Lake> lakes) {
        int direction = random.nextBoolean() ? 1 : -1;
        for (float offset = 0.3F; offset < 0.8F; offset += spacing.next(random)) {
            for (boolean attempt = true; attempt; attempt = false) {
                direction = -direction;
                float change = direction * NoiseUtil.PI2 * FORK_ANGLE.next(random);
                float angle = parentAngle + change;
                float dx = NoiseUtil.sin(angle);
                float dz = NoiseUtil.cos(angle);
                float length = config.length * offset * 0.75F;

                Vec2f v1 = parent.bounds.pos(offset);

                float x2 = v1.x - dx * length;
                float z2 = v1.y - dz * length;
                float forkWidth = parent.config.bankWidth * offset * 0.75F;
                float valleyWidth = River.VALLEY_WIDTH * FORK_VALLEY.next(random);

                RiverConfig forkConfig = config.createFork(forkWidth);

                RiverBounds bounds = new RiverBounds((int) x2, (int) z2, (int) v1.x, (int) v1.y);

                River.Settings settings = new River.Settings();
                settings.connecting = true;
                settings.fadeIn = config.fade;
                settings.valleySize = valleyWidth;
                settings.valleyCurve = River.getValleyType(random);
                settings.continentRiverModifier = 0.1F * random.nextFloat();
                settings.continentValleyModifier = settings.continentRiverModifier + (0.3F * random.nextFloat());

                River fork = new River(bounds, forkConfig, settings, terrain);

                if (riverOverlaps(fork, parent, rivers)) {
                    continue;
                }

                rivers.add(fork);
            }
        }
        addLake(parent, random, lakes);
    }

    private void generateAdditionalLakes(int x, int z, Random random, List<GenRiver> roots, List<River> rivers, List<Lake> lakes) {
        Collections.sort(roots);

        float size = 175;
        Variance sizeVariance = Variance.of(1F, 0.25F);
        Variance angleVariance = Variance.of(1.99F, 0.02F);
        Variance distanceVariance = Variance.of(0.6F, 0.3F);
        for (int i = 0; i + 1 < roots.size(); i++) {
            GenRiver a = roots.get(i);
            GenRiver b = roots.get(i + 1);
            float angle = (a.angle + b.angle) / angleVariance.next(random);
            float dx = NoiseUtil.sin(angle);
            float dz = NoiseUtil.cos(angle);
            float distance = distanceVariance.next(random);
            float lx = x + dx * a.length * distance;
            float lz = z + dz * a.length * distance;
            float variance = sizeVariance.next(random);
            Vec2f center = new Vec2f(lx, lz);
            if (lakeOverlaps(center, size, rivers)) {
                continue;
            }
            lakes.add(new Lake(center, size, variance, lake, terrain));
        }
    }

    private void addLake(River river, Random random, List<Lake> lakes) {
        if (random.nextFloat() <= lake.chance) {
            float lakeSize = lake.sizeMin + random.nextFloat() * lake.sizeMax;
            Vec2f center = river.bounds.pos(0);
            lakes.add(new Lake(center, lakeSize, 1, lake, terrain));
        }
    }

    private boolean riverOverlaps(River river, River parent, List<River> rivers) {
        for (River other : rivers) {
            if (other != parent && other.bounds.overlaps(river.bounds) && other.bounds.intersects(river.bounds)) {
                return true;
            }
        }
        return false;
    }

    private boolean lakeOverlaps(Vec2f lake, float size, List<River> rivers) {
        for (River other : rivers) {
            if (!other.main && other.bounds.overlaps(lake, size)) {
                return true;
            }
        }
        return false;
    }
}
