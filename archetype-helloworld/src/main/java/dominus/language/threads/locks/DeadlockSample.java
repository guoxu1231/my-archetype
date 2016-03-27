package dominus.language.threads.locks;


/**
 * Simple Deadlock Examples
 * http://stackoverflow.com/questions/1385843/simple-deadlock-examples
 * <p/>
 * <p/>
 * Name: Thread-0
 * State: BLOCKED on dominus.language.threads.locks.Account@3337df owned by: Thread-1
 * Total blocked: 1  Total waited: 1
 * Stack trace:
 * dominus.language.threads.locks.Account.transfer(DeadlockSample.java:76)
 * - locked dominus.language.threads.locks.Account@173e7e5
 * dominus.language.threads.locks.DeadlockSample$1.run(DeadlockSample.java:14)
 * <p/>
 * <p/>
 * <p/>
 * Name: Thread-1
 * State: BLOCKED on dominus.language.threads.locks.Account@173e7e5 owned by: Thread-0
 * Total blocked: 1  Total waited: 1
 * <p/>
 * Stack trace:
 * dominus.language.threads.locks.Account.transfer(DeadlockSample.java:76)
 * - locked dominus.language.threads.locks.Account@3337df
 * dominus.language.threads.locks.DeadlockSample$2.run(DeadlockSample.java:21)
 */
public class DeadlockSample {

    public static void main(String[] args) {

        final Account a = new Account(1, 100);
        final Account b = new Account(2, 9900);

        new Thread() {
            @Override
            public void run() {
                Account.transfer(a, b, 50);
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                Account.transfer(b, a, 5000);
            }
        }.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(a);
        System.out.println(b);
    }
}


class Account {
    double balance;
    int id;

    public Account(int id, double balance) {
        this.id = id;
        this.balance = balance;
    }

    void withdraw(double amount) {
        balance -= amount;
    }

    void deposit(double amount) {
        balance += amount;
    }

    static void transfer(Account from, Account to, double amount) {

        System.out.println("Enter [Account].transfer " + Thread.currentThread().getName());

        synchronized (from) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //business process
            synchronized (to) {
                from.withdraw(amount);
                System.out.println("from.withdraw(amount)  " + Thread.currentThread().getName());
                to.deposit(amount);
                System.out.println("to.deposit(amount)  " + Thread.currentThread().getName());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("[ID] %s [Balance] %f$", id, balance);
    }
}
