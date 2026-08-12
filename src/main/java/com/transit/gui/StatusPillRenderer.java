package com.transit.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Renders the "Status" column as a colored pill badge (green for
 * Active, red for Full) instead of plain text, matching the visual
 * language of the stat card accent colors.
 */
class StatusPillRenderer extends DefaultTableCellRenderer {

    private static final Color ACTIVE_BG = new Color(0x3E, 0x9C, 0x8F);
    private static final Color FULL_BG = new Color(0xC1, 0x55, 0x3D);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        JLabel label = new JLabel(String.valueOf(value));
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        boolean full = "FULL".equals(value);
        label.setBackground(full ? FULL_BG : ACTIVE_BG);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(true);
        wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        wrapper.add(roundedPill(label, full ? FULL_BG : ACTIVE_BG));
        return wrapper;
    }

    private JComponent roundedPill(JLabel label, Color bg) {
        JPanel pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.add(label, BorderLayout.CENTER);
        return pill;
    }
}
