import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class AddTransactionDialog extends JDialog {
    private DBHelper db;
    private int userId;
    public boolean saved = false;
    private String type;

    private JTextField amountField;
    private JComboBox<String> categoryCombo;
    private JTextField noteField;
    private JTextField dateField;

    // Example categories; user can type custom category too
    private static final String[] EXPENSE_CATS = {
            "Food", "Transport", "Shopping", "Bills", "Rent", "Entertainment", "Health", "Education", "Other"
    };

    public AddTransactionDialog(Frame owner, String type, DBHelper db, int userId) {
        super(owner, (type.equals("income")?"Add Income":"Add Expense"), true);
        this.db = db;
        this.userId = userId;
        this.type = type;
        setSize(420,260);
        setLocationRelativeTo(owner);
        initUI();
    }

    private void initUI() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(p);

        JPanel form = new JPanel(new GridLayout(4,2,8,8));
        form.add(new JLabel("Amount (₹):"));
        amountField = new JTextField();
        form.add(amountField);

        form.add(new JLabel("Category:"));
        if (type.equals("expense")) {
            categoryCombo = new JComboBox<>(EXPENSE_CATS);
            categoryCombo.setEditable(true);
            form.add(categoryCombo);
        } else {
            categoryCombo = new JComboBox<>(new String[] {"Salary","Gift","Interest","Other"});
            categoryCombo.setEditable(true);
            form.add(categoryCombo);
        }

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField(LocalDate.now().toString());
        form.add(dateField);

        form.add(new JLabel("Note:"));
        noteField = new JTextField();
        form.add(noteField);

        p.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        btns.add(save);
        btns.add(cancel);
        p.add(btns, BorderLayout.SOUTH);

        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> dispose());
    }

    private void onSave() {
        String amtS = amountField.getText().trim();
        if (amtS.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter amount.");
            return;
        }
        double amt;
        try {
            amt = Double.parseDouble(amtS);
            if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive number for amount.");
            return;
        }
        String cat = categoryCombo.getEditor().getItem().toString().trim();
        String note = noteField.getText().trim();
        String date = dateField.getText().trim();
        // Validate date format (basic)
        try {
            DateTimeFormatter.ISO_LOCAL_DATE.parse(date);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter date in YYYY-MM-DD format.");
            return;
        }

        boolean ok = db.addTransaction(userId, type, amt, cat, note, date);
        if (ok) {
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save transaction.");
        }
    }
}
