import rmi.*;
import java.net.*;

public class PingpongServer
{
	public static void main(String[] args){
		PingpongImpl server = new PingpongImpl();
		InetSocketAddress address = new InetSocketAddress(7000);
		Skeleton<PingpongInterface> skeleton = new Skeleton<PingpongInterface>(PingpongInterface.class, server, address);
		try{
			skeleton.start();
		}catch(Throwable e){
			System.out.println("skeleton start failed.");
		}
	}
}
