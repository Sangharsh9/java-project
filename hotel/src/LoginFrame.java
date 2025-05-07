import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;

    public LoginFrame() {
        setTitle("Hotel Management Login");
        setSize(600, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create the background panel with the image
        BackgroundPanel background = new BackgroundPanel("b1.png");
        background.setLayout(null); // Use absolute positioning

        // Username Label and Field
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        background.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(100, 20, 150, 25);
        background.add(usernameField);

        // Password Label and Field
        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 60, 80, 25);
        background.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 60, 150, 25);
        background.add(passwordField);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setBounds(100, 100, 100, 25);
        background.add(loginButton);

        // Sign Up Button
        signupButton = new JButton("Sign Up");
        signupButton.setBounds(100, 140, 100, 25);
        background.add(signupButton);

        // Set the content pane to the background panel
        setContentPane(background);

        // Add action listeners
        loginButton.addActionListener(e -> login());
        signupButton.addActionListener(e -> signup());

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");
                JOptionPane.showMessageDialog(this, "Login successful as " + role);
                dispose();
                if (role.equals("owner")) {
                    new OwnerDashboard();
                } else {
                    new CustomerDashboard(userId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error!");
        }
    }

    private void signup() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password to register!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String checkSql = "SELECT * FROM users WHERE username=?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists! Try another.");
                return;
            }

            String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Sign Up successful! You can now login.");
            usernameField.setText("");
            passwordField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error during Sign Up!");
        }
    }
}
