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

package com.terraforged.world.continent.generator;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.WorldSettings;
import com.terraforged.n2d.util.Vec2i;
import com.terraforged.world.continent.MutableVeci;

public class SingleContinentGenerator extends ContinentGenerator {

    private final Vec2i center;

    public SingleContinentGenerator(Seed seed, WorldSettings settings) {
        super(seed, settings);
        MutableVeci pos = new MutableVeci();
        getNearestCenter(0, 0, pos);
        this.center = new Vec2i(pos.x, pos.z);
    }

    @Override
    public void apply(Cell cell, final float x, final float y) {
        super.apply(cell, x, y);

        // reset if outside of the world's only continent
        if (cell.continentX != center.x || cell.continentZ != center.y) {
            cell.continentIdentity = 0F;
            cell.continentEdge = 0F;
            cell.continentX = 0;
            cell.continentZ = 0;
        }
    }
}
