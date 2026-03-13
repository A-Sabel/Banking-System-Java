# Banking Management System (Java Swing)

A Java Swing banking application focused on object-oriented design and practical account operations.

## Project Overview

This project manages savings and checking accounts through a desktop GUI. It supports common banking operations such as account creation, deposits, withdrawals, transfers, account maintenance, and month-end processing.

The codebase demonstrates key OOP concepts:

- Abstraction (`Account` as an abstract base class)
- Inheritance (`SavingsAccount`, `CheckingAccount`)
- Polymorphism (`monthlyProcess()` and overridden `withdraw()` behavior)
- Encapsulation (private fields with controlled access)

## Tech Stack

- Language: Java 17
- UI: Java Swing
- Data storage: local text/CSV-style files
- Concepts: OOP (abstraction, inheritance, polymorphism, encapsulation), event-driven UI, input validation

## Current Features

### Core Account Operations

- Open savings account with configurable interest rate
- Open checking account with configurable transaction fee
- Deposit funds
- Withdraw funds (checking accounts include fee in deduction)
- Transfer funds between accounts
- Process month-end interest for savings accounts

### Account Management

- View all accounts in a sortable table
- Search/filter accounts by ID, owner, type, balance, or details
- Edit selected account fields (owner, balance, interest rate/fee)
- Delete selected account

### Transaction Visibility

- Recent transaction panel on home screen
- Per-account transaction history viewer
- Last transaction indicator in dashboard

### Persistence and Data Tools

- Auto-save account data to `src/Files/accounts.txt`
- Auto-save transaction log to `src/Files/transactionss.txt`
- Export account data to CSV
- Backup/restore accounts from file chooser

### UI/UX Features

- CardLayout navigation for stable form switching
- Status bar feedback
- Color-coded action buttons
- Input placeholders and live validation visuals
- Clear buttons and Enter-key form submission support

## Project Structure

```
Banking-System-Java/
├── src/
│   ├── Account.java
│   ├── Bank.java
│   ├── BankingGUI.java
│   ├── CheckingAccount.java
│   ├── SavingsAccount.java
│   └── Files/
│       ├── accounts.txt
│       └── transactionss.txt
└── README.md
```

## Class Summary

### `Account` (abstract)

- Stores account ID, owner name, balance, and limited transaction history
- Provides shared behavior: `deposit`, `withdraw`, validation helpers
- Declares abstract `monthlyProcess()`

### `SavingsAccount`

- Adds `interestRate`
- `monthlyProcess()` applies interest using:

$$
\mathrm{newBalance} = \mathrm{balance} + (\mathrm{balance} \times \mathrm{interestRate})
$$

### `CheckingAccount`

- Adds `transactionFee`
- Overrides `withdraw(amount)` to deduct `amount + transactionFee`

### `Bank`

- Manages in-memory list of accounts
- Opens accounts, finds accounts by ID, performs transfers
- Executes month-end processing for all accounts
- Supports account deletion and list retrieval

### `BankingGUI`

- Swing application entry point (`main`)
- Builds all operation views and menu navigation
- Handles validation, transaction recording, and persistence calls

## Validation Rules

- Owner name:
  - 2 to 50 characters
  - Must start with a letter
  - Allowed chars: letters, spaces, apostrophes, periods, hyphens
- Interest rate range: `0.00` to `0.25`
- Transaction fee range: `0.00` to `1000.00`
- Monetary actions must use positive values
- Transfers to same account are blocked
- Withdrawals fail on insufficient balance (including checking fee)

## Requirements

- JDK 17 or higher
- Windows, macOS, or Linux terminal

## Build and Run

### Compile

```bash
javac src/*.java
```

### Run GUI

```bash
java -cp src BankingGUI
```

Note: The current workspace version is GUI-focused. A CLI launcher class is not present in this repository snapshot.

## Usage Quick Guide

1. Launch the app and use top navigation buttons.
2. Create at least one account (Savings or Checking).
3. Use Deposit/Withdraw/Transfer for transactions.
4. Open Accounts view to search, edit, delete, export, backup, or restore.
5. Open Month End to preview and apply savings interest.

## Error Handling

The app displays clear dialog messages for invalid input and operational issues, including:

- Invalid numeric input
- Invalid owner names
- Missing account selections
- Insufficient funds
- File load/save/export/restore errors

## Known Notes

- Data is stored in plain text/CSV-like format under `src/Files`.

## Future Improvements

- Optional database storage (SQLite/MySQL)
- Authentication and user roles
- Printable account statements
- Unit tests for banking rules

## License

Educational project for academic use.
