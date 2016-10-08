import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import java.io.*;


//Author: Syed Taqi Raza

public class myfileserver extends Thread implements Runnable {  
 public static synchronized int getSuccReq() { // setters and getters
		return succReq;
	}

	public static synchronized void setSuccReq(int succReq) {
		myfileserver.succReq = succReq;
	}

	public static synchronized int getTotalReq() {
		return totalReq;
	}

	public static synchronized void setTotalReq(int totalReq) {
		myfileserver.totalReq = totalReq;
	}

private Thread t;
	public static int succReq = 0; 
	public static int totalReq = 1;
	public static  int workers = 0;
//	private static String fileFolder = "C:/Testingdir/";
	public static String fileReq = null;
	boolean checkExist = false;
	public myfileserver(int port) {	//basic constructor, will be called 
		try {
	ServerSocket	SrvSck = new ServerSocket(port);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public myfileserver(Socket ConnectedClient) {
		BufferedReader InFromClient = null;
		try {
			InFromClient = (new BufferedReader(new InputStreamReader(ConnectedClient.getInputStream())));
			fileReq = InFromClient.readLine(); // the only info we get from client is the filename
			System.out.println(fileReq + " Requested from "+ ConnectedClient.getInetAddress());

	checkExist = new File( fileReq).isFile(); //checks if the thing exists
	if (checkExist == false)
	{
		
		int b = 0; // used to tell the client whether or not it found it
		  System.out.println("REQ " + getTotalReq() + ":"+ " Not successful");
		
		
		  PrintWriter reply = new PrintWriter(ConnectedClient.getOutputStream());
		OutputStream os = ConnectedClient.getOutputStream();
		os.write(b);
		os.flush();
		sendStats(os);

		ConnectedClient.close();
		workers--;
		
	}
	else 
	{
		
		int b = 1;  /// this is used to tell the client if the file existed or not
		OutputStream oss = ConnectedClient.getOutputStream();
		oss.write(b);
		oss.flush();
		 File myFile = new File(fileReq);
         byte[] mybytearray = new byte[(int) myFile.length()];
//    	 System.out.println("File "+myFile+" sent to client.");
         FileInputStream fis = new FileInputStream(myFile);
         BufferedInputStream bis = new BufferedInputStream(fis);
         //bis.read(mybytearray, 0, mybytearray.length);

         DataInputStream dis = new DataInputStream(bis);
         dis.readFully(mybytearray, 0, mybytearray.length);

         //handle file send over socket
         OutputStream os = ConnectedClient.getOutputStream();

         //Sending file name and file size to the server
         DataOutputStream dos = new DataOutputStream(os);
         dos.writeUTF(myFile.getName());
         dos.writeLong(mybytearray.length);
         dos.write(mybytearray, 0, mybytearray.length);
         dos.flush();
		 succReq++;
//		os.write();
		  System.out.println("REQ " + getTotalReq() + ":"+ " successful");
			
		os.flush();
		sendStats(os); /// this is the function call to send the stats
		
		ConnectedClient.close();
		fis.close();
		workers--;
	}		
			
			
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
public static void sendStats(OutputStream os ) throws IOException{
	  System.out.println("REQ " + getTotalReq() + ":"+ " Total successful requests so far ="+ getSuccReq());
	  System.out.println("REQ " + getTotalReq() + ":" + " File transfer complete");

	os.write(succReq);
	os.flush();
	os.write(totalReq);
	os.flush();
}

		

	

	public static void main(String[] args) throws IOException{
		   int reqs =0;
	  //   if (args.length != 1) {
	//            System.err.println("Usage: java myfileserver <port number>");
	//            System.exit(1);
	//        }
		   
	    int portNumber = 4444;    //Integer.parseInt(args[0]); 
	     boolean listening = true;
	  
	     
	     try ( 
	    		 
	    		
	    		 ServerSocket serverSocket = new ServerSocket(portNumber)){ // this line starts the actual listening process
	    	 while (listening) {
	    		  System.out.println(" "); //clear some space
	    		 if (workers <10)
	    		 {
	    			 workers++;
	    			 
	    		 new myfileserver(serverSocket.accept()).start();
	    		totalReq++;
	    		 
	    		 }
	    		 else
	    		 {
	    			 System.out.println("Waiting for worker to become free");
	    		 }
	    		 // System.out.println("Serving files from" + Files.list(Paths.get(filefolder)));
		    	 
	    	 }
	        
	    	
	    	 
	    	 
	    	 
	    	 
	        } catch (IOException e) {
	            System.out.println("Exception caught when trying to listen on port "
	                + portNumber + " or listening for a connection");
	            System.out.println(e.getMessage());
	        }
	}




	final class ThreadPool {

	    private BlockingQueue queue = new BlockingQueue();
	    private boolean closed = true;

	    private int poolSize = 10;
	    
	    
	    public void setPoolSize(int poolSize) {
	        this.poolSize = poolSize;
	    }
	    
	    public int getPoolSize() {
	        return poolSize;
	    }
	    
	    synchronized public void start() {
	    	int a = getTotalReq();
	    	a++;
	    	setTotalReq(a);
	        if (!closed) {
	            throw new IllegalStateException("Pool already started.");
	        }
	        closed = false;
	        for (int i = 0; i < poolSize; ++i) {
	            new PooledThread().start();
	        }
	    }

	    synchronized public void execute(Runnable job) {
	        if (closed) {
	            throw new PoolClosedException();
	        }
	        queue.enqueue(job);
	    }
	    
	    private class PooledThread extends Thread {
	        
	        public void run() {
	            while (true) {
	                Runnable job = (Runnable) queue.dequeue();
	                if (job == null) {
	                    break;
	                }
	                try {
	                    job.run();
	                } catch (Throwable t) {
	                    // ignore
	                }
	            }
	        }
	    }

	    public void close() {
	        closed = true;
	        queue.close();
	    }
	    
	    private  class PoolClosedException extends RuntimeException {
	        PoolClosedException() { 
	            super ("Pool closed.");
	        }
	    }
	}

	 class BlockingQueue {

	  private final LinkedList list = new LinkedList();
	  private boolean closed = false;
	  private boolean wait = false;
	  
	  synchronized public void enqueue(Object o) {
	    if (closed) {
	      throw new ClosedException();
	    }
	    list.add(o);
	    notify();
	  }

	  synchronized public Object dequeue() {
	    while (!closed && list.size() == 0) {
	      try {
	        wait();
	      }
	      catch (InterruptedException e) {
	        // ignore
	      }
	    }
	    if (list.size() == 0) {
	      return null;
	    }
	    return list.removeFirst();
	  }
	  
	  synchronized public int size() {
	      return list.size();
	  }
	  
	  synchronized public void close() {
	    closed = true;
	    notifyAll();
	  }
	  
	  synchronized public void open() {
	      closed = false;
	  }
	  
	  public class ClosedException extends RuntimeException {
	      ClosedException() {
	          super("Queue closed.");
	      }
	  }
	}
}