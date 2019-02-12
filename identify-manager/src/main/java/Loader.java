import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Loader {

    public static void main(String[] args) throws RemoteException {
        
        IdentityService server = new IdentityServiceImpl();
        IdentityService stub = (IdentityService) UnicastRemoteObject.exportObject(server,0);
        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("IdentityService",stub);

    }


}

