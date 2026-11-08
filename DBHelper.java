import java.sql.*;
import java.util.*;

public class DBHelper {
    private static final String DB_URL = "jdbc:sqlite:finance.db";

    public DBHelper() {
        createTablesIfNotExist();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTablesIfNotExist() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "password_hash TEXT NOT NULL"
                + ");";

        String createTrans = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "type TEXT NOT NULL, " // income / expense
                + "amount REAL NOT NULL, "
                + "category TEXT, "
                + "note TEXT, "
                + "date TEXT NOT NULL, "
                + "FOREIGN KEY(user_id) REFERENCES users(id)"
                + ");";

        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(createUsers);
            s.execute(createTrans);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // User operations
    public boolean registerUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash) VALUES (?, ?)";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, passwordHash);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            // username exists or other error
            return false;
        }
    }

    public Integer loginUser(String username, String passwordHash) {
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, passwordHash);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Transaction operations
    public boolean addTransaction(int userId, String type, double amount, String category, String note,
            String dateIso) {
        String sql = "INSERT INTO transactions(user_id, type, amount, category, note, date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, type);
            p.setDouble(3, amount);
            p.setString(4, category);
            p.setString(5, note);
            p.setString(6, dateIso);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaction(int transId) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, transId);
            p.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsForUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC, id DESC";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("note"),
                        rs.getString("date"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Aggregates for pie chart / totals
    public double getTotalByType(int userId, String type) {
        String sql = "SELECT SUM(amount) AS s FROM transactions WHERE user_id = ? AND type = ?";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            p.setString(2, type);
            ResultSet rs = p.executeQuery();
            return rs.next() ? rs.getDouble("s") : 0.0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public Map<String, Double> getExpenseBreakdown(int userId) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) AS s FROM transactions WHERE user_id = ? AND type = 'expense' GROUP BY category ORDER BY s DESC";
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, userId);
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                String cat = rs.getString("category");
                if (cat == null || cat.trim().isEmpty())
                    cat = "Uncategorized";
                map.put(cat, rs.getDouble("s"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
