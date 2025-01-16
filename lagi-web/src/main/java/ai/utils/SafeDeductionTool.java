package ai.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SafeDeductionTool {

    private static final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    @Getter
    private static class Account {
        @Setter
        private double balance;
        private final Lock lock = new ReentrantLock();

        public Account(double initialBalance) {
            this.balance = initialBalance;
        }

    }

    public static void createAccount(String accountId, double initialBalance) {
        accounts.putIfAbsent(accountId, new Account(initialBalance));
    }

    public static void removeAccount(String accountId) {
        accounts.remove(accountId);
    }


    public static boolean deduct(String accountId, double amount) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }

        Lock lock = account.getLock();
        lock.lock();
        try {
            if (account.getBalance() >= amount) {
                account.setBalance(account.getBalance() - amount);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public static double getBalance(String accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }

        Lock lock = account.getLock();
        lock.lock();
        try {
            return account.getBalance();
        } finally {
            lock.unlock();
        }
    }



}
