package tcps;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Dora Di
 * 
 * 1. Create a server socket and bind it to a specific port number
 * 2. Listen for a connection from the client and accept it. This results in a client socket, created on the server, for the connection.
 * 3. Read data from the client via an InputStream obtained from the client socket
 * 4. Send data to the client via the client socket’s OutputStream.
 * 5. Close the connection with the client.
 * 
 * The steps 3 and 4 can be repeated many times depending on the protocol agreed between the server and the client.
 */

public class TCPS 
{
    public static final int PORT = 6666;
    public static ServerSocket serverSocket = null; // Server gets found
    //public static Socket openSocket = null;         // Server communicates with the client
    public static ExecutorService executorService = Executors.newFixedThreadPool(10);
    public static boolean running = true;
    
    public static Socket configureServer() throws UnknownHostException, IOException
    {
        if (serverSocket == null) {
            // get server's own IP address
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server ip: " + serverIP);

            // create a socket at the predefined port
            serverSocket = new ServerSocket(PORT);
        }
                    
        // Start listening and accepting requests on the serverSocket port, blocked until a request arrives

        Socket openSocket = serverSocket.accept();
        System.out.println("Server accepts requests at: " + openSocket);      
        
        return openSocket;
    }
    
    public static void connectClient(Socket openSocket) throws IOException 
    {
        String request, response;
        
        // two I/O streams attached to the server socket:       
        Scanner in;         // Scanner is the incoming stream (requests from a client)
        PrintWriter out;    // PrintWriter is the outcoming stream (the response of the server)
        in = new Scanner(openSocket.getInputStream()); 
        out = new PrintWriter(openSocket.getOutputStream(),true);
        // Parameter true ensures automatic flushing of the output buffer

        // Server keeps listening for request and reading data from the Client,  
        // until the Client sends "stop" requests
        while(in.hasNextLine())
        {
            request = in.nextLine();

            if(request.equals("stop"))
            {
                out.println("Good bye, client!");
                System.out.println("Log: " + request + " client!");
                try
                {
                    openSocket.close();
                    openSocket = null;
                }
                catch(Exception e)
                {
                    System.out.println("Could not close client's socket.");
                }
                break;
            }
            else
            {
                // mimic heavy load
                long millis = (long) (Math.random() * 10000);
                System.out.println("Sleeps for " + millis + " ms");
                try
                {
                    Thread.sleep(millis);
                }
                catch(Exception e){}
                System.out.println("waking up.");
                // server responses
                response = new StringBuilder(request).reverse().toString();
                out.println(response);   
                // Log response on the server's console, too
                System.out.println("Log: " + response);
            }
        }
    }
    
    public static void main(String[] args) throws IOException 
    {
        while(running) {
            try {
                // call configureServer which blocks, awaiting incoming request.
                Socket openSocket = configureServer();

                // Handle incoming request in separate thread - use ExecutorService interface. TODO get factory to return the ExecutorService instance.
                executorService.submit(() -> {
                    try
                    {
                        connectClient(openSocket);
                    }
                    catch (IOException e)
                    {
                        System.out.println(" Connection fails: " + e);
                    }
                    finally
                    {
                        try
                        {
                            openSocket.close();
                            System.out.println("Connection to client closed");
                        }
                        catch (IOException e)
                        {
                            System.out.println("Socket could not be closed.");
                        }
                    }
                });
            }
            catch (Exception e)
            {
                System.out.println(" System fails: " + e);
            }
        }
        serverSocket.close();
        System.out.println("Server port closed");
        running = false;
    }
}