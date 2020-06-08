package com.terraforged.core.util;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Visualizer {

    public static void main(String[] args) {
        int size = 512;
        Module noise = Source.perlin(123, 200, 1).steps(5, 0.45, 0.6F);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        PosIterator iterator = PosIterator.area(0, 0, size, 1);
        while (iterator.next()) {
            float value = noise.getValue(iterator.x(), iterator.z());
            int height = NoiseUtil.round((image.getHeight() - 1) * value);
            for (int y = 0; y < height; y++) {
                image.setRGB(iterator.x(), image.getHeight() - 1 - y, 0xffffff);
            }
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
