package rmi;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;


public class ListenThread<T> implements Runnable {

   	public volatile boolean runState;//true indicates that it starts acceptting TCP requests
	private ServerSocket serverSocket;
	private Socket clientSockHandler;
	private Thread thread;
	private T server;
	private Class<T> myClass;
	private Skeleton skeleton;
	public ListenThread(Skeleton skeleton, ServerSocket svSocket, T server, Class<T> myClass) throws RMIException {
		serverSocket = svSocket;
		runState = true;
		this.server = server;
		this.skeleton = skeleton;
		this.myClass = myClass;
	}
	
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		while(runState){
			try{
				clientSockHandler = serverSocket.accept();
//    				System.out.println("Skeleton accept one client connection:"+  clientSockHandler.getRemoteSocketAddress().toString() + ":" + clientSockHandler.getPort());
				CommunicationThread serverHandler = new CommunicationThread(skeleton, clientSockHandler, server, myClass);
				serverHandler.start();
			}catch(IOException e){
//    				System.out.println("***asdaexception");
				if(runState) skeleton.listen_error(e);
			}
		}
	}
	
	public synchronized void start(){
		if(thread == null){
			thread = new Thread(this);
			runState = true;
			thread.start();
			
		}
	}
	
	public synchronized void stop() {
		runState = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Thread getThread(){
		return thread;
	}

	public ServerSocket getSocket(){
		return serverSocket;
	}
	
	public synchronized boolean getRunState(){
		if (runState) return true;
		else return false;
	}
	
	public synchronized void setRunState(boolean flag){
		runState = flag;
	}


    public class CommunicationThread<T> implements Runnable{
        private Socket clientSocket;
        private Thread thread;
        private T server;
        private Skeleton skeleton;
        private Class<T> myClass;
        public CommunicationThread(Skeleton skeleton, Socket cSocketHandler, T server, Class<T> myClass){
            this.clientSocket = cSocketHandler;
            this.server = server;
            this.skeleton = skeleton;
            this.myClass = myClass;
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
                        Method mthd = myClass.getMethod(method.name, method.paramTypes);
                        mthd.setAccessible(true);
                        retObj = mthd.invoke(server, method.parameters);
                    } catch (Exception e) {
                        if(e instanceof NoSuchMethodException){
                            out.writeObject(e);
                            skeleton.service_error(new RMIException("Interface not found"));
                        }else if (e instanceof EOFException || e instanceof SocketException) {
                        } else if(e instanceof InvocationTargetException){
                            out.writeObject(e);
                        }  else{
                            skeleton.service_error(new RMIException("Exception thrown in service response."));
                        }
                    }
                    out.writeObject(retObj);
                    in.close();
                    out.close();
                    clientSocket.close();
                }
            } catch (ClassNotFoundException e){
                skeleton.service_error(new RMIException(e.getCause()));
            } catch (SecurityException e) {
                skeleton.service_error(new RMIException(e.getCause()));
            } catch(IOException e) {
                skeleton.service_error(new RMIException(e.getCause()));
            }
        }
        
        public void start(){
            if(thread == null){
                thread = new Thread(this);
                thread.start();
            }
        }
    }

}


