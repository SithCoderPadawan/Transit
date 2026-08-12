package com.transit.gui;

import javax.swing.*;
import java.awt.*;

/** A JPanel with rounded corners and a subtle border, used for cards throughout the dashboard. */
class RoundedPanel extends JPanel {

    private final int arc;
    private final Color borderColor;

    RoundedPanel(int arc) {
        this(arc, new Color(0xE5, 0xE7, 0xEB));
    }

    RoundedPanel(int arc, Color borderColor) {
        this.arc = arc;
        this.borderColor = borderColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
