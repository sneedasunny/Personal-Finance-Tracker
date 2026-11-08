import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private DBHelper db;
    private int userId;
    private String username;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel netIncomeLabel;
    private PieChartPanel piePanel;

    public MainFrame(DBHelper db, int userId, String username) {
        this.db = db;
        this.userId = userId;
        this.username = username;
        setTitle("Finance Tracker - " + username);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel(new BorderLayout());
        add(top, BorderLayout.NORTH);

        JLabel head = new JLabel("Hello, " + username + " — Personal Finance Tracker");
        head.setFont(new Font("SansSerif", Font.BOLD, 16));
        top.add(head, BorderLayout.WEST);

        JPanel controls = new JPanel();
        JButton addIncome = new JButton("Add Income");
        JButton addExpense = new JButton("Add Expense");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        controls.add(addIncome);
        controls.add(addExpense);
        controls.add(deleteBtn);
        controls.add(refreshBtn);
        top.add(controls, BorderLayout.EAST);

        addIncome.addActionListener(e -> openAddDialog("income"));
        addExpense.addActionListener(e -> openAddDialog("expense"));
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> refreshData());

        // Center split: left table, right pie chart & totals
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);

        // Table
        String[] cols = { "ID", "Date", "Type", "Amount", "Category", "Note" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0)); // hide ID column
        JScrollPane scr = new JScrollPane(table);
        split.setLeftComponent(scr);

        // Right panel
        JPanel right = new JPanel(new BorderLayout(6, 6));
        split.setRightComponent(right);

        piePanel = new PieChartPanel();
        piePanel.setPreferredSize(new Dimension(300, 300));
        right.add(piePanel, BorderLayout.CENTER);

        netIncomeLabel = new JLabel("Net: ₹0.00", JLabel.CENTER);
        netIncomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        right.add(netIncomeLabel, BorderLayout.SOUTH);

        // Row double-click to edit? (not implemented) but show details on double click
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = table.convertRowIndexToModel(row);
                        int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
                        showDetails(id);
                    }
                }
            }
        });
    }

    private void openAddDialog(String type) {
        AddTransactionDialog d = new AddTransactionDialog(this, type, db, userId);
        d.setVisible(true);
        if (d.saved) {
            refreshData();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a transaction row first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected transaction?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (db.deleteTransaction(id)) {
                JOptionPane.showMessageDialog(this, "Deleted.");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Delete failed.");
            }
        }
    }

    private void showDetails(int id) {
        // fetch transactions from model list
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Integer.parseInt(tableModel.getValueAt(i, 0).toString()) == id) {
                String date = tableModel.getValueAt(i, 1).toString();
                String type = tableModel.getValueAt(i, 2).toString();
                String amount = tableModel.getValueAt(i, 3).toString();
                String cat = tableModel.getValueAt(i, 4).toString();
                String note = tableModel.getValueAt(i, 5).toString();
                JOptionPane.showMessageDialog(this,
                        "Date: " + date + "\nType: " + type + "\nAmount: " + amount + "\nCategory: " + cat + "\nNote: "
                                + note,
                        "Transaction details", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
    }

    public void refreshData() {
        List<Transaction> list = db.getTransactionsForUser(userId);
        tableModel.setRowCount(0);
        for (Transaction t : list) {
            tableModel.addRow(new Object[] {
                    t.id,
                    t.date,
                    t.type,
                    String.format("%.2f", t.amount),
                    (t.category == null ? "" : t.category),
                    (t.note == null ? "" : t.note)
            });
        }
        double totalIncome = db.getTotalByType(userId, "income");
        double totalExpense = db.getTotalByType(userId, "expense");
        double net = totalIncome - totalExpense;
        netIncomeLabel.setText(String.format(
                "<html><div style='text-align:center'>Income: ₹%.2f<br>Expense: ₹%.2f<br><b>Net: ₹%.2f</b></div></html>",
                totalIncome, totalExpense, net));

        // Update pie chart with expense breakdown
        Map<String, Double> breakdown = db.getExpenseBreakdown(userId);
        piePanel.setData(breakdown);
    }
}
