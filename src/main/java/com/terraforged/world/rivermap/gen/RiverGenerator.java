package com.terraforged.world.rivermap.gen;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.util.Variance;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;
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
import com.terraforged.world.rivermap.wetland.Wetland;
import com.terraforged.world.rivermap.wetland.WetlandConfig;
import com.terraforged.world.terrain.Terrains;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class RiverGenerator {

    //random valley width for main rivers
    private static final Variance MAIN_VALLEY = Variance.of(0.8, 0.7);

    // random valley width for a river fork
    private static final Variance FORK_VALLEY = Variance.of(0.4, 0.75);

    // random angle between a river and it's fork
    private static final Variance FORK_ANGLE = Variance.of(0.05, 0.075);

    // random spacing between forks along a given river
    private static final Variance MAIN_SPACING = Variance.of(0.05, 0.2);

    // used to randomly produce ravines inland
    private static final float CONTINENT_MODIFIER_MIN = 0.025F;
    private static final float CONTINENT_MODIFIER_RANGE = 0.1F;

    private static final AtomicInteger listSize = new AtomicInteger(32);

    private final int count;
    private final int mainValleyWidth;
    private final int forkValleyWidth;

    private final Seed seed;
    private final LakeConfig lake;
    private final RiverConfig main;
    private final RiverConfig fork;
    private final WetlandConfig wetland;
    private final Terrains terrain;
    private final Heightmap heightmap;
    private final Levels levels;

    public RiverGenerator(Heightmap heightmap, GeneratorContext context) {
        this.heightmap = heightmap;
        this.levels = context.levels;

        seed = context.seed.nextSeed();

        count = context.settings.rivers.riverCount;

        mainValleyWidth = context.settings.rivers.mainRivers.valleyWidth;
        forkValleyWidth = context.settings.rivers.branchRivers.valleyWidth;

        main = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.mainRivers.minBankHeight, context.settings.rivers.mainRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.mainRivers.bankWidth)
                .bedWidth(context.settings.rivers.mainRivers.bedWidth)
                .bedDepth(context.settings.rivers.mainRivers.bedDepth)
                .fade(context.settings.rivers.mainRivers.fade)
                .length(5000)
                .main(true)
                .order(0)
                .build();

        fork = RiverConfig.builder(context.levels)
                .bankHeight(context.settings.rivers.branchRivers.minBankHeight, context.settings.rivers.branchRivers.maxBankHeight)
                .bankWidth(context.settings.rivers.branchRivers.bankWidth)
                .bedWidth(context.settings.rivers.branchRivers.bedWidth)
                .bedDepth(context.settings.rivers.branchRivers.bedDepth)
                .fade(context.settings.rivers.branchRivers.fade)
                .length(4500)
                .order(1)
                .build();

        wetland = new WetlandConfig(context.settings.rivers.wetlands);

        lake = LakeConfig.of(context.settings.rivers.lakes, context.levels);

        terrain = context.terrain;
    }

    public Rivermap compute(int x, int z, long id) {
        Random random = new Random(id);

        GenWarp warp = new GenWarp((int) id);
        int size = listSize.get();

        List<Lake> lakes = new ArrayList<>(size);
        List<Wetland> wetland = new ArrayList<>(size);
        List<RiverBounds> riverPath = riverPath(heightmap, x, z);
        List<River> rivers = new ArrayList<>();
        if(riverPath != null){
            for (RiverBounds riverBounds : riverPath) {
            River.Settings settings = createSettings(random);
            settings.fadeIn = main.fade;
            settings.valleySize = mainValleyWidth;
            rivers.add(new River(riverBounds, main, settings, terrain, levels));
            }
        }

        return new Rivermap(x, z, warp, rivers, lakes, wetland);
    }


    private static River.Settings createSettings(Random random) {
        River.Settings settings = new River.Settings();
        settings.valleyCurve = River.getValleyType(random);
        settings.continentRiverModifier = CONTINENT_MODIFIER_MIN * random.nextFloat();
        settings.continentValleyModifier = settings.continentRiverModifier + (CONTINENT_MODIFIER_RANGE * random.nextFloat());
        settings.connecting = true;
        return settings;
    }

    private class Adjacency {
        public int from;
        public int to;
        public float weight;

        public Adjacency(int from, int to, float weight) {
            this.from = from;
            this.to = to;
            this.weight = weight > 0 ? weight * 20 : weight;
        }
    }


    public List<RiverBounds> riverPath(Heightmap heightmap,int x, int z) {
        MutableVeci pos = new MutableVeci(x,z);
        int topBorder = z - (int) heightmap.getContinent().getDistanceToEdge(x,z,0f,-1f, pos);
        int bottomBorder = z + (int) heightmap.getContinent().getDistanceToEdge(x,z,0f,1f, pos);
        int leftBorder = x - (int) heightmap.getContinent().getDistanceToEdge(x,z,-1f,0f, pos);
        int rightBorder = x + (int) heightmap.getContinent().getDistanceToEdge(x,z,1f,0f, pos);
        int TILESIZE = 100;
        int heightmapHeight = (bottomBorder - topBorder) /TILESIZE;
        int heightmapWidth = (rightBorder - leftBorder) /TILESIZE;
        if(heightmapHeight == 0 || heightmapWidth == 0){
            return null;
        }
        float[] terrainHeights = new float[heightmapWidth * heightmapHeight];
        Map<Integer, List<Adjacency>> adjacencyLists = new HashMap<>();
        int highestTile = 0;
        float maxHeightTile = Float.MIN_VALUE;
        float minHeightTile = Float.MAX_VALUE;
        for (int i = 0; i < heightmapWidth; i ++ ) {
            for (int j = 0; j < heightmapHeight; j ++) {
                try (Resource<Cell> cell = Cell.pooled()) {
                    heightmap.applyBase(cell.get(), leftBorder + i*TILESIZE, topBorder + j*TILESIZE);
                    float value = cell.get().value;
//                float value = heightmap.getRoot().getValue(leftBorder + i*TILESIZE, topBorder + j*TILESIZE);
                    terrainHeights[twoDtoOneD(i, j, heightmapWidth)] = value;
                    if (value > maxHeightTile) {
                        maxHeightTile = value;
                        highestTile = twoDtoOneD(i, j, heightmapWidth);
                    }
                    if(value < minHeightTile){
                        minHeightTile = value;
                    }
                }
            }
        }

        final float[] maxWeight = {0};

        for (int i = 0; i < heightmapWidth; i++) {
            for (int j = 0; j < heightmapHeight; j++) {
                if (i + 1 < heightmapWidth) {
                    int left = twoDtoOneD(i, j, heightmapWidth);
                    int right = twoDtoOneD(i + 1, j, heightmapWidth);
                    createAdjecencies(terrainHeights, adjacencyLists, maxWeight, left, right);
                }
                if (j + 1 < heightmapHeight) {
                    int top = twoDtoOneD(i, j, heightmapWidth);
                    int bottom = twoDtoOneD(i, j + 1, heightmapWidth);
                    createAdjecencies(terrainHeights, adjacencyLists, maxWeight, top, bottom);
                }
            }
        }
        float[] fScores = new float[heightmapHeight * heightmapWidth];
        float[] gScores = new float[heightmapHeight * heightmapWidth];

        for (int i = 0; i < fScores.length; i++) {
            fScores[i] = Float.POSITIVE_INFINITY;
            gScores[i] = Float.POSITIVE_INFINITY;
        }

        int startnode = highestTile;
        final Map<Integer, Integer> cameFrom = new HashMap<>();
        Function<Integer, List<RiverBounds>> reconstructPath = (endNode) -> {
            ArrayList<RiverBounds> path = new ArrayList<>();
            int current = endNode;
            int last;
            while (cameFrom.containsKey(current)) {
                last = current;
                current = cameFrom.get(current);
                int[] xAndYCurrent = oneDtoTwoD(current, heightmapWidth);
                int[] xAndYLast = oneDtoTwoD(last, heightmapWidth);
                path.add(new RiverBounds(xAndYLast[0] * TILESIZE + leftBorder, xAndYLast[1] * TILESIZE + topBorder, xAndYCurrent[0] * TILESIZE + leftBorder, xAndYCurrent[1] * TILESIZE + topBorder, Math.min(terrainHeights[current], terrainHeights[last])));
            }
            return path;
        };

        gScores[startnode] = 0;
        fScores[startnode] = terrainHeights[startnode];

        PriorityQueue<Integer> openList = new PriorityQueue<>(Comparator.comparing(e -> fScores[e]));
        openList.add(startnode);

        while (!openList.isEmpty()) {
            int current = openList.poll();
            if (terrainHeights[current] <= maxHeightTile * 0.2 ) {
                return reconstructPath.apply(current);
            }
            if (!adjacencyLists.containsKey(current)) {
                continue;
            }
            for (Adjacency neighbor : adjacencyLists.get(current)) {
                float tentativeGScore = gScores[current] + maxWeight[0] + neighbor.weight;
                if (tentativeGScore < gScores[neighbor.to]) {
                    cameFrom.put(neighbor.to, current);
                    gScores[neighbor.to] = tentativeGScore;
                    fScores[neighbor.to] = tentativeGScore + 0f; //replace 0f with heuristic
                    openList.remove(neighbor.to);
                    openList.add(neighbor.to);
                }
            }
        }
        return null;
    }

    private void createAdjecencies(float[] heightmap, Map<Integer, List<Adjacency>> adjacencyLists, float[] maxWeight, int nodeA, int nodeB) {
        float weightRight = heightmap[nodeB] - heightmap[nodeA];
        if (Math.abs(weightRight) > maxWeight[0]) {
            maxWeight[0] = Math.abs(weightRight);
        }
        adjacencyLists.putIfAbsent(nodeA, new ArrayList<>());
        adjacencyLists.get(nodeA).add(new Adjacency(nodeA, nodeB, weightRight));
        adjacencyLists.putIfAbsent(nodeB, new ArrayList<>());
        adjacencyLists.get(nodeB).add(new Adjacency(nodeB, nodeA, -weightRight));

    }

    private int twoDtoOneD(int x, int y, int length) {
        return y * length + x;
    }

    private int[] oneDtoTwoD(int tile, int length) {
        int y = tile / length;
        int x = tile % length;
        return new int[]{x, y};
    }
}
