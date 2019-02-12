import java.rmi.RemoteException;

public class IdentityServiceImpl implements IdentityService{
    private int id = 1;
    Object sync = new Object();

    public int getId(String appInstance) throws RemoteException {
        synchronized (sync){
            int value = id++;
            System.out.println("APP INSTANCE: " + appInstance + " retrieve ID: " + value);
            return value;
        }
    }
}
