import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Loader {

    private static String INSTANCE_APP = "App1";
    private static int CAPACITY_QUEUES = 3;
    private static IdentityService identityService = null;
    private static final UserDao userDao = new UserDao();

    private static Lock lockExecute = new ReentrantLock();
    private static Condition executeCondition = lockExecute.newCondition();

    private static int option = 0;
    private static boolean run = true;

    private static int COUNT = 1;
    private static final int LIMIT = 1000;
    private static int LAST;
    private static volatile int COUNT_PIPES_EXECUTED = 0;
    private static int COUNT_PIPES_AUX = 0;


    public static void main(String[] args) throws InterruptedException {
        System.out.println(INSTANCE_APP + " running");
        try {
            Registry registry = LocateRegistry.getRegistry();
            identityService = (IdentityService) registry.lookup("IdentityService");
        } catch (RemoteException | NotBoundException e) {
            new RuntimeException("Unable to communicate with identity manager");
        }

        //  scanner thread
        Runnable runnableScanner = new Runnable() {
            private Scanner sc = new Scanner(System.in);

            public void run() {
                System.out.println("CHANGE: ");
                System.out.println("1- PAUSE");
                System.out.println("2- CONTINUE");
                while (true) {
                    option = sc.nextInt();
                    System.out.println("YOU SELECTED " + option);
                    //
                    lockExecute.lock();
                    if(option == 1) {
                        run = false;
                    }else if(option == 2) {
                        run = true;
                        executeCondition.signalAll();
                    }
                    lockExecute.unlock();
                    //
                }
            }
        };
        Thread threadScanner = new Thread(runnableScanner);
        threadScanner.start();
        //
        final BlockingQueue<Integer> queueInsert = new ArrayBlockingQueue<Integer>(CAPACITY_QUEUES);
        final BlockingQueue<Integer> queueUpdate = new ArrayBlockingQueue<Integer>(CAPACITY_QUEUES);
        final BlockingQueue<Integer> queueDelete = new ArrayBlockingQueue<Integer>(CAPACITY_QUEUES);

        final long t0 = System.currentTimeMillis();
        while (COUNT<=LIMIT) {

            //
            lockExecute.lock();
            while(!run){
                executeCondition.await();
            }
            lockExecute.unlock();
            //

            try {
                COUNT = identityService.getId(INSTANCE_APP);
            } catch (RemoteException e) {
                new RuntimeException("Could not retrieve id");
            }

            if(COUNT>LIMIT){
                break;
            }else{
                COUNT_PIPES_AUX++;
            }

            queueInsert.put(COUNT);

            Runnable runnableInsert = () -> {
                try {
                    Integer take = queueInsert.take();
                    User user = new User(take, "JoãoDeDeus");
                    userDao.insert(user);
                    queueUpdate.put(take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable runnableUpdate = () -> {
                try {
                    Integer take = queueUpdate.take();
                    userDao.update(take);
                    queueDelete.put(take);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            Runnable runnableDelete = () -> {
                try {
                    Integer take = queueDelete.take();
                    userDao.delete(take);
                    COUNT_PIPES_EXECUTED++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };

            Thread threadInsert = new Thread(runnableInsert);
            Thread threadUpdate = new Thread(runnableUpdate);
            Thread threadDelete = new Thread(runnableDelete);

            threadInsert.start();
            threadUpdate.start();
            threadDelete.start();

        }

        while(true){
            if(COUNT>LIMIT && COUNT_PIPES_EXECUTED==COUNT_PIPES_AUX){
                imprimirTempo(t0);
                break;
            }
        }

        threadScanner.interrupt();

    }

    private static void imprimirTempo(long inicio){
        long t1 = System.currentTimeMillis();
        long tempoTotal = t1 - inicio;
        System.out.println("APP 1" + " - Durou: " + tempoTotal);
    }
//
//    private static int retrieveId(){
//        int id = -1;
//        try {
//            id =
//        } catch (RemoteException e) {
//
//        }
//        return id;
//    }

}
