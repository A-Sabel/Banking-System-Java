public class SavingsAccount extends Account {

    private double interestRate;

    public SavingsAccount(String ownerName, double openingBalance, double interestRate) {
        super(ownerName, openingBalance);
        setInterestRate(interestRate);
    }

    public SavingsAccount(int accountId, String ownerName, double openingBalance, double interestRate) {
        super(accountId, ownerName, openingBalance);
        setInterestRate(interestRate);
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        if (interestRate < 0)
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        this.interestRate = interestRate;
    }

    @Override
    public void monthlyProcess() {
        double interest = getBalance() * interestRate;
        setBalance(getBalance() + interest);
    }
}
