package com.transit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Small hand-drawn icons for the dashboard stat cards. Drawn with
 * Graphics2D rather than using emoji glyphs or image assets, so
 * rendering is consistent across platforms/fonts.
 */
final class StatIcon implements Icon {

    enum Type { ROUTE, PEOPLE, BUS, BUILDING }

    private final Type type;
    private final Color color;
    private final int size;

    StatIcon(Type type, Color color, int size) {
        this.type = type;
        this.color = color;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setColor(color);

        switch (type) {
            case ROUTE -> paintRoute(g2);
            case PEOPLE -> paintPeople(g2);
            case BUS -> paintBus(g2);
            case BUILDING -> paintBuilding(g2);
        }
        g2.dispose();
    }

    private void paintRoute(Graphics2D g2) {
        g2.setStroke(new BasicStroke(size / 9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D path = new Path2D.Float();
        path.moveTo(size * 0.15, size * 0.85);
        path.curveTo(size * 0.15, size * 0.55, size * 0.55, size * 0.65, size * 0.55, size * 0.4);
        path.curveTo(size * 0.55, size * 0.2, size * 0.85, size * 0.25, size * 0.85, size * 0.15);
        g2.draw(path);
        g2.fillOval((int) (size * 0.08), (int) (size * 0.78), (int) (size * 0.18), (int) (size * 0.18));
        g2.fillOval((int) (size * 0.75), (int) (size * 0.05), (int) (size * 0.2), (int) (size * 0.2));
    }

    private void paintPeople(Graphics2D g2) {
        int r = (int) (size * 0.22);
        g2.fillOval((int) (size * 0.08), (int) (size * 0.12), r, r);
        g2.fillArc((int) (size * 0.0), (int) (size * 0.42), (int) (size * 0.52), (int) (size * 0.5), 0, 180);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
        g2.fillOval((int) (size * 0.42), (int) (size * 0.2), r, r);
        g2.fillArc((int) (size * 0.36), (int) (size * 0.5), (int) (size * 0.52), (int) (size * 0.5), 0, 180);
    }

    private void paintBus(Graphics2D g2) {
        int bodyW = (int) (size * 0.9), bodyH = (int) (size * 0.55);
        int bx = (int) (size * 0.05), by = (int) (size * 0.15);
        g2.fillRoundRect(bx, by, bodyW, bodyH, size / 6, size / 6);
        g2.setColor(Color.WHITE);
        int winY = by + bodyH / 5;
        int winW = bodyW / 5;
        for (int i = 0; i < 3; i++) {
            g2.fillRoundRect(bx + size / 12 + i * (winW + size / 14), winY, winW, bodyH / 3, 3, 3);
        }
        g2.setColor(color);
        int wheelY = by + bodyH - size / 14;
        int wheelR = size / 6;
        g2.fillOval(bx + size / 10, wheelY, wheelR, wheelR);
        g2.fillOval(bx + bodyW - size / 10 - wheelR, wheelY, wheelR, wheelR);
    }

    private void paintBuilding(Graphics2D g2) {
        int w = (int) (size * 0.7), h = (int) (size * 0.85);
        int bx = (int) (size * 0.15), by = (int) (size * 0.08);
        g2.fillRoundRect(bx, by, w, h, 4, 4);
        g2.setColor(Color.WHITE);
        int cols = 2, rows = 3;
        int padX = w / 6, padY = h / 8;
        int cellW = (w - padX * (cols + 1)) / cols;
        int cellH = (h - padY * (rows + 1)) / rows;
        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int wx = bx + padX + col * (cellW + padX);
                int wy = by + padY + r * (cellH + padY);
                g2.fillRect(wx, wy, cellW, cellH);
            }
        }
    }

    @Override public int getIconWidth() { return size; }
    @Override public int getIconHeight() { return size; }
}
