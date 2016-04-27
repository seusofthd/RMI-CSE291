

import java.io.Serializable;

import rmi.RMIException;

public interface FactoryInterface extends Serializable{
	PingpongInterface pingpongTest(int port) throws RMIException;
	void closePingpongTest(int port) throws RMIException;
}
