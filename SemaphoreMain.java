package th.ac.crru.ce.os;

import java.util.concurrent.Semaphore;

/**
 * Created by Anusorn on 3/7/2015.
 *
 */

class Account {
    private String AccountNumber;
    private double AccountBalance;
    Semaphore semaphore = new Semaphore(1);

    public String getAccountNumber() {
        return AccountNumber;
    }

    public double getAccountBalance() {
        return AccountBalance;
    }

    public Account(String AccountNumber) {
        this.AccountNumber = AccountNumber;
    }

    // critical section
    public boolean depositAmount(double amount) {
        if (amount < 0) {
            return false;
        } else {
            semaphore.acquireUninterruptibly();
            AccountBalance = AccountBalance + amount;
            semaphore.release();
            return true;
        }
    }

    // critical section
    public boolean withdrawAmount(double amount) {
        if (amount > AccountBalance) {
            return false;
        } else {
            semaphore.acquireUninterruptibly();
            AccountBalance = AccountBalance - amount;
            semaphore.release();
            return true;
        }
    }
}

class Transaction extends Thread {

    public static enum TransactionType {
        DEPOSIT_MONEY(1), WITHDRAW_MONEY(2);

        private TransactionType(int value) {
        }
    }

    ;

    private TransactionType transactionType;
    private Account Account;
    private double Amount;

    /*
    * If transactionType == 1, depositAmount() else if transactionType == 2 withdrawAmount()
    */
    public Transaction(Account account, TransactionType transactionType, double Amount) {
        this.transactionType = transactionType;
        this.Account = account;
        this.Amount = Amount;
    }

    public void run() {
        switch (this.transactionType) {
            case DEPOSIT_MONEY:
                 depositAmount();
                printBalance();
                break;
            case WITHDRAW_MONEY:
                 withdrawAmount();
                 printBalance();
                break;
            default:
                System.out.println("NOT A VALID TRANSACTION");
        }
    }

    public void depositAmount() {
        this.Account.depositAmount(this.Amount);
    }

    public void withdrawAmount() {
        this.Account.withdrawAmount(Amount);
    }

    public void printBalance() {
        System.out.println(Thread.currentThread().getName() + " : TransactionType: " + this.transactionType + ", Amount: " + this.Amount);
        System.out.println("New Account Balance: " + this.Account.getAccountBalance());
    }
}


public class SemaphoreMain {
    public static void main(String args[]) {
        Account account = new Account("AccountNumber");

        // Total Expected Deposit: 10000 (100 x 100)
        for (int i = 0; i < 100; i++) {
            Transaction t = new Transaction(account, Transaction.TransactionType.DEPOSIT_MONEY, 100);
            System.out.println("Start Deposit Threads No. " + i);
            t.start();
        }

        // Total Expected Withdrawal: 5000 (100 x 50)
        for (int i = 0; i < 100; i++) {
            Transaction t = new Transaction(account, Transaction.TransactionType.WITHDRAW_MONEY, 50);
            System.out.println("Start Withdrawal Threads No. " + i);
            t.start();

        }

        // Let's just wait for a second to make sure all thread execution completes.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        // Expected account balance is 5000
        System.out.println("\n Final Account Balance: " + account.getAccountBalance());
    }
}
