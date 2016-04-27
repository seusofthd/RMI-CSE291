import rmi.*;

public class PingpongImpl implements PingpongInterface
{
	public String ping(int id) throws RMIException{
		return "Pong " + id;
	}
}
