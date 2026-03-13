public class CheckingAccount extends Account {

    private double transactionFee;

    public CheckingAccount(String ownerName, double openingBalance, double transactionFee) {
        super(ownerName, openingBalance);
        setTransactionFee(transactionFee);
    }

    public CheckingAccount(int accountId, String ownerName, double openingBalance, double transactionFee) {
        super(accountId, ownerName, openingBalance);
        setTransactionFee(transactionFee);
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        if (transactionFee < 0)
            throw new IllegalArgumentException("Fee cannot be negative.");
        this.transactionFee = transactionFee;
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal must be positive.");

        double total = amount + transactionFee;

        if (total > getBalance())
            throw new IllegalStateException("Insufficient funds (including fee).");

        setBalance(getBalance() - total);
    }

    @Override
    public void monthlyProcess() {
        // Optional: maintenance rules (no-op)
    }
}
