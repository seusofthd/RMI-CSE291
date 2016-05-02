

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;

import rmi.RMIException;
import rmi.Skeleton;
import rmi.Stub;

public class FactoryImpl implements FactoryInterface, Serializable {
	HashMap<Integer, Skeleton> skeletons = new HashMap<Integer, Skeleton>();
	
	@Override
	public PingpongInterface pingpongTest(int port) throws RMIException {
		// TODO Auto-generated method stub
		InetSocketAddress address = new InetSocketAddress(port);
		PingpongImpl pingpong = new PingpongImpl();
		Skeleton<PingpongInterface> skeleton = new Skeleton(PingpongInterface.class, pingpong, address);
		skeleton.start();
		skeletons.put(port, skeleton);
		PingpongInterface pingpong_remote = Stub.create(PingpongInterface.class, skeleton, "server");
		return pingpong_remote;
	}

	@Override
	public void closePingpongTest(int port) throws RMIException {
		// TODO Auto-generated method stub
		skeletons.get(port).stop();
	}
	
}
