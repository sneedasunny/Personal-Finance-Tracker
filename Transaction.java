public class Transaction {
    public int id;
    public int userId;
    public String type; // income / expense
    public double amount;
    public String category;
    public String note;
    public String date; // ISO string e.g. 2025-10-10

    public Transaction(int id, int userId, String type, double amount, String category, String note, String date) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
    }
}

