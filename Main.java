import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Ensure SQLite JDBC driver is available
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "SQLite JDBC driver not found. Please add sqlite-jdbc jar to classpath.", "Driver missing",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            DBHelper db = new DBHelper();
            new LoginFrame(db).setVisible(true);
        });
    }
}
