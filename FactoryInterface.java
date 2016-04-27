

import java.io.Serializable;

import rmi.RMIException;

public interface FactoryInterface extends Serializable{
	PingpongInterface makePingpongTest(int port) throws RMIException;
}
