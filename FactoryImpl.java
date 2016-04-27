

import java.io.Serializable;
import java.net.InetSocketAddress;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

public class FactoryImpl implements FactoryInterface, Serializable {

	@Override
	public PingpongInterface makePingpongTest(int port) throws RMIException {
		// TODO Auto-generated method stub
		InetSocketAddress address = new InetSocketAddress(port);
		PingpongImpl pingpong = new PingpongImpl();
		Skeleton<PingpongInterface> skeleton = new Skeleton(PingpongInterface.class, pingpong, address);
		skeleton.start();
		PingpongInterface pingpong_remote = Stub.create(PingpongInterface.class, skeleton, "localhost");
		return pingpong_remote;
	}

}
