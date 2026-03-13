import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class BankingGUI extends JFrame {

    private Bank bank;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;
    private JLabel totalAccountsLabel;
    private JLabel totalCapitalLabel;
    private JLabel lastTransactionLabel;
    private String lastTransaction = "None";
    private DefaultTableModel tableModel;
    private JTable accountTable;
    private LinkedList<String> transactionHistory;
    private List<String> allTransactionHistory;
    private DefaultTableModel historyTableModel;
    private List<JComboBox<String>> accountSelectors;
    private JTextArea accountHistoryArea;
    private JTextArea monthEndPreviewArea;
    private JTextField accountSearchField;
    private static final int MAX_HISTORY = 15;
    private static final File ACCOUNTS_FILE = new File("src/Files/accounts.txt");
    private static final File TRANSACTIONS_FILE = new File("src/Files/transactionss.txt");
    
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color PANEL_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);

    public BankingGUI() {
        bank = new Bank();
        transactionHistory = new LinkedList<>();
        allTransactionHistory = new ArrayList<>();
        accountSelectors = new ArrayList<>();
        loadTransactionHistory();
        loadFromCSV();
        
        setTitle("Banking Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Banking Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 15));
        menuPanel.setBackground(BACKGROUND_COLOR);
        menuPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 2, 0, new Color(189, 195, 199)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        menuPanel.add(createStyledButton("Home", new Color(41, 128, 185), e -> showCard("WELCOME")));
        menuPanel.add(createStyledButton("Open Savings", SECONDARY_COLOR, e -> showCard("SAVINGS")));
        menuPanel.add(createStyledButton("Open Checking", SECONDARY_COLOR, e -> showCard("CHECKING")));
        menuPanel.add(createStyledButton("Deposit", SUCCESS_COLOR, e -> showCard("DEPOSIT")));
        menuPanel.add(createStyledButton("Withdraw", WARNING_COLOR, e -> showCard("WITHDRAW")));
        menuPanel.add(createStyledButton("Transfer", new Color(155, 89, 182), e -> showCard("TRANSFER")));
        menuPanel.add(createStyledButton("Accounts", new Color(52, 73, 94), e -> showCard("ACCOUNTS")));
        menuPanel.add(createStyledButton("Month End", DANGER_COLOR, e -> showCard("MONTHEND")));
        menuPanel.add(createStyledButton("Exit", new Color(192, 57, 43), e -> System.exit(0)));
        
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setPreferredSize(new Dimension(800, 400));
        contentPanel.setBackground(PANEL_COLOR);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 20, 15, 20),
            new LineBorder(new Color(189, 195, 199), 1, true)
        ));
        
        // Create all form panels and add to CardLayout
        contentPanel.add(createWelcomePanel(), "WELCOME");
        contentPanel.add(createOpenSavingsPanel(), "SAVINGS");
        contentPanel.add(createOpenCheckingPanel(), "CHECKING");
        contentPanel.add(createDepositPanel(), "DEPOSIT");
        contentPanel.add(createWithdrawPanel(), "WITHDRAW");
        contentPanel.add(createTransferPanel(), "TRANSFER");
        contentPanel.add(createAccountListPanel(), "ACCOUNTS");
        contentPanel.add(createMonthEndPanel(), "MONTHEND");
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.add(menuPanel, BorderLayout.NORTH);
        
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(BACKGROUND_COLOR);
        contentWrapper.add(contentPanel, BorderLayout.CENTER);
        contentWrapper.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        mainPanel.add(contentWrapper, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        
        // status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        statusLabel.setBackground(BACKGROUND_COLOR);
statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.PAGE_END);
        
        // welcome screen
        cardLayout.show(contentPanel, "WELCOME");
        updateStatus("Welcome! Ready to assist you.");
        
        setSize(900, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showCard(String cardName) {
        if (cardName.equals("DEPOSIT") || cardName.equals("WITHDRAW") || cardName.equals("TRANSFER")) {
            refreshAccountSelectors(false);
        }
        if (cardName.equals("ACCOUNTS")) {
            refreshAccountList();
            updateAccountHistoryView(getSelectedTableAccount());
        }
        if (cardName.equals("MONTHEND")) {
            updateMonthEndPreview();
        }
        cardLayout.show(contentPanel, cardName);
        updateStatus(getStatusForCard(cardName));
    }

    private String getStatusForCard(String cardName) {
        switch (cardName) {
            case "SAVINGS": return "Opening Savings Account form";
            case "CHECKING": return "Opening Checking Account form";
            case "DEPOSIT": return "Deposit transaction form";
            case "WITHDRAW": return "Withdrawal transaction form";
            case "TRANSFER": return "Transfer transaction form";
            case "ACCOUNTS": return getTotalAccounts() + " account(s) in the system";
            case "MONTHEND": return "Month-end processing form";
            default: return "Ready to assist you";
        }
    }

    private JButton createStyledButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(12, 22, 12, 22));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void updateQuickStats() {
        if (totalAccountsLabel != null) {
            totalAccountsLabel.setText(String.valueOf(getTotalAccounts()));
        }
        if (totalCapitalLabel != null) {
            totalCapitalLabel.setText(String.format("$%.2f", getTotalCapital()));
        }
        if (lastTransactionLabel != null) {
            lastTransactionLabel.setText(lastTransaction);
        }
    }

    private void addTransactionToHistory(String transaction) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        transactionHistory.addFirst(transaction);
        allTransactionHistory.add(transaction);
        
        if (transactionHistory.size() > MAX_HISTORY) {
            transactionHistory.removeLast();
        }

        saveTransactionHistory();
        syncTransactionHistoryTable(timestamp);
    }

    private void syncTransactionHistoryTable(String fallbackTimestamp) {
        if (historyTableModel != null) {
            historyTableModel.setRowCount(0);
            for (String trans : transactionHistory) {
                String[] parts = trans.split("\\|", 2);
                if (parts.length == 2) {
                    historyTableModel.addRow(new Object[]{parts[0], parts[1]});
                } else {
                    historyTableModel.addRow(new Object[]{fallbackTimestamp, trans});
                }
            }
        }
    }

    private void rebuildRecentTransactionHistory() {
        transactionHistory.clear();
        int start = Math.max(0, allTransactionHistory.size() - MAX_HISTORY);
        for (int i = allTransactionHistory.size() - 1; i >= start; i--) {
            transactionHistory.add(allTransactionHistory.get(i));
        }
    }

    private void saveTransactionHistory() {
        File parent = TRANSACTIONS_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(TRANSACTIONS_FILE))) {
            for (String transaction : allTransactionHistory) {
                writer.println(transaction);
            }
        } catch (IOException e) {
            updateStatus("Unable to save transaction history: " + e.getMessage());
        }
    }

    private void loadTransactionHistory() {
        allTransactionHistory.clear();

        if (!TRANSACTIONS_FILE.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(TRANSACTIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim();
                if (!value.isEmpty()) {
                    allTransactionHistory.add(value);
                }
            }
            rebuildRecentTransactionHistory();
        } catch (IOException e) {
            updateStatus("Unable to load transaction history.");
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    private void recordAccountTransaction(String message, Account... accounts) {
        String timestamp = getCurrentTimestamp();
        addTransactionToHistory(timestamp + "|" + message);
        for (Account account : accounts) {
            if (account != null) {
                account.addTransaction(timestamp + " - " + message);
            }
        }
        updateAccountHistoryView(getSelectedTableAccount());
    }

    private String validateOwnerName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.length() < 2) {
            throw new IllegalArgumentException("Owner name must be at least 2 characters long.");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Owner name must be 50 characters or fewer.");
        }
        if (!name.matches("[A-Za-z][A-Za-z .'-]*")) {
            throw new IllegalArgumentException("Owner name can only contain letters, spaces, apostrophes, periods, and hyphens.");
        }
        return name;
    }

    private double validateInterestRate(double rate) {
        if (rate < 0 || rate > 0.25) {
            throw new IllegalArgumentException("Interest rate must be between 0.00 and 0.25 (0% to 25%).");
        }
        return rate;
    }

    private double validateTransactionFee(double fee) {
        if (fee < 0 || fee > 1000) {
            throw new IllegalArgumentException("Transaction fee must be between 0.00 and 1000.00.");
        }
        return fee;
    }

    private void applyPlaceholder(JTextField field, String placeholder) {
        field.putClientProperty("placeholder", placeholder);
        field.putClientProperty("showingPlaceholder", Boolean.TRUE);
        field.setForeground(new Color(149, 165, 166));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (Boolean.TRUE.equals(field.getClientProperty("showingPlaceholder"))) {
                    field.putClientProperty("showingPlaceholder", Boolean.FALSE);
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.putClientProperty("showingPlaceholder", Boolean.TRUE);
                    field.setForeground(new Color(149, 165, 166));
                    field.setText(placeholder);
                }
            }
        });
    }

    private boolean isShowingPlaceholder(JTextField field) {
        return Boolean.TRUE.equals(field.getClientProperty("showingPlaceholder"));
    }

    private void runWithLoading(String message, Runnable action) {
        Cursor originalCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        updateStatus(message);
        try {
            action.run();
        } finally {
            setCursor(originalCursor);
        }
    }

    private Account getSelectedTableAccount() {
        if (accountTable == null || accountTable.getSelectedRow() < 0) {
            return null;
        }
        int viewRow = accountTable.getSelectedRow();
        int modelRow = accountTable.convertRowIndexToModel(viewRow);
        Object value = tableModel.getValueAt(modelRow, 1);
        if (value == null) {
            return null;
        }
        return bank.find(Integer.parseInt(String.valueOf(value)));
    }

    private void updateAccountHistoryView(Account account) {
        if (accountHistoryArea == null) {
            return;
        }
        if (account == null) {
            accountHistoryArea.setText("Select an account in the table to view account-specific history.");
            return;
        }
        List<String> history = account.getTransactionHistory();
        if (history.isEmpty()) {
            accountHistoryArea.setText("No recorded transactions for account #" + account.getAccountId() + ".");
            return;
        }
        accountHistoryArea.setText(String.join("\n", history));
        accountHistoryArea.setCaretPosition(0);
    }

    private String getAccountDetails(Account account) {
        if (account instanceof SavingsAccount) {
            return String.format("Rate: %.2f%%", ((SavingsAccount) account).getInterestRate() * 100);
        }
        if (account instanceof CheckingAccount) {
            return String.format("Fee: $%.2f", ((CheckingAccount) account).getTransactionFee());
        }
        return "";
    }

    private int getPreviewSavingsCount() {
        int count = 0;
        for (Account account : bank.getAccounts()) {
            if (account instanceof SavingsAccount) {
                count++;
            }
        }
        return count;
    }

    private void updateMonthEndPreview() {
        if (monthEndPreviewArea == null) {
            return;
        }
        StringBuilder preview = new StringBuilder();
        double totalInterest = 0;
        for (Account account : bank.getAccounts()) {
            if (account instanceof SavingsAccount) {
                SavingsAccount savings = (SavingsAccount) account;
                double interest = savings.getBalance() * savings.getInterestRate();
                totalInterest += interest;
                preview.append(String.format("#%d %s: +$%.2f -> New Balance: $%.2f%n",
                    savings.getAccountId(),
                    savings.getOwnerName(),
                    interest,
                    savings.getBalance() + interest));
            }
        }
        if (preview.length() == 0) {
            preview.append("No savings accounts available for month-end processing.");
        } else {
            preview.insert(0, String.format("Projected interest payout: $%.2f%n%n", totalInterest));
        }
        monthEndPreviewArea.setText(preview.toString());
        monthEndPreviewArea.setCaretPosition(0);
    }

    private void saveToFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Type,ID,Owner,Balance,Extra");
            for (Account account : bank.getAccounts()) {
                String type = account.getClass().getSimpleName();
                int id = account.getAccountId();
                String owner = account.getOwnerName();
                double balance = account.getBalance();
                String extra = "";

                if (account instanceof SavingsAccount) {
                    extra = String.valueOf(((SavingsAccount) account).getInterestRate());
                } else if (account instanceof CheckingAccount) {
                    extra = String.valueOf(((CheckingAccount) account).getTransactionFee());
                }

                writer.println(String.format("%s,%d,%s,%.2f,%s", type, id, owner, balance, extra));
            }
        }
    }

    private void loadFromFile(File file, boolean replaceExisting) throws IOException {
        List<String[]> loadedRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    loadedRows.add(parts);
                }
            }
        }

        if (replaceExisting) {
            bank.clearAccounts();
        }

        int maxId = 999;
        for (String[] parts : loadedRows) {
            String type = parts[0];
            int id = Integer.parseInt(parts[1]);
            String owner = parts[2];
            double balance = Double.parseDouble(parts[3]);
            double extra = Double.parseDouble(parts[4]);
            maxId = Math.max(maxId, id);

            if (type.equals("SavingsAccount")) {
                bank.openSavings(id, owner, balance, extra);
            } else if (type.equals("CheckingAccount")) {
                bank.openChecking(id, owner, balance, extra);
            }
        }
        Account.setNextId(maxId + 1);
    }

    private void exportAccounts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Accounts");
        chooser.setSelectedFile(new File("accounts-export.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            runWithLoading("Exporting accounts...", () -> {
                try {
                    saveToFile(chooser.getSelectedFile());
                    updateStatus("Accounts exported to " + chooser.getSelectedFile().getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void backupAccounts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Backup Accounts");
        chooser.setSelectedFile(new File("accounts-backup.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            runWithLoading("Creating backup...", () -> {
                try {
                    saveToFile(chooser.getSelectedFile());
                    updateStatus("Backup created: " + chooser.getSelectedFile().getName());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private void restoreAccounts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Restore Accounts");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Restore will replace current accounts with the selected backup. Continue?",
                "Confirm Restore",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                runWithLoading("Restoring backup...", () -> {
                    try {
                        loadFromFile(chooser.getSelectedFile(), true);
                        saveToCSV();
                        refreshAccountSelectors(false);
                        refreshAccountList();
                        updateMonthEndPreview();
                        updateAccountHistoryView(null);
                        updateStatus("Backup restored from " + chooser.getSelectedFile().getName());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Restore failed: " + ex.getMessage(), "Restore Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }

    private void deleteSelectedAccount() {
        Account selected = getSelectedTableAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an account in the table first.", "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Delete account #%d owned by %s?", selected.getAccountId(), selected.getOwnerName()),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            bank.deleteAccount(selected.getAccountId());
            lastTransaction = "Deleted account #" + selected.getAccountId();
            addTransactionToHistory(getCurrentTimestamp() + "|" + lastTransaction);
            saveToCSV();
            refreshAccountSelectors(false);
            refreshAccountList();
            updateMonthEndPreview();
            updateAccountHistoryView(null);
            updateQuickStats();
            updateStatus(lastTransaction);
        }
    }

    private void editSelectedAccount() {
        Account selected = getSelectedTableAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an account in the table first.", "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField ownerField = new JTextField(selected.getOwnerName(), 20);
        JTextField balanceField = new JTextField(String.format("%.2f", selected.getBalance()), 20);
        JTextField extraField = new JTextField(20);
        String extraLabel = "";

        if (selected instanceof SavingsAccount) {
            extraLabel = "Interest Rate (0.00 - 0.25):";
            extraField.setText(String.valueOf(((SavingsAccount) selected).getInterestRate()));
        } else if (selected instanceof CheckingAccount) {
            extraLabel = "Transaction Fee (0.00 - 1000.00):";
            extraField.setText(String.valueOf(((CheckingAccount) selected).getTransactionFee()));
        }

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Owner Name:"));
        panel.add(ownerField);
        panel.add(new JLabel("Balance:"));
        panel.add(balanceField);
        panel.add(new JLabel(extraLabel));
        panel.add(extraField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Account #" + selected.getAccountId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                selected.setOwnerName(validateOwnerName(ownerField.getText()));
                selected.updateBalance(Double.parseDouble(balanceField.getText().trim()));
                if (selected instanceof SavingsAccount) {
                    ((SavingsAccount) selected).setInterestRate(validateInterestRate(Double.parseDouble(extraField.getText().trim())));
                } else if (selected instanceof CheckingAccount) {
                    ((CheckingAccount) selected).setTransactionFee(validateTransactionFee(Double.parseDouble(extraField.getText().trim())));
                }
                lastTransaction = "Edited account #" + selected.getAccountId();
                recordAccountTransaction(lastTransaction, selected);
                saveToCSV();
                refreshAccountSelectors(false);
                refreshAccountList();
                updateMonthEndPreview();
                updateQuickStats();
                updateStatus(lastTransaction);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to update account: " + ex.getMessage(), "Edit Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int getTotalAccounts() {
        return bank.getAccounts().size();
    }

    private double getTotalCapital() {
        return bank.getAccounts().stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    private JPanel createQuickInfoCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color, 2, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(127, 140, 141));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_COLOR);
        
        // Quick stats cards at top
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(PANEL_COLOR);
        statsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JPanel accountsCard = createQuickInfoCard("Total Accounts", "0", SECONDARY_COLOR);
        totalAccountsLabel = (JLabel) accountsCard.getComponent(1);
        totalAccountsLabel.setText(String.valueOf(getTotalAccounts()));
        
        JPanel capitalCard = createQuickInfoCard("Total Bank Capital", "$0.00", SUCCESS_COLOR);
        totalCapitalLabel = (JLabel) capitalCard.getComponent(1);
        totalCapitalLabel.setText(String.format("$%.2f", getTotalCapital()));
        
        JPanel transactionCard = createQuickInfoCard("Last Transaction", "None", WARNING_COLOR);
        lastTransactionLabel = (JLabel) transactionCard.getComponent(1);
        lastTransactionLabel.setText(lastTransaction);
        
        statsPanel.add(accountsCard);
        statsPanel.add(capitalCard);
        statsPanel.add(transactionCard);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Welcome message
        JPanel messagePanel = new JPanel(new GridLayout(3, 1, 10, 10));
        messagePanel.setBackground(PANEL_COLOR);
        
        JLabel iconLabel = new JLabel("BANK", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 64));
        iconLabel.setForeground(PRIMARY_COLOR);
        
        JLabel welcomeLabel = new JLabel("Welcome to Banking System", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        
        JLabel instructionLabel = new JLabel("Select an operation from the menu above", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(TEXT_COLOR);
        
        messagePanel.add(iconLabel);
        messagePanel.add(welcomeLabel);
        messagePanel.add(instructionLabel);
        
        panel.add(messagePanel, BorderLayout.CENTER);
        
        // Transaction History Panel
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBackground(PANEL_COLOR);
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(20, 0, 0, 0),
            BorderFactory.createTitledBorder(
                new LineBorder(PRIMARY_COLOR, 2),
                "Recent Transactions",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                PRIMARY_COLOR
            )
        ));
        
        String[] columns = {"Time", "Transaction"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(25);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyTable.setShowGrid(false);
        historyTable.setIntercellSpacing(new java.awt.Dimension(0, 0));

        syncTransactionHistoryTable(getCurrentTimestamp());
        
        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setPreferredSize(new java.awt.Dimension(0, 200));
        historyScroll.setBorder(null);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        
        panel.add(historyPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JTextField createValidatedTextField(JButton submitButton) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validate(); }
            public void removeUpdate(DocumentEvent e) { validate(); }
            public void insertUpdate(DocumentEvent e) { validate(); }
            
            private void validate() {
                String text = field.getText().trim();
                if (text.isEmpty()) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(189, 195, 199), 1),
                        new EmptyBorder(8, 10, 8, 10)
                    ));
                    submitButton.setEnabled(false);
                } else {
                    try {
                        double value = Double.parseDouble(text);
                        if (value > 0) {
                            field.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(SUCCESS_COLOR, 2),
                                new EmptyBorder(7, 9, 7, 9)
                            ));
                            submitButton.setEnabled(true);
                        } else {
                            // Zero or negative value
                            field.setBorder(BorderFactory.createCompoundBorder(
                                new LineBorder(DANGER_COLOR, 2),
                                new EmptyBorder(7, 9, 7, 9)
                            ));
                            submitButton.setEnabled(false);
                        }
                    } catch (NumberFormatException ex) {
                        field.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(DANGER_COLOR, 2),
                            new EmptyBorder(7, 9, 7, 9)
                        ));
                        submitButton.setEnabled(false);
                    }
                }
            }
        });
        
        return field;
    }

    private JButton createActionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(12, 30, 12, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }

    private JButton createClearButton(JTextField... fields) {
        JButton btn = new JButton("Clear");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(new Color(189, 195, 199));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(12, 30, 12, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            for (JTextField field : fields) {
                String placeholder = (String) field.getClientProperty("placeholder");
                if (placeholder != null) {
                    field.putClientProperty("showingPlaceholder", Boolean.TRUE);
                    field.setForeground(new Color(149, 165, 166));
                    field.setText(placeholder);
                } else {
                    field.setText("");
                }
            }
        });
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(149, 165, 166));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(189, 195, 199));
            }
        });
        
        return btn;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JPanel createFormPanel(String title) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBackground(PANEL_COLOR);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        return mainPanel;
    }

    private void addEnterKeyListener(JTextField field, JButton submitButton) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && submitButton.isEnabled()) {
                    submitButton.doClick();
                }
            }
        });
    }

    private JComboBox<String> createAccountComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setEditable(true);
        populateAccountComboBox(comboBox, "");
        installAccountSearch(comboBox);
        accountSelectors.add(comboBox);
        
        return comboBox;
    }

    private Account getSelectedAccount(JComboBox<String> comboBox) {
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem == null || String.valueOf(selectedItem).trim().isEmpty()) {
            selectedItem = comboBox.getEditor().getItem();
        }
        if (selectedItem == null) {
            selectedItem = comboBox.getSelectedItem();
        }

        String selectedText = String.valueOf(selectedItem).trim();
        if (selectedText.isEmpty() || selectedText.equals("-- Select Account --")) {
            return null;
        }

        int separatorIndex = selectedText.indexOf(' ');
        if (!selectedText.startsWith("#") || separatorIndex <= 1) {
            return null;
        }

        try {
            int accountId = Integer.parseInt(selectedText.substring(1, separatorIndex));
            return bank.find(accountId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void populateAccountComboBox(JComboBox<String> comboBox, String filterText) {
        String currentText = filterText == null ? "" : filterText.trim().toLowerCase();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("-- Select Account --");

        for (Account account : bank.getAccounts()) {
            String displayText = String.format("#%d - %s (%s) - $%.2f",
                account.getAccountId(),
                account.getOwnerName(),
                account.getClass().getSimpleName().replace("Account", ""),
                account.getBalance());

            if (currentText.isEmpty() || displayText.toLowerCase().contains(currentText)) {
                model.addElement(displayText);
            }
        }

        comboBox.setModel(model);
    }

    private void installAccountSearch(JComboBox<String> comboBox) {
        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        editor.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));

        final boolean[] updating = {false};
        final boolean[] acceptingSelection = {false};
        editor.getDocument().addDocumentListener(new DocumentListener() {
            private void updateFilter() {
                if (updating[0] || acceptingSelection[0]) {
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    if (updating[0] || acceptingSelection[0]) {
                        return;
                    }

                    updating[0] = true;
                    String typedText = editor.getText();
                    int caretPosition = editor.getCaretPosition();
                    populateAccountComboBox(comboBox, typedText);
                    comboBox.getEditor().setItem(typedText);
                    if (comboBox.isShowing() && !typedText.trim().isEmpty() && comboBox.getItemCount() > 1) {
                        comboBox.setPopupVisible(true);
                    }
                    editor.setCaretPosition(Math.min(caretPosition, typedText.length()));
                    updating[0] = false;
                });
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFilter();
            }
        });

        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                acceptingSelection[0] = false;
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(() -> acceptingSelection[0] = false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                acceptingSelection[0] = false;
            }
        });

        comboBox.addActionListener(e -> {
            if (updating[0]) {
                return;
            }

            if (comboBox.isPopupVisible() && comboBox.getSelectedIndex() > 0 && comboBox.getSelectedItem() != null) {
                acceptingSelection[0] = true;
            }
        });
    }

    private void refreshAccountSelectors(boolean preserveFilter) {
        List<JComboBox<String>> activeSelectors = new ArrayList<>();
        for (JComboBox<String> comboBox : accountSelectors) {
            if (comboBox.isDisplayable()) {
                String currentText = preserveFilter ? String.valueOf(comboBox.getEditor().getItem()) : "";
                populateAccountComboBox(comboBox, currentText);
                if (preserveFilter) {
                    comboBox.getEditor().setItem(currentText);
                } else {
                    comboBox.setSelectedIndex(0);
                }
                comboBox.revalidate();
                comboBox.repaint();
                activeSelectors.add(comboBox);
            }
        }
        accountSelectors = activeSelectors;
    }

    private JPanel createOpenSavingsPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PANEL_COLOR);
        
        JPanel formPanel = createFormPanel("Open Savings Account");
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        fieldsPanel.setBackground(PANEL_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        fieldsPanel.add(createFieldLabel("Owner Name:"));
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        nameField.setToolTipText("Enter the account owner's full name");
        applyPlaceholder(nameField, "e.g., Andrea Dela Cruz");
        fieldsPanel.add(nameField);
        
        JButton submitBtn = createActionButton("✓ Create Account", SECONDARY_COLOR);
        
        fieldsPanel.add(createFieldLabel("Opening Balance:"));
        JTextField balanceField = createValidatedTextField(submitBtn);
        balanceField.setToolTipText("Enter initial deposit amount (must be positive)");
        fieldsPanel.add(balanceField);
        
        fieldsPanel.add(createFieldLabel("Interest Rate (e.g., 0.01):"));
        JTextField rateField = createValidatedTextField(submitBtn);
        rateField.setToolTipText("Enter as decimal: 0.01 for 1%, 0.05 for 5%");
        fieldsPanel.add(rateField);
        
        // Add Enter key support
        addEnterKeyListener(nameField, submitBtn);
        addEnterKeyListener(balanceField, submitBtn);
        addEnterKeyListener(rateField, submitBtn);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> {
            try {
                String name = validateOwnerName(isShowingPlaceholder(nameField) ? "" : nameField.getText());
                double balance = Double.parseDouble(balanceField.getText().trim());
                double rate = validateInterestRate(Double.parseDouble(rateField.getText().trim()));
                
                int accountId = bank.openSavings(name, balance, rate);
                Account account = bank.find(accountId);
                lastTransaction = "Savings account #" + accountId + " created";
                recordAccountTransaction(lastTransaction, account);
                saveToCSV();
                
                JOptionPane.showMessageDialog(this, 
                    "Savings Account Created Successfully!\n\n" +
                    "Account ID: " + accountId + "\n" +
                    "Owner: " + name + "\n" +
                    "Balance: $" + String.format("%.2f", balance), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshAccountSelectors(false);
                nameField.setText("");
                balanceField.setText("");
                rateField.setText("");
                updateQuickStats();
                updateStatus("Savings account " + accountId + " created successfully");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton clearBtn = createClearButton(nameField, balanceField, rateField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formPanel);
        return mainPanel;
    }

    private JPanel createOpenCheckingPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PANEL_COLOR);
        
        JPanel formPanel = createFormPanel("Open Checking Account");
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        fieldsPanel.setBackground(PANEL_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        fieldsPanel.add(createFieldLabel("Owner Name:"));
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        nameField.setToolTipText("Enter the account owner's full name");
        applyPlaceholder(nameField, "e.g., Ysabela Santos");
        fieldsPanel.add(nameField);
        
        JButton submitBtn = createActionButton("✓ Create Account", SECONDARY_COLOR);
        
        fieldsPanel.add(createFieldLabel("Opening Balance:"));
        JTextField balanceField = createValidatedTextField(submitBtn);
        balanceField.setToolTipText("Enter initial deposit amount (must be positive)");
        fieldsPanel.add(balanceField);
        
        fieldsPanel.add(createFieldLabel("Transaction Fee:"));
        JTextField feeField = createValidatedTextField(submitBtn);
        feeField.setToolTipText("Enter fee per transaction (e.g., 1.50 for $1.50)");
        fieldsPanel.add(feeField);
        
        // Add Enter key support
        addEnterKeyListener(nameField, submitBtn);
        addEnterKeyListener(balanceField, submitBtn);
        addEnterKeyListener(feeField, submitBtn);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> {
            try {
                String name = validateOwnerName(isShowingPlaceholder(nameField) ? "" : nameField.getText());
                double balance = Double.parseDouble(balanceField.getText().trim());
                double fee = validateTransactionFee(Double.parseDouble(feeField.getText().trim()));
                
                int accountId = bank.openChecking(name, balance, fee);
                Account account = bank.find(accountId);
                lastTransaction = "Checking account #" + accountId + " created";
                recordAccountTransaction(lastTransaction, account);
                saveToCSV();
                
                JOptionPane.showMessageDialog(this, 
                    "Checking Account Created Successfully!\n\n" +
                    "Account ID: " + accountId + "\n" +
                    "Owner: " + name + "\n" +
                    "Balance: $" + String.format("%.2f", balance), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshAccountSelectors(false);
                nameField.setText("");
                balanceField.setText("");
                feeField.setText("");
                updateQuickStats();
                updateStatus("Checking account " + accountId + " created successfully");
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton clearBtn = createClearButton(nameField, balanceField, feeField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formPanel);
        return mainPanel;
    }

    private JPanel createDepositPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PANEL_COLOR);
        
        JPanel formPanel = createFormPanel("Deposit Money");
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        fieldsPanel.setBackground(PANEL_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        JButton submitBtn = createActionButton("Deposit", SUCCESS_COLOR);
        
        fieldsPanel.add(createFieldLabel("Account:"));
        JComboBox<String> accountCombo = createAccountComboBox();
        accountCombo.setToolTipText("Select the account to deposit into");
        fieldsPanel.add(accountCombo);
        
        fieldsPanel.add(createFieldLabel("Amount:"));
        JTextField amountField = createValidatedTextField(submitBtn);
        amountField.setToolTipText("Enter the amount to deposit (must be positive)");
        fieldsPanel.add(amountField);
        
        // Add Enter key support
        addEnterKeyListener(amountField, submitBtn);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> {
            try {
                Account account = getSelectedAccount(accountCombo);
                if (account == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Please select an account!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double amount = Double.parseDouble(amountField.getText().trim());
                
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Amount must be positive!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                account.deposit(amount);
                lastTransaction = String.format("Deposited $%.2f to #%d", amount, account.getAccountId());
                recordAccountTransaction(lastTransaction, account);
                saveToCSV();
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Deposit Successful!\n\nDeposited: $%.2f\nNew Balance: $%.2f", 
                        amount, account.getBalance()), 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshAccountSelectors(false);
                accountCombo.setSelectedIndex(0);
                amountField.setText("");
                updateQuickStats();
                updateStatus(lastTransaction);
                refreshAccountList();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton clearBtn = createClearButton(amountField);
        clearBtn.addActionListener(e -> accountCombo.setSelectedIndex(0));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formPanel);
        return mainPanel;
    }

    private JPanel createWithdrawPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PANEL_COLOR);
        
        JPanel formPanel = createFormPanel("Withdraw Money");
        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        fieldsPanel.setBackground(PANEL_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        JButton submitBtn = createActionButton("Withdraw", WARNING_COLOR);
        
        fieldsPanel.add(createFieldLabel("Account:"));
        JComboBox<String> accountCombo = createAccountComboBox();
        accountCombo.setToolTipText("Select the account to withdraw from");
        fieldsPanel.add(accountCombo);
        
        fieldsPanel.add(createFieldLabel("Amount:"));
        JTextField amountField = createValidatedTextField(submitBtn);
        amountField.setToolTipText("Enter the amount to withdraw (must be positive)");
        fieldsPanel.add(amountField);
        
        // Balance preview panel
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        balancePanel.setBackground(PANEL_COLOR);
        JLabel balanceLabel = new JLabel("");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        balanceLabel.setForeground(PRIMARY_COLOR);
        balancePanel.add(balanceLabel);
        
        // Update balance preview when account is selected
        accountCombo.addActionListener(e -> {
            Account selected = getSelectedAccount(accountCombo);
            if (selected != null) {
                balanceLabel.setText(String.format("Current Balance: $%.2f", selected.getBalance()));
            } else {
                balanceLabel.setText("");
            }
        });
        
        // Add Enter key support
        addEnterKeyListener(amountField, submitBtn);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(PANEL_COLOR);
        centerPanel.add(fieldsPanel, BorderLayout.NORTH);
        centerPanel.add(balancePanel, BorderLayout.CENTER);
        formPanel.add(centerPanel, BorderLayout.CENTER);
        
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> {
            try {
                Account account = getSelectedAccount(accountCombo);
                if (account == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Please select an account!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double amount = Double.parseDouble(amountField.getText().trim());
                
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Amount must be positive!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double fee = (account instanceof CheckingAccount) ? ((CheckingAccount) account).getTransactionFee() : 0;
                account.withdraw(amount);
                String feeNote = fee > 0 ? String.format(" (fee: $%.2f)", fee) : "";
                lastTransaction = String.format("Withdrew $%.2f from #%d%s", amount, account.getAccountId(), feeNote);
                recordAccountTransaction(lastTransaction, account);
                saveToCSV();
                
                String withdrawMsg = String.format("Withdrawal Successful!\n\nWithdrew: $%.2f", amount);
                if (fee > 0) {
                    withdrawMsg += String.format("\nTransaction Fee: $%.2f\nTotal Deducted: $%.2f", fee, amount + fee);
                }
                withdrawMsg += String.format("\nNew Balance: $%.2f", account.getBalance());
                JOptionPane.showMessageDialog(this, withdrawMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                refreshAccountSelectors(false);
                accountCombo.setSelectedIndex(0);
                amountField.setText("");
                balanceLabel.setText("");
                updateQuickStats();
                updateStatus(lastTransaction);
                refreshAccountList();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton clearBtn = createClearButton(amountField);
        clearBtn.addActionListener(e -> accountCombo.setSelectedIndex(0));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formPanel);
        return mainPanel;
    }

    private JPanel createTransferPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(PANEL_COLOR);
        
        JPanel formPanel = createFormPanel("Transfer Money");
        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        fieldsPanel.setBackground(PANEL_COLOR);
        fieldsPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        JButton submitBtn = createActionButton("Transfer", new Color(155, 89, 182));
        
        fieldsPanel.add(createFieldLabel("From Account:"));
        JComboBox<String> fromAccountCombo = createAccountComboBox();
        fromAccountCombo.setToolTipText("Select the source account");
        fieldsPanel.add(fromAccountCombo);
        
        fieldsPanel.add(createFieldLabel("To Account:"));
        JComboBox<String> toAccountCombo = createAccountComboBox();
        toAccountCombo.setToolTipText("Select the destination account");
        fieldsPanel.add(toAccountCombo);
        
        fieldsPanel.add(createFieldLabel("Amount:"));
        JTextField amountField = createValidatedTextField(submitBtn);
        amountField.setToolTipText("Enter the amount to transfer (must be positive)");
        fieldsPanel.add(amountField);
        
        // Balance preview panel
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        balancePanel.setBackground(PANEL_COLOR);
        JLabel fromBalanceLabel = new JLabel("");
        fromBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fromBalanceLabel.setForeground(PRIMARY_COLOR);
        balancePanel.add(fromBalanceLabel);
        
        // Update balance preview when accounts are selected
        fromAccountCombo.addActionListener(e -> {
            Account selected = getSelectedAccount(fromAccountCombo);
            if (selected != null) {
                fromBalanceLabel.setText(String.format("From Account Balance: $%.2f", selected.getBalance()));
            } else {
                fromBalanceLabel.setText("");
            }
        });
        
        // Add Enter key support
        addEnterKeyListener(amountField, submitBtn);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(PANEL_COLOR);
        centerPanel.add(fieldsPanel, BorderLayout.NORTH);
        centerPanel.add(balancePanel, BorderLayout.CENTER);
        formPanel.add(centerPanel, BorderLayout.CENTER);
        
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> {
            try {
                Account fromAccount = getSelectedAccount(fromAccountCombo);
                Account toAccount = getSelectedAccount(toAccountCombo);
                
                if (fromAccount == null || toAccount == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Please select both accounts!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (fromAccount.getAccountId() == toAccount.getAccountId()) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot transfer to same account!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double amount = Double.parseDouble(amountField.getText().trim());
                
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Amount must be positive!", 
                        "Invalid Input", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                double fromFee = (fromAccount instanceof CheckingAccount) ? ((CheckingAccount) fromAccount).getTransactionFee() : 0;
                bank.transfer(fromAccount.getAccountId(), toAccount.getAccountId(), amount);
                String transferFeeNote = fromFee > 0 ? String.format(" (fee: $%.2f applied to source)", fromFee) : "";
                lastTransaction = String.format("Transferred $%.2f from #%d to #%d%s",
                    amount, fromAccount.getAccountId(), toAccount.getAccountId(), transferFeeNote);
                recordAccountTransaction(lastTransaction, fromAccount, toAccount);
                saveToCSV();
                
                String transferMsg = String.format("Transfer Successful!\n\nTransferred: $%.2f\nFrom Account: #%d (%s)\nTo Account: #%d (%s)",
                    amount, fromAccount.getAccountId(), fromAccount.getOwnerName(),
                    toAccount.getAccountId(), toAccount.getOwnerName());
                if (fromFee > 0) {
                    transferMsg += String.format("\nTransaction Fee: $%.2f\nTotal Debited from Source: $%.2f", fromFee, amount + fromFee);
                }
                JOptionPane.showMessageDialog(this, transferMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                refreshAccountSelectors(false);
                fromAccountCombo.setSelectedIndex(0);
                toAccountCombo.setSelectedIndex(0);
                amountField.setText("");
                fromBalanceLabel.setText("");
                updateQuickStats();
                updateStatus(lastTransaction);
                refreshAccountList();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton clearBtn = createClearButton(amountField);
        clearBtn.addActionListener(e -> {
            fromAccountCombo.setSelectedIndex(0);
            toAccountCombo.setSelectedIndex(0);
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(submitBtn);
        buttonPanel.add(clearBtn);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(formPanel);
        return mainPanel;
    }

    private JPanel createAccountListPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBackground(PANEL_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(PANEL_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("All Accounts");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(PANEL_COLOR);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchPanel.add(searchLabel);

        accountSearchField = new JTextField(20);
        accountSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accountSearchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        accountSearchField.setToolTipText("Search by ID, Owner, Type, or Balance");
        applyPlaceholder(accountSearchField, "Search by owner, type, balance...");
        searchPanel.add(accountSearchField);

        headerPanel.add(searchPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"Type", "ID", "Owner", "Balance", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setRowHeight(30);
        accountTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountTable.setSelectionBackground(new Color(52, 152, 219, 50));
        accountTable.setShowGrid(true);
        accountTable.setGridColor(new Color(189, 195, 199));
        accountTable.setAutoCreateRowSorter(true);
        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateAccountHistoryView(getSelectedTableAccount());
            }
        });

        accountSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTable(isShowingPlaceholder(accountSearchField) ? "" : accountSearchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTable(isShowingPlaceholder(accountSearchField) ? "" : accountSearchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTable(isShowingPlaceholder(accountSearchField) ? "" : accountSearchField.getText()); }
        });

        JTableHeader header = accountTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(PRIMARY_COLOR);
        headerRenderer.setForeground(Color.WHITE);
        headerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setOpaque(true);
        for (int i = 0; i < accountTable.getColumnModel().getColumnCount(); i++) {
            accountTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(accountTable);
        tableScrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));

        accountHistoryArea = new JTextArea();
        accountHistoryArea.setEditable(false);
        accountHistoryArea.setLineWrap(true);
        accountHistoryArea.setWrapStyleWord(true);
        accountHistoryArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountHistoryArea.setText("Select an account in the table to view account-specific history.");
        JScrollPane historyScrollPane = new JScrollPane(accountHistoryArea);
        historyScrollPane.setPreferredSize(new Dimension(0, 150));
        historyScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(189, 195, 199), 1), "Account Transaction History"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, historyScrollPane);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        JButton refreshBtn = createActionButton("Refresh", new Color(52, 73, 94));
        refreshBtn.addActionListener(e -> refreshAccountList());
        JButton editBtn = createActionButton("Edit", new Color(39, 174, 96));
        editBtn.addActionListener(e -> editSelectedAccount());
        JButton deleteBtn = createActionButton("Delete", DANGER_COLOR);
        deleteBtn.addActionListener(e -> deleteSelectedAccount());
        JButton exportBtn = createActionButton("Export CSV", new Color(41, 128, 185));
        exportBtn.addActionListener(e -> exportAccounts());
        JButton backupBtn = createActionButton("Backup", new Color(142, 68, 173));
        backupBtn.addActionListener(e -> backupAccounts());
        JButton restoreBtn = createActionButton("Restore", new Color(243, 156, 18));
        restoreBtn.addActionListener(e -> restoreAccounts());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(backupBtn);
        buttonPanel.add(restoreBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void refreshAccountList() {
        if (accountSearchField != null && !isShowingPlaceholder(accountSearchField) && !accountSearchField.getText().trim().isEmpty()) {
            filterTable(accountSearchField.getText());
            updateQuickStats();
            return;
        }

        tableModel.setRowCount(0);
        List<Account> accounts = bank.getAccounts();

        for (Account account : accounts) {
            String type = account.getClass().getSimpleName();
            int id = account.getAccountId();
            String owner = account.getOwnerName();
            String balance = String.format("$%.2f", account.getBalance());
            String details = getAccountDetails(account);

            tableModel.addRow(new Object[]{type, id, owner, balance, details});
        }

        updateQuickStats();
        updateAccountHistoryView(getSelectedTableAccount());
    }

    private void filterTable(String searchText) {
        tableModel.setRowCount(0);
        List<Account> accounts = bank.getAccounts();
        String filter = searchText.toLowerCase().trim();
        
        for (Account account : accounts) {
            String type = account.getClass().getSimpleName();
            int id = account.getAccountId();
            String owner = account.getOwnerName();
            String balance = String.format("$%.2f", account.getBalance());
            String details = "";
            
            if (account instanceof SavingsAccount) {
                SavingsAccount sa = (SavingsAccount) account;
                details = String.format("Rate: %.2f%%", sa.getInterestRate() * 100);
            } else if (account instanceof CheckingAccount) {
                CheckingAccount ca = (CheckingAccount) account;
                details = String.format("Fee: $%.2f", ca.getTransactionFee());
            }
            
            // Filter by any column
            if (filter.isEmpty() || 
                type.toLowerCase().contains(filter) ||
                String.valueOf(id).contains(filter) ||
                owner.toLowerCase().contains(filter) ||
                balance.toLowerCase().contains(filter) ||
                details.toLowerCase().contains(filter)) {
                tableModel.addRow(new Object[]{type, id, owner, balance, details});
            }
        }
    }

    private JPanel createMonthEndPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(PANEL_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        headerPanel.setBackground(PANEL_COLOR);

        JLabel iconLabel = new JLabel("MONTH END", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        iconLabel.setForeground(DANGER_COLOR);

        JLabel titleLabel = new JLabel("Month-End Processing", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);

        JLabel infoLabel = new JLabel("Preview projected savings interest before processing month-end.", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(TEXT_COLOR);

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);
        headerPanel.add(infoLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        monthEndPreviewArea = new JTextArea();
        monthEndPreviewArea.setEditable(false);
        monthEndPreviewArea.setLineWrap(true);
        monthEndPreviewArea.setWrapStyleWord(true);
        monthEndPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        monthEndPreviewArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane previewScrollPane = new JScrollPane(monthEndPreviewArea);
        previewScrollPane.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(189, 195, 199), 1), "Month-End Preview"));
        mainPanel.add(previewScrollPane, BorderLayout.CENTER);

        JButton previewBtn = createActionButton("Refresh Preview", new Color(52, 73, 94));
        previewBtn.addActionListener(e -> updateMonthEndPreview());
        JButton processBtn = createActionButton("Process Month End", DANGER_COLOR);
        processBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Process month-end for " + getPreviewSavingsCount() + " savings account(s)?",
                "Confirm Month-End Processing",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                runWithLoading("Processing month-end...", () -> {
                    List<Account> savingsAccounts = new ArrayList<>();
                    List<Double> projectedInterest = new ArrayList<>();
                    for (Account account : bank.getAccounts()) {
                        if (account instanceof SavingsAccount) {
                            SavingsAccount savings = (SavingsAccount) account;
                            savingsAccounts.add(savings);
                            projectedInterest.add(savings.getBalance() * savings.getInterestRate());
                        }
                    }

                    bank.processMonthEnd();
                    for (int i = 0; i < savingsAccounts.size(); i++) {
                        savingsAccounts.get(i).addTransaction(getCurrentTimestamp() + " - Month-end interest applied: +$" + String.format("%.2f", projectedInterest.get(i)));
                    }
                    lastTransaction = "Month-end processing completed";
                    addTransactionToHistory(getCurrentTimestamp() + "|" + lastTransaction);
                    saveToCSV();
                    refreshAccountSelectors(false);
                    refreshAccountList();
                    updateMonthEndPreview();
                    updateQuickStats();
                    updateStatus(lastTransaction);
                    JOptionPane.showMessageDialog(this,
                        "Month-end processing complete. Preview has been refreshed with the new balances.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        buttonPanel.add(previewBtn);
        buttonPanel.add(processBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        updateMonthEndPreview();
        return mainPanel;
    }

    private void saveToCSV() {
        try {
            File parent = ACCOUNTS_FILE.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            saveToFile(ACCOUNTS_FILE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving data: " + e.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadFromCSV() {
        if (!ACCOUNTS_FILE.exists()) {
            return;
        }

        try {
            loadFromFile(ACCOUNTS_FILE, true);
            updateQuickStats();
        } catch (IOException e) {
            updateStatus("No saved data loaded.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading data: Invalid format in src/Files/accounts.txt",
                "Load Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default
        }
        
        SwingUtilities.invokeLater(() -> new BankingGUI());
    }
}
