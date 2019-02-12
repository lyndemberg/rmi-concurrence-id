import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IdentityService extends Remote {
    int getId(String appInstance) throws RemoteException;
}
