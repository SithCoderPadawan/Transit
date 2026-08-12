package com.transit.gui;

import com.transit.auth.AuthService;
import com.transit.model.LEAOfficer;
import com.transit.model.User;

import javax.swing.*;

public class DashboardPreview {
    public static void main(String[] args) throws Exception {
        AppTheme.init();
        User lea = new LEAOfficer("U-002", "Janet Matthews", "j.matthews@lea.gov.uk");
        AuthService authService = new AuthService();
        SwingUtilities.invokeLater(() -> new DashboardFrame(lea, authService).setVisible(true));
    }
}
