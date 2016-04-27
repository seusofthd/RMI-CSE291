import rmi.*;

public interface PingpongInterface {
	public String ping(int id) throws RMIException;
}
