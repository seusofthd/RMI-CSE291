
import java.io.Serializable;

import rmi.RMIException;

public class PingpongImpl implements PingpongInterface, Serializable{

	@Override
	public String pingpong(String ping, int num) throws RMIException{
		// TODO Auto-generated method stub
		if(ping.equals("ping")){
			return "pong " + num;
		}else return null;
	}

}
