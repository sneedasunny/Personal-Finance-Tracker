import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.MessageDigest;

public class LoginFrame extends JFrame {
    private DBHelper db;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame(DBHelper db) {
        this.db = db;
        setTitle("Finance Tracker - Login");
        setSize(400, 260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        add(panel);

        JLabel title = new JLabel("Personal Finance Tracker", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2,2,8,8));
        fields.add(new JLabel("Username:"));
        usernameField = new JTextField();
        fields.add(usernameField);
        fields.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        fields.add(passwordField);
        panel.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        panel.add(buttons, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> onLogin());
        registerBtn.addActionListener(e -> onRegister());

        // Enter key triggers login
        passwordField.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }
        String hash = sha256(pass);
        Integer userId = db.loginUser(user, hash);
        if (userId != null) {
            SwingUtilities.invokeLater(() -> {
                MainFrame mf = new MainFrame(db, userId, user);
                mf.setVisible(true);
            });
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    private void onRegister() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password to register.");
            return;
        }
        String hash = sha256(pass);
        boolean ok = db.registerUser(user, hash);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Registered successfully. You can login now.");
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed (maybe username already exists).");
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) {
                sb.append(String.format("%02x", x));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
