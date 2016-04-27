package rmi;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.lang.reflect.Proxy;

public class ClientHandler<T> implements InvocationHandler, Serializable{
	private InetSocketAddress address;
	private Class<T> c;
	public ClientHandler(InetSocketAddress address, Class<T> c){
		this.address = address;
//		System.out.println("ClientHandler" + this.address.getPort() + ":" + this.address.getHostName().toString());
		this.c = c;
	}
	
	public InetSocketAddress getAddress(){
		return address;
	}
	
	public Class<T> getMyClass(){
		return c;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// TODO Auto-generated method stub
		//before remote invokation, we should deal with the several method invocation cases like: hashcode, toString and equals
		if(method.getName().equals("hashCode") && method.getParameterTypes().length == 0){
			ClientHandler handler = (ClientHandler) Proxy.getInvocationHandler(proxy);
			return handler.address.hashCode();
		}
		
		if(method.getName().equals("toString") && method.getParameterTypes().length == 0 && method.getReturnType().getName().equals("String")){
			ClientHandler handler = (ClientHandler) Proxy.getInvocationHandler(proxy);
			return "Class info:" + handler.getMyClass().toString() + " Network info:" + handler.getAddress().getHostName() + ":" + handler.getAddress().getPort();
		}
		if(method.getName().equals("equals") && method.getParameterTypes().length == 1 && method.getReturnType().getName().equals("boolean")){
			if(args.length != 1) return false;
			if(args[0] == null) return false;
			ClientHandler handler1 = (ClientHandler) Proxy.getInvocationHandler(proxy);
			ClientHandler handler2 = (ClientHandler) Proxy.getInvocationHandler(args[0]);
			if(handler1.getClass().equals(handler2.getClass()) && (handler1.getAddress().equals(handler2.getAddress()))) return true;
			else return false;
			
		}
		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		
		try{
			socket = new Socket(address.getHostName(), address.getPort());
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
			SerializedMethod serialMethed = new SerializedMethod(method, args);
			out.writeObject(serialMethed);
		}catch(Exception e){
			throw new RMIException("error happened when establishing connections and transmitting methods");
		}
		Object ret = null;
		ret = in.readObject();

		if(ret instanceof InvocationTargetException){
			throw ((InvocationTargetException) ret).getTargetException();
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch(Exception e){
			throw new RMIException("Client connection exception happened");
		}
		return ret;
	}

}
