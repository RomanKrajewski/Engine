package com.terraforged.world.rivermap;

import com.terraforged.cereal.spec.Context;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.filter.Filter;
import com.terraforged.core.filter.Filterable;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.terrain.Terrain;


import java.util.*;
import java.util.function.Function;

public class RiverPostProcessor implements Filter {
    private static final int TILESIZE = 5;
    private static final float TILESIZESQUARE = (float) TILESIZE * TILESIZE;
    private int heightmapSize;
    GeneratorContext context;

    public RiverPostProcessor(GeneratorContext ctx) {
        this.context = ctx;
    }

    ;

    @Override
    public void apply(Filterable map, int seedX, int seedZ, int iterations) {
        List<Integer> riverPath = riverPath(map);
        if (riverPath == null) {
            return;
        }
        for (int tile :
                riverPath) {
            int[] xAndY = oneDtoTwoD(tile);
            for (int i = 0; i < TILESIZE; i++) {
                for (int j = 0; j < TILESIZE; j++) {
                    Cell cell = map.getCellRaw(xAndY[0] * TILESIZE + i, xAndY[1] * TILESIZE + j);
                    cell.waterLevel = cell.value * 0.98f;
                    cell.value *= 0.95f;
                    cell.terrain = context.terrain.river;
                }
            }
        }
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


    public List<Integer> riverPath(Filterable map) {
        heightmapSize = map.getSize().total / TILESIZE;
        float[] heightmap = new float[heightmapSize * heightmapSize];
        Map<Integer, List<Adjacency>> adjacencyLists = new HashMap<>();
        int highestTile = 0;
        float maxHeightTile = 0;
        for (int i = 0; i < heightmapSize; i += 1) {
            for (int j = 0; j < heightmapSize; j += 1) {
                float valSum = 0;
                for (int x = 0; x < TILESIZE; x++) {
                    for (int y = 0; y < TILESIZE; y++) {
                        valSum += map.getCellRaw(i * TILESIZE + x, j * TILESIZE + y).value;
                    }
                }
                float averageHeight = valSum / TILESIZESQUARE;
                heightmap[twoDtoOneD(i, j, heightmapSize)] = averageHeight;
                if (averageHeight > maxHeightTile) {
                    maxHeightTile = averageHeight;
                    highestTile = twoDtoOneD(i, j, heightmapSize);
                }
            }
        }

        final float[] maxWeight = {0};
        final Function<Integer, Integer> manHattenDistance = (node) -> {
            int[] xAndY = oneDtoTwoD(node);
            int distX = Math.min(xAndY[0], heightmapSize - xAndY[0]);
            int distY = Math.min(xAndY[1], heightmapSize - xAndY[1]);
            return Math.min(distX, distY);
        };
        final Function<Integer, Float> h = (node) -> manHattenDistance.apply(node) * 0f;

        for (int x = 0; x < heightmapSize; x++) {
            for (int y = 0; y < heightmapSize; y++) {
                if (x + 1 < heightmapSize) {
                    int left = twoDtoOneD(x, y, heightmapSize);
                    int right = twoDtoOneD(x + 1, y, heightmapSize);
                    createAdjecencies(heightmap, (Map<Integer, List<Adjacency>>) adjacencyLists, maxWeight, left, right);
                }
                if (y + 1 < heightmapSize) {
                    int top = twoDtoOneD(x, y, heightmapSize);
                    int bottom = twoDtoOneD(x, y + 1, heightmapSize);
                    createAdjecencies(heightmap, (Map<Integer, List<Adjacency>>) adjacencyLists, maxWeight, top, bottom);
                }
            }
        }
        float[] fScores = new float[heightmapSize * heightmapSize];
        float[] gScores = new float[heightmapSize * heightmapSize];

        for (int i = 0; i < fScores.length; i++) {
            fScores[i] = Float.POSITIVE_INFINITY;
            gScores[i] = Float.POSITIVE_INFINITY;
        }

        int startnode = highestTile;
        final Map<Integer, Integer> cameFrom = new HashMap<>();
        Function<Integer, List<Integer>> reconstructPath = (endNode) -> {
            ArrayList<Integer> path = new ArrayList<>();
            int current = endNode;
            path.add(current);
            while (cameFrom.containsKey(current)) {
                current = cameFrom.get(current);
                path.add(current);
            }
            return path;
        };

        gScores[startnode] = 0;
        fScores[startnode] = h.apply(startnode);

        PriorityQueue<Integer> openList = new PriorityQueue<>(Comparator.comparing(e -> fScores[e]));
        openList.add(startnode);

        while (!openList.isEmpty()) {
            int current = openList.poll();
            if (manHattenDistance.apply(current) == 0) {
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
                    fScores[neighbor.to] = tentativeGScore + h.apply(neighbor.to);
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

    private int[] oneDtoTwoD(int tile) {
        int y = tile / heightmapSize;
        int x = tile % heightmapSize;
        return new int[]{x, y};
    }
}
