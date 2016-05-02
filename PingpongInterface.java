import java.io.Serializable;

import rmi.RMIException;

public interface PingpongInterface extends Serializable{
	public String pingpong(String ping, int num) throws RMIException;
}
