
import java.net.InetSocketAddress;

import rmi.Stub;

public class ClientTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = Integer.parseInt(args[0]);
		InetSocketAddress address = new InetSocketAddress("localhost", 12333);
		FactoryInterface factory = Stub.create(FactoryInterface.class, address);
		try{
			PingpongInterface ping_rmi = factory.makePingpongTest(port);
			String result = "";
			for(int i = 0; i < 4; i++){
				result = ping_rmi.pingpong("ping", i);
				if(result != null) System.out.println(result);
				else System.out.println("input wrong");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
