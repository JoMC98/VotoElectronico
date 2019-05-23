package ei1034.votoElectronico.votoElectronico;

import java.net.*;
import java.io.*;

/**
 * A wrapper class of Socket which contains 
 * methods for sending and receiving messages
 * @author M. L. Liu
 */
public class MyStreamSocket extends Socket {
   private Socket  socket;
   private BufferedReader bufferedReader;
   private PrintWriter printWriter;
   private OutputStream output;
   private InetAddress acceptorHost;
   private InputStream input;

   public MyStreamSocket(InetAddress acceptorHost,
                  int acceptorPort ) throws SocketException, IOException{

      socket = new Socket(acceptorHost, acceptorPort );
      this.acceptorHost = acceptorHost;
      setStreams( );

   }

   public MyStreamSocket(Socket socket)  throws IOException {
      this.socket = socket;
      String host = socket.getInetAddress().getHostName();
      System.out.println("Creado socket con host: " + host);
      setStreams( );
   }

   private void setStreams( ) throws IOException{
      input = socket.getInputStream();
      output = socket.getOutputStream();
      bufferedReader = new BufferedReader(new InputStreamReader(input));
      printWriter = new PrintWriter(new OutputStreamWriter(output));
   }

   public void sendLongitud(String message) throws IOException {
      printWriter.print(message + "\n");
      printWriter.flush();
   }

   public void sendMessage(byte[] message) throws IOException {
      output.write(message);
      output.flush();
   }

   public String receiveLongitud() throws IOException {
      String message = bufferedReader.readLine( );
      return message;
   }

   public byte[] receiveMessage(int l) throws IOException {
      byte[] message = new byte[l];
      input.read(message);
      return message;
   } //end receiveMessage

   public void close( )
		throws IOException {	
      socket.close( );
   }

   @Override
   public String toString() {
      return acceptorHost.toString();
   }
} //end class
