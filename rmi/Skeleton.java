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


/**
 * RMI skeleton
 * 
 * <p>
 * A skeleton encapsulates a multithreaded TCP server. The server's clients are intended to be RMI stubs created using the
 * <code>Stub</code> class.
 * 
 * <p>
 * The skeleton class is parametrized by a type variable. This type variable should be instantiated with an interface. The
 * skeleton will accept from the stub requests for calls to the methods of this interface. It will then forward those requests to
 * an object. The object is specified when the skeleton is constructed, and must implement the remote interface. Each method in
 * the interface should be marked as throwing <code>RMIException</code>, in addition to any other exceptions that the user
 * desires.
 * 
 * <p>
 * Exceptions may occur at the top level in the listening and service threads. The skeleton's response to these exceptions can be
 * customized by deriving a class from <code>Skeleton</code> and overriding <code>listen_error</code> or
 * <code>service_error</code>.
 */
public class Skeleton<T> {
    private Class<T> sclass = null;
    private T server = null;
    private InetSocketAddress sockAddr;
    private ListenThread listenThread;

    /**
     * Creates a <code>Skeleton</code> with no initial server address. The address will be determined by the system when
     * <code>start</code> is called. Equivalent to using <code>Skeleton(null)</code>.
     * 
     * <p>
     * This constructor is for skeletons that will not be used for bootstrapping RMI - those that therefore do not require a
     * well-known port.
     * 
     * @param c
     *            An object representing the class of the interface for which the skeleton server is to handle method call
     *            requests.
     * @param server
     *            An object implementing said interface. Requests for method calls are forwarded by the skeleton to this object.
     * @throws Error
     *             If <code>c</code> does not represent a remote interface - an interface whose methods are all marked as throwing
     *             <code>RMIException</code>.
     * @throws NullPointerException
     *             If either of <code>c</code> or <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server) {
        // error-checking; nullpointer and remote interface
        if (c == null || server == null) {
            throw new NullPointerException();
        }
        Method[] mthds = c.getDeclaredMethods();
        for (Method mthd : mthds) {
            Class[] exceptions = mthd.getExceptionTypes();
            if (!(Arrays.asList(exceptions).contains(RMIException.class)) || !(c.isInterface())) {
                throw new Error("C does not represent a remote interface");
            }
        }
        // creates skeleton
        sclass = c;
        this.server = server;
        sockAddr = new InetSocketAddress(12345);
    }

    /**
     * Creates a <code>Skeleton</code> with the given initial server address.
     * 
     * <p>
     * This constructor should be used when the port number is significant.
     * 
     * @param c
     *            An object representing the class of the interface for which the skeleton server is to handle method call
     *            requests.
     * @param server
     *            An object implementing said interface. Requests for method calls are forwarded by the skeleton to this object.
     * @param address
     *            The address at which the skeleton is to run. If <code>null</code>, the address will be chosen by the system when
     *            <code>start</code> is called.
     * @throws Error
     *             If <code>c</code> does not represent a remote interface - an interface whose methods are all marked as throwing
     *             <code>RMIException</code>.
     * @throws NullPointerException
     *             If either of <code>c</code> or <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address) {
        // error-checking; nullpointer and remote interface
        if (c == null || server == null) {
            throw new NullPointerException();
        }
        Method[] mthds = c.getDeclaredMethods();
        for (Method mthd : mthds) {
            Class[] exceptions = mthd.getExceptionTypes();
            if (!(Arrays.asList(exceptions).contains(RMIException.class)) || !(c.isInterface())) {
                throw new Error("C does not represent a remote interface");
            }
        }
        // creates skeleton
        sclass = c;
        this.server = server;
        sockAddr = address;
    }

    /**
     * Called when the listening thread exits.
     * 
     * <p>
     * The listening thread may exit due to a top-level exception, or due to a call to <code>stop</code>.
     * 
     * <p>
     * When this method is called, the calling thread owns the lock on the <code>Skeleton</code> object. Care must be taken to
     * avoid deadlocks when calling <code>start</code> or <code>stop</code> from different threads during this call.
     * 
     * <p>
     * The default implementation does nothing.
     * 
     * @param cause
     *            The exception that stopped the skeleton, or <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause) {
        if (cause != null) {
            cause.printStackTrace();
        }
    }

    /**
     * Called when an exception occurs at the top level in the listening thread.
     * 
     * <p>
     * The intent of this method is to allow the user to report exceptions in the listening thread to another thread, by a
     * mechanism of the user's choosing. The user may also ignore the exceptions. The default implementation simply stops the
     * server. The user should not use this method to stop the skeleton. The exception will again be provided as the argument to
     * <code>stopped</code>, which will be called later.
     * 
     * @param exception
     *            The exception that occurred.
     * @return <code>true</code> if the server is to resume accepting connections, <code>false</code> if the server is to shut
     *         down.
     */
    protected boolean listen_error(Exception exception) {
        return false;
    }

    /**
     * Called when an exception occurs at the top level in a service thread.
     * 
     * <p>
     * The default implementation does nothing.
     * 
     * @param exception
     *            The exception that occurred.
     */
    protected void service_error(RMIException exception) {
        if (!exception.getClass().equals(EOFException.class)) {
            exception.printStackTrace();
        }
    }

    /**
     * Starts the skeleton server.
     * 
     * <p>
     * A thread is created to listen for connection requests, and the method returns immediately. Additional threads are created
     * when connections are accepted. The network address used for the server is determined by which constructor was used to
     * create the <code>Skeleton</code> object.
     * 
     * @throws RMIException
     *             When the listening socket cannot be created or bound, when the listening thread cannot be created, or when the
     *             server has already been started and has not since stopped.
     */
    public synchronized void start() throws RMIException {
        // check for conditions to throw RMIException
        if ((listenThread != null) && listenThread.thread.isAlive()) {
            throw new RMIException("Server has already been started and has not since stopped.");
        } else {
            try {
                listenThread = new ListenThread(new ServerSocket(sockAddr.getPort()), server);
                listenThread.start();
            } catch (Exception e) {
                throw new RMIException(
                        "Listening socket could not be created or bound, or listening thread could not be created.");
            }
        }
    }

    /**
     * Stops the skeleton server, if it is already running.
     * 
     * <p>
     * The listening thread terminates. Threads created to service connections may continue running until their invocations of the
     * <code>service</code> method return. The server stops at some later time; the method <code>stopped</code> is called at that
     * point. The server may then be restarted.
     */
    public synchronized void stop() {
        // set run state to false and close socket connection
        if (listenThread != null && listenThread.thread.isAlive()) {
            listenThread.runState = false;
            try {
                if (listenThread.serverSocket != null && !listenThread.serverSocket.isClosed()) {
                    listenThread.serverSocket.close();
                }
            } catch (Exception e) {
                listen_error(e);
            }
            // waits for thread to terminate with or without exception
            try {
                listenThread.thread.join();
                stopped(null);
            } catch (Exception e) {
                stopped(e);
            }
        }
    }

    public InetSocketAddress getSockAddr(){
    	return sockAddr;
    }
    
    public ListenThread getListenThread(){
    	return listenThread;
    }
    
    public class ListenThread implements Runnable{
    	public volatile boolean runState;//true indicates that it starts acceptting TCP requests
    	private ServerSocket serverSocket;
    	private Socket clientSockHandler;
    	private Thread thread;
    	private T server;
    	public ListenThread(ServerSocket svSocket, T server) throws RMIException {
    		serverSocket = svSocket;
    		runState = true;
    		this.server = server;
    	}
    	
		@Override
		public synchronized void run() {
			// TODO Auto-generated method stub
			while(runState){
				try{
					clientSockHandler = serverSocket.accept();
//					System.out.println("Skeleton accept one client connection:"+  clientSockHandler.getRemoteSocketAddress().toString() + ":" + clientSockHandler.getPort());
					ServerHandler serverHandler = new ServerHandler(clientSockHandler, server);
					serverHandler.start();
				}catch(IOException e){
//					System.out.println("***asdaexception");
					if(runState) listen_error(e);
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

		
		public synchronized boolean getRunState(){
			if (runState) return true;
			else return false;
		}
		
		public synchronized void setRunState(boolean flag){
			runState = flag;
		}
    }
    
    
    public class CommunicationThread implements Runnable{
    	private Socket clientSocket;
    	private Thread thread;
    	
    	public CommunicationThread(Socket cSocketHandler){
    		this.clientSocket = cSocketHandler;
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
			
//			System.out.println("client thread handling"+clientSocket.getPort());
		}
		
		public void start(){
			if(thread == null){
				thread = new Thread(this);
				thread.start();
			}
		}
	}



}
