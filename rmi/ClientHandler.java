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
		//method.getName().equals("toString") && method.getParameterTypes().length == 0 && method.getReturnType().getName().equals("String") &&  method.getParameterTypes()[0].toString().contains("Object")
		if(method.toString().equals("public java.lang.String java.lang.Object.toString()")){
			ClientHandler handler = (ClientHandler) Proxy.getInvocationHandler(proxy);
			
			String res = handler.getMyClass().getName() + " " + handler.getAddress().toString();
			System.out.println(res);
			return res;
		}
		
		if(method.toString().equals("public boolean java.lang.Object.equals(java.lang.Object)")){
			if(args[0] == null) return false;
			ClientHandler handler1 = (ClientHandler) Proxy.getInvocationHandler(proxy);
			try{
				ClientHandler handler2 = (ClientHandler) Proxy.getInvocationHandler(args[0]);
				if(handler1.getMyClass().equals(handler2.getMyClass()) && (handler1.getAddress().equals(handler2.getAddress()))) return true;
				else return false;
			}catch(IllegalArgumentException e){
				return false;
			}
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
		if(ret instanceof NoSuchMethodException){
			throw new RMIException(((NoSuchMethodException) ret).getCause());
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
