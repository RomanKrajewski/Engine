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

import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Visualizer {

    public static void main(String[] args) {
        int size = 512;
        Module noise = Source.simplex(123, 40, 2).warp(Source.RAND, 124, 2, 1, 4);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

        PosIterator iterator = PosIterator.area(0, 0, size, size);
        while (iterator.next()) {
            float value = noise.getValue(iterator.x(), iterator.z());
            image.setRGB(iterator.x(), iterator.z(), getMaterial(value));
        }

        JFrame frame = new JFrame();
        frame.add(new JLabel(new ImageIcon(image)));
        frame.setVisible(true);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static int getMaterial(float value) {
        if (value > 0.6) {
            if (value < 0.75) {
                return Color.HSBtoRGB(0.05F, 0.4F, 0.2F);
            }
            return Color.HSBtoRGB(0.05F, 0.4F, 0.4F);
        }
        return Color.HSBtoRGB(0.25F, 0.4F, 0.6F);
    }
}
