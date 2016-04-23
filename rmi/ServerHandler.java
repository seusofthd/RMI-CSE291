package rmi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;

public class ServerHandler<T> implements Runnable{
	private Socket clientSocket;
	private Thread thread;
	T server;
	public ServerHandler(Socket cSocketHandler, T server){
		this.clientSocket = cSocketHandler;
		this.server = server;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		ObjectInputStream in;
		ObjectOutputStream out;
		
		try{
			if(clientSocket != null && !clientSocket.isClosed()){
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(clientSocket.getInputStream());            
				
				SerializedMethod method = (SerializedMethod) in.readObject();
				Object retObj = null;
                try {
                	Method mthd = server.getClass().getMethod(method.name, method.paramTypes);
			           	retObj = mthd.invoke(server, method.parameters);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e.getClass().equals(EOFException.class) || e.getClass().equals(SocketException.class)) {
	                } else if(e.getClass().equals(InvocationTargetException.class)){
	                	out.writeObject(e);;
	                	return;
	                }else{
	                    e.printStackTrace();
	                    service_error(new RMIException("Exception thrown in service response."));
	                }
				}
                out.writeObject(retObj);
				in.close();
				out.close();
				clientSocket.close();
			}
		} catch (ClassNotFoundException e){
			service_error(new RMIException(e.getCause()));
		} catch (SecurityException e) {
			service_error(new RMIException(e.getCause()));
		} catch(IOException e) {}
		
//		System.out.println("client thread handling"+clientSocket.getPort());
	}
	
	public void start(){
		if(thread == null){
			thread = new Thread(this);
			thread.start();
		}
	}
	
    protected void service_error(RMIException exception) {
        if (!exception.getClass().equals(EOFException.class)) {
            exception.printStackTrace();
        }
    }
}
