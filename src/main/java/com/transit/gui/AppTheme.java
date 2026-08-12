package com.transit.gui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Shared look-and-feel setup, called once by every GUI entry point
 * (LoginFrame, DashboardPreview) so the styling is consistent
 * regardless of which screen launches first.
 */
final class AppTheme {

    private AppTheme() { }

    static void init() {
        FlatLightLaf.setup();

        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("CheckBox.arc", 4);
        UIManager.put("ProgressBar.arc", 8);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("Table.showHorizontalLines", false);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 0));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("OptionPane.buttonAreaBorder", javax.swing.BorderFactory.createEmptyBorder(8, 8, 4, 8));
    }
}
