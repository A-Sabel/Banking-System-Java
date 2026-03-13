import java.util.ArrayList;
import java.util.List;

public class Bank {

    private final List<Account> accounts = new ArrayList<>();

    public int openSavings(String owner, double opening, double rate) {
        Account a = new SavingsAccount(owner, opening, rate);
        accounts.add(a);
        return a.getAccountId();
    }

    public int openSavings(int accountId, String owner, double opening, double rate) {
        Account a = new SavingsAccount(accountId, owner, opening, rate);
        accounts.add(a);
        return a.getAccountId();
    }

    public int openChecking(String owner, double opening, double fee) {
        Account a = new CheckingAccount(owner, opening, fee);
        accounts.add(a);
        return a.getAccountId();
    }

    public int openChecking(int accountId, String owner, double opening, double fee) {
        Account a = new CheckingAccount(accountId, owner, opening, fee);
        accounts.add(a);
        return a.getAccountId();
    }

    public Account find(int id) {
        for (Account a : accounts)
            if (a.getAccountId() == id)
                return a;
        return null;
    }

    public void transfer(int fromId, int toId, double amount) {
        Account from = find(fromId);
        Account to = find(toId);

        if (from == null || to == null)
            throw new IllegalArgumentException("One or both accounts were not found.");

        if (amount <= 0)
            throw new IllegalArgumentException("Transfer must be positive.");

        if (fromId == toId)
            throw new IllegalArgumentException("Cannot transfer to the same account.");

        from.withdraw(amount);
        to.deposit(amount);
    }

    public void processMonthEnd() {
        for (Account a : accounts)
            a.monthlyProcess();
    }

    public boolean deleteAccount(int id) {
        return accounts.removeIf(account -> account.getAccountId() == id);
    }

    public void clearAccounts() {
        accounts.clear();
    }

    public void listAccounts() {
        for (Account a : accounts)
            System.out.println(a);
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }
}
