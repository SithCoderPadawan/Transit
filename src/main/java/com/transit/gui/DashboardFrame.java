package com.transit.gui;

import com.transit.auth.AuthService;
import com.transit.data.TransportDataStore;
import com.transit.exception.*;
import com.transit.model.*;
import com.transit.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Main window after login. A thin presentation layer exactly like
 * ConsoleMenu -- every action here delegates to the same service
 * classes the console menu calls. No business logic is duplicated.
 */
public class DashboardFrame extends JFrame {

    private static final Color NAVY = new Color(0x1F, 0x3B, 0x57);
    private static final Color SIDEBAR = new Color(0x16, 0x26, 0x38);
    private static final Color SIDEBAR_ACTIVE = new Color(0x2C, 0x6E, 0x8F);
    private static final Color SIDEBAR_HOVER = new Color(0x22, 0x38, 0x50);
    private static final Color TEAL = new Color(0x2C, 0x6E, 0x8F);
    private static final Color GREEN = new Color(0x3E, 0x9C, 0x8F);
    private static final Color AMBER = new Color(0xE0, 0xA9, 0x4C);
    private static final Color RUST = new Color(0xC1, 0x55, 0x3D);
    private static final Color BG = new Color(0xF4, 0xF6, 0xF8);
    private static final Color SLATE = new Color(0x6B, 0x72, 0x80);
    private static final Color CARD_BG = Color.WHITE;

    private final User user;
    private final AuthService authService;
    private final TransportDataStore dataStore = new TransportDataStore();
    private final RouteManager routeManager = new RouteManager(dataStore);
    private final PupilService pupilService = new PupilService(dataStore);
    private final SchoolService schoolService = new SchoolService(dataStore);
    private final CsvImportService csvImportService = new CsvImportService(dataStore);
    private final ReportService reportService = new ReportService(dataStore);
    private final ContractService contractService = new ContractService(dataStore);

    private JPanel centerPanel;
    private JLabel headerTitle;
    private JLabel headerSubtitle;

    public DashboardFrame(User user, AuthService authService) {
        super("Transit \u2014 School Transport System [Logged in: " + user.getName() + " (" + user.getRoleLabel() + ")]");
        this.user = user;
        this.authService = authService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);

        showDashboardOverview();
    }

    // ------------------------------------------------------------------
    // Sidebar
    // ------------------------------------------------------------------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(22, 14, 20, 14));

        JLabel logo = new JLabel("\u25CF  Transit");
        logo.setForeground(Color.WHITE);
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 18f));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(4));
        JLabel tagline = new JLabel("School transport, simplified.");
        tagline.setForeground(new Color(0x6B, 0x82, 0x9C));
        tagline.setFont(tagline.getFont().deriveFont(9.5f));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(22));

        JPanel userChip = new JPanel();
        userChip.setLayout(new BoxLayout(userChip, BoxLayout.Y_AXIS));
        userChip.setBackground(new Color(0x20, 0x34, 0x4A));
        userChip.setAlignmentX(Component.LEFT_ALIGNMENT);
        userChip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        userChip.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel roleLabel = new JLabel(user.getRoleLabel().toUpperCase());
        roleLabel.setForeground(new Color(0x8F, 0xA5, 0xBC));
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD, 9f));
        userChip.add(roleLabel);
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(nameLabel.getFont().deriveFont(13f));
        userChip.add(nameLabel);
        sidebar.add(userChip);
        sidebar.add(Box.createVerticalStrut(18));

        sidebar.add(navButton("\u25A4", "Dashboard Overview", true, this::showDashboardOverview));

        if (user.hasPermission(Permission.MANAGE_ROUTES)) {
            sidebar.add(navButton("\u21C4", "Move Pupil to New Route", false, this::openMovePupilDialog));
        }
        if (user.hasPermission(Permission.EDIT_ANY_PUPIL_RECORDS) || user.hasPermission(Permission.EDIT_OWN_PUPIL_RECORDS)) {
            sidebar.add(navButton("\u270E", "Update Pupil Record", false, this::openUpdatePupilDialog));
        }
        if (user.hasPermission(Permission.VIEW_ROUTES)) {
            sidebar.add(navButton("\u2299", "View Route & Pickup Point", false, this::openViewRouteDialog));
        }
        if (user.hasPermission(Permission.CORRECT_ANY_DATA)) {
            sidebar.add(navButton("\u260E", "Enter School Staff Contact", false, this::openStaffContactDialog));
            sidebar.add(navButton("\u2913", "Import Pupil Data (CSV)", false, this::openCsvImportDialog));
        }
        if (user.hasPermission(Permission.MANAGE_CONTRACTS)) {
            sidebar.add(navButton("\u2696", "Manage Bus Contracts", false, this::openContractsDialog));
        }
        if (user.hasPermission(Permission.VIEW_REPORTS)) {
            sidebar.add(navButton("\u2637", "Generate Report", false, this::openReportDialog));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(separator());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(navButton("\u2715", "Log Out", false, this::logOut));

        return sidebar;
    }

    private JComponent separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2A, 0x40, 0x58));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JButton navButton(String glyph, String text, boolean active, Runnable action) {
        JButton button = new JButton("  " + glyph + "    " + text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(active ? SIDEBAR_ACTIVE : SIDEBAR);
        button.setForeground(active ? Color.WHITE : new Color(0xC8, 0xD2, 0xDC));
        button.setFont(button.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 11.5f));
        button.setBorder(new EmptyBorder(4, 4, 4, 4));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.putClientProperty("JButton.arc", 8);
        button.addActionListener(e -> action.run());
        button.getModel().addChangeListener(e -> {
            if (!active) {
                button.setBackground(button.getModel().isRollover() ? SIDEBAR_HOVER : SIDEBAR);
            }
        });
        return button;
    }

    // ------------------------------------------------------------------
    // Main area
    // ------------------------------------------------------------------

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel headerBlock = new JPanel();
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        headerBlock.setOpaque(false);

        headerTitle = new JLabel("Dashboard Overview");
        headerTitle.setFont(headerTitle.getFont().deriveFont(Font.BOLD, 23f));
        headerTitle.setForeground(NAVY);
        headerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBlock.add(headerTitle);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        headerSubtitle = new JLabel(dateStr);
        headerSubtitle.setFont(headerSubtitle.getFont().deriveFont(12f));
        headerSubtitle.setForeground(SLATE);
        headerSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerSubtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        headerBlock.add(headerSubtitle);

        main.add(headerBlock, BorderLayout.NORTH);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        main.add(centerPanel, BorderLayout.CENTER);

        return main;
    }

    private void showDashboardOverview() {
        headerTitle.setText("Dashboard Overview \u2014 " + user.getRoleLabel());
        centerPanel.removeAll();

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.add(buildStatCardsRow(), BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 16));
        middle.setOpaque(false);
        middle.add(buildRoutesTablePanel(), BorderLayout.CENTER);
        middle.add(buildAlertsPanel(), BorderLayout.SOUTH);
        content.add(middle, BorderLayout.CENTER);

        centerPanel.add(content, BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private JPanel buildStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 100));

        int totalRoutes = dataStore.getAllRoutes().size();
        int totalPupils = dataStore.getAllPupils().size();
        int totalBuses = dataStore.getAllContractors().values().stream()
                .mapToInt(c -> c.getBuses().size()).sum();
        int totalContractors = dataStore.getAllContractors().size();

        row.add(statCard("Total Routes", String.valueOf(totalRoutes), NAVY, StatIcon.Type.ROUTE));
        row.add(statCard("Pupils Transported", String.valueOf(totalPupils), TEAL, StatIcon.Type.PEOPLE));
        row.add(statCard("Vehicle Fleet", totalBuses + " Buses", AMBER, StatIcon.Type.BUS));
        row.add(statCard("Bus Contractors", String.valueOf(totalContractors), GREEN, StatIcon.Type.BUILDING));

        return row;
    }

    private JPanel statCard(String label, String value, Color accent, StatIcon.Type iconType) {
        RoundedPanel card = new RoundedPanel(14);
        card.setBackground(CARD_BG);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel iconLabel = new JLabel(new StatIcon(iconType, accent, 34));
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(true);
        iconWrap.setBackground(mixWithWhite(accent, 0.85f));
        iconWrap.setPreferredSize(new Dimension(54, 54));
        iconWrap.add(iconLabel);
        RoundedPanel iconRound = new RoundedPanel(27);
        iconRound.setBackground(mixWithWhite(accent, 0.85f));
        iconRound.setLayout(new GridBagLayout());
        iconRound.setPreferredSize(new Dimension(54, 54));
        iconRound.add(iconLabel);
        card.add(iconRound, BorderLayout.WEST);

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);
        JLabel labelText = new JLabel(label);
        labelText.setForeground(SLATE);
        labelText.setFont(labelText.getFont().deriveFont(11f));
        JLabel valueText = new JLabel(value);
        valueText.setForeground(NAVY);
        valueText.setFont(valueText.getFont().deriveFont(Font.BOLD, 22f));
        textBlock.add(Box.createVerticalGlue());
        textBlock.add(labelText);
        textBlock.add(valueText);
        textBlock.add(Box.createVerticalGlue());
        card.add(textBlock, BorderLayout.CENTER);

        return card;
    }

    private Color mixWithWhite(Color c, float whiteAmount) {
        int r = (int) (c.getRed() * (1 - whiteAmount) + 255 * whiteAmount);
        int g = (int) (c.getGreen() * (1 - whiteAmount) + 255 * whiteAmount);
        int b = (int) (c.getBlue() * (1 - whiteAmount) + 255 * whiteAmount);
        return new Color(r, g, b);
    }

    private JPanel buildRoutesTablePanel() {
        RoundedPanel panel = new RoundedPanel(14);
        panel.setLayout(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Active Routes Overview");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(NAVY);
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Route ID", "Pupils", "Capacity", "Bus", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (Route route : dataStore.getAllRoutes().values()) {
            int pupils = route.getPupils().size();
            int capacity = route.getCapacity();
            String bus = route.getBus() != null ? route.getBus().getRegPlate() : "Unassigned";
            String status = pupils >= capacity ? "FULL" : "Active";
            model.addRow(new Object[]{route.getRouteId(), pupils, capacity, bus, status});
        }

        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(table.getFont().deriveFont(12.5f));
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 11.5f));
        table.getTableHeader().setForeground(SLATE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(mixWithWhite(TEAL, 0.85f));
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusPillRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Computed from real data -- not placeholder alerts. Flags routes
     * at/over capacity and buses left unassigned, so this panel
     * actually reflects the current state of TransportDataStore.
     */
    private JPanel buildAlertsPanel() {
        RoundedPanel panel = new RoundedPanel(14);
        panel.setLayout(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));
        panel.setPreferredSize(new Dimension(0, 130));

        JLabel title = new JLabel("Alerts");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(NAVY);
        panel.add(title, BorderLayout.NORTH);

        List<String> alerts = new ArrayList<>();
        for (Route route : dataStore.getAllRoutes().values()) {
            if (route.getPupils().size() >= route.getCapacity()) {
                alerts.add("Route " + route.getRouteId() + " is at full capacity ("
                        + route.getPupils().size() + "/" + route.getCapacity() + ").");
            }
            if (route.getBus() == null) {
                alerts.add("Route " + route.getRouteId() + " has no bus assigned.");
            }
        }

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(8, 0, 0, 0));

        if (alerts.isEmpty()) {
            JLabel none = new JLabel("No alerts \u2014 all routes within capacity and assigned a bus.");
            none.setForeground(GREEN);
            none.setFont(none.getFont().deriveFont(12f));
            list.add(none);
        } else {
            for (String alert : alerts) {
                JLabel item = new JLabel("\u26A0  " + alert);
                item.setForeground(RUST);
                item.setFont(item.getFont().deriveFont(12f));
                item.setBorder(new EmptyBorder(3, 0, 3, 0));
                list.add(item);
            }
        }

        panel.add(list, BorderLayout.CENTER);
        return panel;
    }

    // ------------------------------------------------------------------
    // Action dialogs
    // ------------------------------------------------------------------

    private void openMovePupilDialog() {
        JTextField pupilIdField = new JTextField();
        JTextField routeIdField = new JTextField();
        Object[] form = {"Pupil ID:", pupilIdField, "New Route ID:", routeIdField};

        int result = JOptionPane.showConfirmDialog(this, form, "Move Pupil to New Route", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            routeManager.movePupilToRoute(pupilIdField.getText().trim(), routeIdField.getText().trim());
            info("Pupil reassigned successfully.");
            showDashboardOverview();
        } catch (RouteCapacityException e) {
            error(e.getMessage() + "\nChoose a different route or contact LEA.");
        } catch (InvalidRouteException | PupilNotFoundException e) {
            error(e.getMessage());
        }
    }

    private void openUpdatePupilDialog() {
        String pupilId = JOptionPane.showInputDialog(this, "Pupil ID:", "Update Pupil Record", JOptionPane.PLAIN_MESSAGE);
        if (pupilId == null || pupilId.isBlank()) return;

        Pupil current;
        try {
            current = pupilService.getPupil(pupilId.trim());
        } catch (PupilNotFoundException e) {
            error(e.getMessage());
            return;
        }

        JTextField nameField = new JTextField(current.getName());
        JTextField contactField = new JTextField(current.getEmergencyContact());
        Object[] form = {
                "Current version: " + current.getVersion(),
                "Name:", nameField,
                "Emergency contact:", contactField
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Update Pupil Record", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            pupilService.updatePupilRecord(user, pupilId.trim(),
                    new PupilDetails(nameField.getText().trim(), contactField.getText().trim()),
                    current.getVersion());
            info("Pupil record updated.");
            showDashboardOverview();
        } catch (UnauthorizedScopeException | PupilNotFoundException e) {
            error(e.getMessage());
        } catch (StaleDataException e) {
            error(e.getMessage() + "\nPlease reopen the record and retry.");
        }
    }

    private void openViewRouteDialog() {
        String pupilId = JOptionPane.showInputDialog(this, "Pupil ID:", "View Route & Pickup Point", JOptionPane.PLAIN_MESSAGE);
        if (pupilId == null || pupilId.isBlank()) return;

        try {
            Route route = routeManager.getRouteForPupil(pupilId.trim());
            StringBuilder sb = new StringBuilder();
            sb.append("Route ").append(route.getRouteId())
                    .append(" \u2014 ").append(route.getPupils().size()).append("/").append(route.getCapacity()).append(" pupils\n\n");
            for (Stop stop : route.getStops()) {
                sb.append("  \u2022 ").append(stop).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Route Details", JOptionPane.INFORMATION_MESSAGE);
        } catch (PupilNotFoundException | InvalidRouteException e) {
            error(e.getMessage());
        }
    }

    private void openStaffContactDialog() {
        JTextField schoolIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField roleField = new JTextField("Headmaster");
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        Object[] form = {
                "School ID:", schoolIdField, "Contact name:", nameField,
                "Role:", roleField, "Email:", emailField, "Phone:", phoneField
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Enter School Staff Contact", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            schoolService.addSchoolContact(schoolIdField.getText().trim(),
                    new SchoolContact(nameField.getText().trim(), roleField.getText().trim(),
                            emailField.getText().trim(), phoneField.getText().trim()));
            info("Contact saved.");
        } catch (InvalidSchoolException e) {
            error(e.getMessage());
        }
    }

    private void openCsvImportDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select pupil CSV file");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            int count = csvImportService.importPupilsFromCsv(file.getAbsolutePath());
            info("Imported " + count + " pupil record(s).");
            showDashboardOverview();
        } catch (IOException e) {
            error("Could not read file: " + e.getMessage());
        } catch (CsvFormatException | RouteCapacityException e) {
            error("Import stopped: " + e.getMessage());
        }
    }

    private void openContractsDialog() {
        StringBuilder listing = new StringBuilder();
        for (String line : contractService.listContractors()) {
            listing.append(line).append("\n");
        }

        JTextField companyField = new JTextField();
        JTextField ratingField = new JTextField();
        Object[] form = {
                new JLabel("<html><pre>" + listing + "</pre></html>"),
                "Company name to review:", companyField,
                "New rating (1-5):", ratingField
        };

        int result = JOptionPane.showConfirmDialog(this, form, "Manage Bus Contracts", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            int rating = Integer.parseInt(ratingField.getText().trim());
            contractService.reviewContractorPerformance(companyField.getText().trim(), rating);
            info("Performance rating updated.");
        } catch (NumberFormatException e) {
            error("Rating must be a number.");
        } catch (InvalidContractorException | InvalidRatingException e) {
            error(e.getMessage());
        }
    }

    private void openReportDialog() {
        List<String> lines = reportService.generateReport(user);
        JTextArea area = new JTextArea(String.join("\n", lines));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 400));
        JOptionPane.showMessageDialog(this, scroll, "Generate Report", JOptionPane.PLAIN_MESSAGE);
    }

    private void logOut() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
