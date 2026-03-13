import java.util.ArrayList;
import java.util.List;

public abstract class Account {

    private static int NEXT_ID = 1000;
    private final int accountId;
    private String ownerName;
    private double balance;
    private final List<String> transactionHistory = new ArrayList<>();

    public Account(String ownerName, double openingBalance) {
        this(NEXT_ID++, ownerName, openingBalance);
    }

    public Account(int accountId, String ownerName, double openingBalance) {
        if (openingBalance < 0)
            throw new IllegalArgumentException("Opening balance cannot be negative.");
        if (ownerName == null || ownerName.trim().isEmpty())
            throw new IllegalArgumentException("Owner name is required.");

        this.accountId = accountId;
        this.ownerName = ownerName.trim();
        this.balance = openingBalance;

        if (accountId >= NEXT_ID) {
            NEXT_ID = accountId + 1;
        }
    }

    public int getAccountId() {
        return accountId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty())
            throw new IllegalArgumentException("Owner name is required.");
        this.ownerName = ownerName.trim();
    }

    public double getBalance() {
        return balance;
    }

    protected void setBalance(double newBalance) {
        this.balance = newBalance;
    }

    public void updateBalance(double newBalance) {
        if (newBalance < 0)
            throw new IllegalArgumentException("Balance cannot be negative.");
        this.balance = newBalance;
    }

    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit must be positive.");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal must be positive.");
        if (amount > balance)
            throw new IllegalStateException("Insufficient funds.");
        balance -= amount;
    }

    public abstract void monthlyProcess();

    public void addTransaction(String transaction) {
        transactionHistory.add(0, transaction);
        if (transactionHistory.size() > 20) {
            transactionHistory.remove(transactionHistory.size() - 1);
        }
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
    
    public static void setNextId(int nextId) {
        NEXT_ID = nextId;
    }

    @Override
    public String toString() {
        return String.format("%s (id=%d, owner='%s', balance=%.2f)",
                getClass().getSimpleName(),
                accountId,
                ownerName,
                balance);
    }
}
