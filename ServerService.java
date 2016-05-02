

import java.net.InetSocketAddress;

import rmi.Skeleton;

public class ServerService {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		InetSocketAddress address = new InetSocketAddress(12333);
		FactoryImpl factory = new FactoryImpl();
		Skeleton<FactoryInterface> skeleton = new Skeleton(FactoryInterface.class, factory, address);
		try{
			skeleton.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
