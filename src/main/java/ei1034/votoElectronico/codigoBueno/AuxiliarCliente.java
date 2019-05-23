package ei1034.votoElectronico.codigoBueno;

import java.net.*;
import java.io.*;

public class AuxiliarCliente {

   private MyStreamSocket mySocket;
   private InetAddress serverHost;
   private int serverPort;

   public AuxiliarCliente(String hostName, int portNum) throws SocketException, UnknownHostException, IOException {
	   serverHost = InetAddress.getByName(hostName);
	   serverPort = portNum;
	   mySocket = new MyStreamSocket(serverHost, serverPort);
	   
   } // end constructor

   public void fin( ) throws IOException {
	   String m = "-1";
	   mySocket.sendLongitud(m);
	   mySocket.close();	   
   }

    public void enviaMensaje(byte[] m, int indice)  throws IOException {
       String cadena = indice + "#" + m.length;
	   mySocket.sendLongitud(cadena);
       String res = mySocket.receiveLongitud();
       if (res.equals("LONGITUD RECIBIDA")) {
           mySocket.sendMessage(m);
       }
   }

    public void enviaLlave(byte[] m, String letra)
            throws IOException {
        String cadena = "0#" + m.length + "#" + letra;
        mySocket.sendLongitud(cadena);
        String res = mySocket.receiveLongitud();
        if (res.equals("LONGITUD RECIBIDA")) {
            mySocket.sendMessage(m);
        }
    }

    public void enviaMensajeFirmado(byte[] m, byte[] firma, int indice, int turno)  throws IOException {
        String cadena = indice + "#" + m.length + "#" + firma.length + "#" + turno;
        mySocket.sendLongitud(cadena);
        String res = mySocket.receiveLongitud();
        if (res.equals("LONGITUD RECIBIDA")) {
            mySocket.sendMessage(m);
        }
        res = mySocket.receiveLongitud();
        if (res.equals("LONGITUD RECIBIDA")) {
            mySocket.sendMessage(firma);
        }

    }

} //end class
