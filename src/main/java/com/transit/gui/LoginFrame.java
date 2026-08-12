package com.transit.gui;

import com.transit.auth.AuthService;
import com.transit.exception.AccountLockedException;
import com.transit.exception.InvalidCredentialsException;
import com.transit.exception.InvalidRoleException;
import com.transit.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Swing entry point for the GUI front end. Wraps AuthService exactly
 * as ConsoleMenu does -- no business logic lives here, it's a thin
 * presentation layer over the same Facade the console menu uses.
 * Proof that the AuthService Facade pattern actually pays off: this
 * class never touches CredentialStore or UserFactory directly.
 */
public class LoginFrame extends JFrame {

    private static final Color NAVY = new Color(0x1F, 0x3B, 0x57);
    private static final Color TEAL = new Color(0x2C, 0x6E, 0x8F);
    private static final Color RUST = new Color(0xC1, 0x55, 0x3D);
    private static final Color SLATE = new Color(0x6B, 0x72, 0x80);

    private final AuthService authService = new AuthService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginFrame() {
        super("Transit \u2014 School Transport System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        JPanel card = buildCard();
        root.add(card);
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(36, 40, 36, 40));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logo mark: simple circle with checkmark, drawn rather than an image asset
        JLabel logo = new JLabel(new LogoIcon(56, NAVY));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);
        card.add(Box.createVerticalStrut(14));

        JLabel title = new JLabel("Transit");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel subtitle = new JLabel("School transport, simplified.");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setForeground(SLATE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(Box.createVerticalStrut(28));

        card.add(fieldLabel("Username"));
        usernameField = new JTextField();
        styleField(usernameField);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));

        card.add(fieldLabel("Password"));
        passwordField = new JPasswordField();
        styleField(passwordField);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(22));

        JButton loginButton = new JButton("Log In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginButton.setBackground(NAVY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(loginButton.getFont().deriveFont(Font.BOLD, 13f));
        loginButton.addActionListener(this::attemptLogin);
        card.add(loginButton);

        card.add(Box.createVerticalStrut(14));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(RUST);
        errorLabel.setFont(errorLabel.getFont().deriveFont(11f));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(errorLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel("<html><center>Demo accounts: admin1/admin123,<br>j.matthews/lea123, school1/school123,<br>parent1/parent123</center></html>");
        hint.setForeground(SLATE);
        hint.setFont(hint.getFont().deriveFont(10f));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hint);

        // Enter key submits from either field
        usernameField.addActionListener(this::attemptLogin);
        passwordField.addActionListener(this::attemptLogin);

        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        label.setForeground(new Color(0x28, 0x2E, 0x35));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 2, 4, 0));
        return label;
    }

    private void styleField(JTextField field) {
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setFont(field.getFont().deriveFont(13f));
    }

    private void attemptLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            errorLabel.setText(" ");
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame(user, authService).setVisible(true));

        } catch (InvalidCredentialsException ex) {
            errorLabel.setText(ex.getMessage() + " (" + authService.getRemainingAttempts(username) + " attempts left)");
        } catch (AccountLockedException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (InvalidRoleException ex) {
            errorLabel.setText("System error: " + ex.getMessage());
        }
    }

    /** Small drawn logo -- a filled circle with a checkmark, avoids needing an image asset file. */
    private static class LogoIcon implements Icon {
        private final int size;
        private final Color color;

        LogoIcon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, size, size);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + size / 2, cy = y + size / 2;
            g2.drawLine(cx - size / 5, cy, cx - size / 14, cy + size / 5);
            g2.drawLine(cx - size / 14, cy + size / 5, cx + size / 4, cy - size / 6);
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    public static void main(String[] args) {
        AppTheme.init();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
