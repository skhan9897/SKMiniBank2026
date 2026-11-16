
package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Check whether a column exists in the current database table.
    private boolean hasColumn(Connection con, String table, String column)
            throws SQLException {

        DatabaseMetaData meta = con.getMetaData();

        try (ResultSet rs = meta.getColumns(
                con.getCatalog(), null, table, null)) {

            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }

        return false;
    }

    // Save transaction. Supports tables with or without status column.
    public boolean saveUpiTransaction(
            String accountNumber,
            String customerName,
            String type,
            double amount,
            double balance,
            String description) {

        if (accountNumber == null || accountNumber.trim().isEmpty()
                || type == null || type.trim().isEmpty()
                || !Double.isFinite(amount) || amount <= 0
                || !Double.isFinite(balance) || balance < 0) {
            return false;
        }

        String cleanAcc = accountNumber.trim();

        String sqlWithStatus =
            "INSERT INTO transactions " +
            "(account_number, customer_name, transaction_type, " +
            "amount, balance, description, transaction_date, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";

        String sqlWithoutStatus =
            "INSERT INTO transactions " +
            "(account_number, customer_name, transaction_type, " +
            "amount, balance, description, transaction_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection con = DBConnection.getConnection()) {

            boolean hasStatus = hasColumn(con, "transactions", "status");

            String sql = hasStatus ? sqlWithStatus : sqlWithoutStatus;

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, cleanAcc);
                ps.setString(2,
                    customerName != null ? customerName : "Customer");
                ps.setString(3, type.trim().toUpperCase());
                ps.setDouble(4, amount);
                ps.setDouble(5, balance);
                ps.setString(6,
                    description != null ? description : "Bank Transaction");

                if (hasStatus) {
                    ps.setString(7, "SUCCESS");
                }

                boolean saved = ps.executeUpdate() > 0;

                if (saved) {
                    System.out.println(
                        "Transaction saved successfully: " + cleanAcc);
                }

                return saved;
            }

        } catch (SQLException e) {
            System.err.println("Transaction save failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean addTransaction(Transaction t) {

        if (t == null) return false;

        return saveUpiTransaction(
            t.getAccountNumber(),
            t.getCustomerName(),
            t.getTransactionType(),
            t.getAmount(),
            t.getBalance(),
            t.getDescription()
        );
    }

    public boolean saveTransaction(Transaction t) {
        return addTransaction(t);
    }

    // Fetch all transactions.
    public List<Transaction> getAllTransactions() {
        return fetchTransactions(
            "SELECT * FROM transactions ORDER BY transaction_date DESC"
        );
    }

    // Today's transactions.
    public List<Transaction> getTodayTransactions() {
        return fetchTransactions(
            "SELECT * FROM transactions " +
            "WHERE DATE(transaction_date) = CURDATE() " +
            "ORDER BY transaction_date DESC"
        );
    }

    // Recent transactions.
    public List<Transaction> getRecentTransactions(int limit) {

        if (limit <= 0) return new ArrayList<>();

        String sql =
            "SELECT * FROM transactions " +
            "ORDER BY transaction_date DESC LIMIT ?";

        return fetchTransactions(sql, limit);
    }

    // Account-wise transaction history.
    public List<Transaction> getTransactionsByAccount(
            String accountNumber) {

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanAcc =
            accountNumber.trim().replaceAll("\\s+", "");

        String sql =
            "SELECT * FROM transactions " +
            "WHERE REPLACE(account_number, ' ', '') = ? " +
            "ORDER BY transaction_date DESC";

        return fetchTransactions(sql, cleanAcc);
    }

    // Date-wise account transaction history.
    public List<Transaction> getTransactionsByRange(
            String accountNumber,
            String startDate,
            String endDate) {

        if (accountNumber == null || startDate == null || endDate == null) {
            return new ArrayList<>();
        }

        String cleanAcc =
            accountNumber.trim().replaceAll("\\s+", "");

        String sql =
            "SELECT * FROM transactions " +
            "WHERE REPLACE(account_number, ' ', '') = ? " +
            "AND DATE(transaction_date) BETWEEN ? AND ? " +
            "ORDER BY transaction_date DESC";

        return fetchTransactions(sql, cleanAcc, startDate, endDate);
    }

    // Search transaction by ID.
    public Transaction getTransactionById(int id) {

        if (id <= 0) return null;

        String sql;

        try (Connection con = DBConnection.getConnection()) {

            if (hasColumn(con, "transactions", "transaction_id")) {
                sql = "SELECT * FROM transactions WHERE transaction_id = ?";
            } else if (hasColumn(con, "transactions", "id")) {
                sql = "SELECT * FROM transactions WHERE id = ?";
            } else {
                return null;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapTransaction(rs);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Map database row to Transaction model.
    private Transaction mapTransaction(ResultSet rs)
            throws SQLException {

        Transaction t = new Transaction();

        ResultSetMetaData meta = rs.getMetaData();

        boolean hasId = false;
        boolean hasStatus = false;

        for (int i = 1; i <= meta.getColumnCount(); i++) {

            String name = meta.getColumnLabel(i);

            if ("transaction_id".equalsIgnoreCase(name)
                    || "id".equalsIgnoreCase(name)) {
                if (!hasId) {
                    t.setId(rs.getInt(i));
                    hasId = true;
                }
            }

            if ("status".equalsIgnoreCase(name)) {
                t.setStatus(rs.getString(i));
                hasStatus = true;
            }
        }

        t.setAccountNumber(rs.getString("account_number"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getDouble("amount"));
        t.setBalance(rs.getDouble("balance"));
        t.setDescription(rs.getString("description"));
        t.setTransactionDate(rs.getTimestamp("transaction_date"));

        if (!hasStatus) {
            t.setStatus("SUCCESS");
        }

        return t;
    }

    // Common transaction fetch method.
    private List<Transaction> fetchTransactions(
            String sql, Object... params) {

        List<Transaction> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {

                Object value = params[i];

                if (value instanceof Integer) {
                    ps.setInt(i + 1, (Integer) value);
                } else {
                    ps.setString(i + 1, String.valueOf(value));
                }
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Transaction fetch error: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    // Today's transaction count.
    public int getTotalTransactions() {

        String sql =
            "SELECT COUNT(*) FROM transactions " +
            "WHERE DATE(transaction_date) = CURDATE()";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Total customer balance.
    public double getTotalBalance() {

        String sql = "SELECT COALESCE(SUM(balance), 0) FROM customer";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // Check whether status column exists.
    private boolean supportsStatus(Connection con) throws SQLException {
        return hasColumn(con, "transactions", "status");
    }

    // Calculate transaction sums.
    private double getSum(String condition) {

        String sql =
            "SELECT COALESCE(SUM(amount), 0) " +
            "FROM transactions WHERE " + condition;

        try (Connection con = DBConnection.getConnection()) {

            if (supportsStatus(con)) {
                sql += " AND status = 'SUCCESS'";
            }

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // Total deposits.
    public double getTotalDeposit() {
        return getSum(
            "UPPER(transaction_type) = 'DEPOSIT'"
        );
    }

    // Total withdrawals.
    public double getTotalWithdraw() {
        return getSum(
            "UPPER(transaction_type) IN ('WITHDRAWAL', 'WITHDRAW')"
        );
    }

    // Total transfers.
    // Counts debit-side transfer records to avoid counting
    // both debit and credit entries for the same transfer.
    public double getTotalTransfer() {
        return getSum(
            "UPPER(transaction_type) IN ('DEBIT', 'TRANSFER')"
        );
    }
}
