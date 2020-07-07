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

import com.terraforged.n2d.source.Line;
import com.terraforged.n2d.util.Vec2f;
import com.terraforged.n2d.util.Vec2i;

public class RiverBounds {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public RiverBounds(int x1, int y1, int x2, int y2) {
        this(x1, y1, x2, y2, 0);
    }

    public RiverBounds(int x1, int y1, int x2, int y2, int radius) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public float length() {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public boolean intersects(RiverBounds other) {
        return Line.intersect(other.x1, other.y1, other.x2, other.y2, x1, y1, x2, y2);
    }

    public boolean overlaps(RiverBounds other) {
        return overlaps(other.minX, other.minY, other.maxX, other.maxY);
    }

    public boolean overlaps(float minX, float minY, float maxX, float maxY) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY;
    }

    public boolean overlaps(Vec2f center, float radius) {
        float minX = center.x - radius;
        float maxX = center.x + radius;
        float minY = center.y - radius;
        float maxY = center.y + radius;
        return overlaps(minX, minY, maxX, maxY);
    }

    public Vec2f pos(float distance) {
        int dx = x2() - x1();
        int dy = y2() - y1();
        return new Vec2f(x1() + dx * distance, y1() + dy * distance);
    }

    public static RiverBounds fromNodes(Vec2i p1, Vec2i p2) {
        return new RiverBounds(p1.x, p1.y, p2.x, p2.y, 300);
    }
}
