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

package com.terraforged.core.util;

import com.terraforged.n2d.util.NoiseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.IntFunction;

public class RollingGrid<T> {

    private final int size;
    private final int half;
    private final T[] grid;
    private final Generator<T> generator;

    private int startX = 0;
    private int startZ = 0;

    public RollingGrid(int size, IntFunction<T[]> constructor, Generator<T> generator) {
        this.size = size;
        this.half = size / 2;
        this.generator = generator;
        this.grid = constructor.apply(size * size);
    }

    public Iterable<T> getIterator() {
        return Arrays.asList(grid);
    }

    public PosIterator iterator() {
        return PosIterator.area(startX, startZ, size, size);
    }

    public int getStartX() {
        return startX;
    }

    public int getStartZ() {
        return startZ;
    }

    public int getSize() {
        return size;
    }

    public void setCenter(int x, int z) {
        setCenter(x - half, z - half, true);
    }

    public void setCenter(int x, int z, boolean update) {
        setPos(x - half, z - half, update);
    }

    public void setPos(int x, int z) {
        setPos(x, z, true);
    }

    public void setPos(int x, int z, boolean update) {
        if (update) {
            int deltaX = x - startX;
            int deltaZ = z - startZ;
            move(deltaX, deltaZ);
        } else {
            startX = x;
            startZ = z;
        }
    }

    public void move(int x, int z) {
        if (x != 0) {
            int minX = x < 0 ? startX + x : startX + size - x + 1;
            int maxX = minX + Math.abs(x);
            for (int px = minX; px < maxX; px++) {
                int dx = wrap(px);
                for (int dz = 0; dz < size; dz++) {
                    int index = index(dx, wrap(startZ + dz));
                    grid[index] = generator.generate(px, startZ + dz);
                }
            }
        }

        if (z != 0) {
            int minZ = z < 0 ? startZ + z : startZ + size - z + 1;
            int maxZ = minZ + Math.abs(z);
            for (int pz = minZ; pz < maxZ; pz++) {
                int dz = wrap(pz);
                for (int dx = 0; dx < size; dx++) {
                    int index = index(wrap(startX + dx), dz);
                    grid[index] = generator.generate(startX + dx, pz);
                }
            }
        }

        this.startX += x;
        this.startZ += z;
    }

    public T get(int x, int z) {
        x += startX;
        z += startZ;
        int mx = wrap(x);
        int mz = wrap(z);
        return grid[index(mx, mz)];
    }

    public void set(int x, int z, T value) {
        x += startX;
        z += startZ;
        int mx = wrap(x);
        int mz = wrap(z);
        grid[index(mx, mz)] = value;
    }

    private int index(int x, int z) {
        return z * size + x;
    }

    private int wrap(int value) {
        return ((value % size) + size) % size;
    }

    public interface Generator<T> {

        T generate(int x, int z);
    }

    public static void main(String[] args) {
        RollingGrid<Chunk> grid = createGrid(32);

        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(render(0, 0, grid)));
        label.setFocusable(true);
        label.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case 'w':
                        grid.move(0, -1);
                        label.setIcon(new ImageIcon(render(0, 0, grid)));
                        label.repaint();
                        break;
                    case 'a':
                        grid.move(-1, 0);
                        label.setIcon(new ImageIcon(render(0, 0, grid)));
                        label.repaint();
                        break;
                    case 's':
                        grid.move(0, 1);
                        label.setIcon(new ImageIcon(render(0, 0, grid)));
                        label.repaint();
                        break;
                    case 'd':
                        grid.move(1, 0);
                        label.setIcon(new ImageIcon(render(0, 0, grid)));
                        label.repaint();
                        break;
                }
            }
        });

        JFrame frame = new JFrame();
        frame.add(label);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static RollingGrid<Chunk> createGrid(int size) {
        RollingGrid<Chunk> grid = new RollingGrid<>(size, Chunk[]::new, Chunk::new);
        PosIterator iterator = PosIterator.area(0, 0, size, size);
        while (iterator.next()) {
            int x = iterator.x();
            int z = iterator.z();
            grid.set(x, z, new Chunk(x, z));
        }
        return grid;
    }

    private static BufferedImage render(int x, int z, RollingGrid<Chunk> grid) {
        int size = grid.size << 4;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        PosIterator chunkIterator = PosIterator.area(0, 0, grid.size, grid.size);
        while (chunkIterator.next()) {
            int chunkX = x + chunkIterator.x();
            int chunkZ = z + chunkIterator.z();
            Chunk chunk = grid.get(chunkX, chunkZ);
            if (chunk == null) {
                continue;
            }

            PosIterator pixel = PosIterator.area(chunkIterator.x() << 4, chunkIterator.z() << 4, 16, 16);
            while (pixel.next()) {
                image.setRGB(pixel.x(), pixel.z(), chunk.color.getRGB());
            }
        }
        return image;
    }

    private static class Chunk {

        private final Color color;

        public Chunk() {
            color = Color.BLACK;
        }

        public Chunk(int x, int z) {
            color = new Color(NoiseUtil.hash(x, z));
        }
    }
}
