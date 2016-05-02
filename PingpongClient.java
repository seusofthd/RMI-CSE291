import java.net.*;
import rmi.*;

public class PingpongClient
{
	public static void main(String[] args){
		String hostname = "server";
		int port = 7000;
		InetSocketAddress address = new InetSocketAddress(hostname, port);
		PingpongInterface remote_server = Stub.create(PingpongInterface.class, address);
		try{
			String ping_result = remote_server.ping(123);
			System.out.println(ping_result);
		}catch(RMIException e){
			e.printStackTrace();
		}
	}
}
