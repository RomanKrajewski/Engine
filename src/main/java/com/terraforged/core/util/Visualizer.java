package com.terraforged.core.util;

import me.dags.noise.Module;
import me.dags.noise.Source;

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
