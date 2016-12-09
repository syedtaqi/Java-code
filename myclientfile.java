import java.io.*;
import java.net.*;
import java.util.Scanner;
//Author: Syed Taqi Raza 
//Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International Public License in effect
	
public class myclientfile {
	public static int succReq = 0; 
	public static int totalReq = 0;

    private static Socket Clientsocket;
    private static PrintWriter outToServer;
    private static BufferedReader inFromServer;
    private static InputStream is;
    private static FileOutputStream fileOserver;
  private static int PORT = 4444;
  private int fileSize;
  public static String fileReq = null;
    boolean Connected;

    public static void main(String[] args) throws InterruptedException {
        String server = "localhost";
        int port = PORT;

        if (args.length >= 1) {
            server = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
  fileReq = args[2];
//  System.out.println(fileReq);
        new myclientfile(server, port);
    }

    public myclientfile(String server, int port) {
        try {
            Clientsocket = new Socket(server, port);
            PrintWriter out = new PrintWriter(Clientsocket.getOutputStream());
            out.write(fileReq +"\n");  // send out the request
            out.flush();
            System.out.println("File " + fileReq +" found at server");
System.out.println("Downloading file" + fileReq);
          is = Clientsocket.getInputStream(); // get the result of if the file exists
         int doesExist =  is.read();
          if (doesExist == 1){
        	  
          
              int bytesRead;

              DataInputStream cData = new DataInputStream(Clientsocket.getInputStream());

              String fileName = cData.readUTF();
              OutputStream output = new FileOutputStream( fileName);
              long size = cData.readLong();
              byte[] buffer = new byte[1024];
              while (size > 0 && (bytesRead = cData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                  output.write(buffer, 0, bytesRead);
                  size -= bytesRead;
              }

              succReq = is.read();
              totalReq = is.read();
              System.out.println("Server handled" +totalReq +" requests," + succReq + " requests were successful" );

              output.close();
              cData.close();
              System.out.println("Download complete");

       
          Clientsocket.close();
          }
          else {
        	   succReq = is.read();
               totalReq = is.read();
          System.out.println("File " + fileReq +" not found at server");
          System.out.println("Server handled" +totalReq +" requests," + succReq + " requests were successful" );
          }
          // at this point, we want to collect and print the stats no matter success or fail
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
