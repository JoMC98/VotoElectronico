package ei1034.votoElectronico.codigoBueno;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Este modulo contiene la logica de aplicacion del servidor del juego Hundir la flota
 * Utiliza sockets en modo stream para llevar a cabo la comunicacion entre procesos.
 * Puede servir a varios clientes de modo concurrente lanzando una hebra para atender a cada uno de ellos.
 * Se le puede indicar el puerto del servidor en linea de ordenes.
 */


public class ServidorSockets {
	
	public static final int port = 3000;
	private static String ficheroIps = "src/main/resources/ips.txt";

	public static void main(String[] args) {
		try (ServerSocket myConnectionSocket = new ServerSocket(port)){
            List<byte[]> mensajesFase1Unsafe = new ArrayList<byte[]>();
            List<byte[]> mensajesFase1 = Collections.synchronizedList(mensajesFase1Unsafe);

			ArrayList<String> listaIps = new ArrayList<String>();

			try {
				Scanner sc = new Scanner(new File(ficheroIps));
				while(sc.hasNextLine()) {
					listaIps.add(sc.nextLine());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			String myIp = null;


				try
			{
				myIp = InetAddress.getLocalHost().getHostAddress();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			int myPosition;

			for (int i=0; i<listaIps.size(); i++) {
				if (listaIps.get(i).equals(myIp)) {
					myPosition = i;
				}
			}

			List<AuxiliarCliente> socketsUnsafe = null;
			List<AuxiliarCliente> sockets = null;

			while(true) {
				MyStreamSocket myDataSocket = new MyStreamSocket(myConnectionSocket.accept());

				if (socketsUnsafe == null) {
					socketsUnsafe = new ArrayList<AuxiliarCliente>();
					for (int i=0; i<listaIps.size(); i++) {
						AuxiliarCliente socket = new AuxiliarCliente(listaIps.get(i), 3000);
						socketsUnsafe.add(socket);
					}
					System.out.println("LISTA DE SOCKETS: " + sockets);
					sockets = Collections.synchronizedList(socketsUnsafe);
				}


				Thread hilo = new Thread(new HiloServidor(myDataSocket, sockets, mensajesFase1));
				hilo.start();
			}
		}
	   	catch (Exception e) {
		   e.printStackTrace();
	   	}
   	}
}
