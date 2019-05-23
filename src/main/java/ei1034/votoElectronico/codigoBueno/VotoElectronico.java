package ei1034.votoElectronico.codigoBueno;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by usuario on 2/05/19.
 */
public class VotoElectronico {
    private static String dirBasePublica = "src/main/resources/claves/llavePublica";
    private static String dirBaseCadenas = "src/main/resources/cadenas/r";
    private static String dirBaseEncriptados = "src/main/resources/encriptados/e";
    private static String ficheroIps = "src/main/resources/ips.txt";

    private static ArrayList<String> listaIps;
    private static String myIp;
    private static int myPosition;
    private static String[] letras = {"A", "B", "C", "D"};

    private static SecureRandom sr = new SecureRandom();
    private static RSA cifrador = new RSA();
    private static AES cifradorAES = new AES();

    private static AuxiliarCliente[] sockets = new AuxiliarCliente[4];

    public static void main(String[] args) {
//        crearLlaves();
        recuento(votar());
    }

    private static void crearLlaves() {
        crearSockets();
        cifrador.generarLlaves(letras[myPosition]);
        enviarLlavesPublicas();
    }

    private static void crearSockets() {
        listaIps = new ArrayList<String>();
        try {
            Scanner sc = new Scanner(new File(ficheroIps));
            while(sc.hasNextLine()) {
                listaIps.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try
        {
            myIp = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        for (int i=0; i<listaIps.size(); i++) {
            if (listaIps.get(i).equals(myIp)) {
                myPosition = i;
            }
        }
        for (int i=0; i<listaIps.size(); i++) {
            try {
                AuxiliarCliente socket = new AuxiliarCliente(listaIps.get(i), 3000);
                sockets[i] = socket;
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Creando socket con " + listaIps.get(i));
        }
    }

    private static void enviarLlavesPublicas() {
        try {
            for (int i=0; i<listaIps.size(); i++) {
                if (!listaIps.get(i).equals(myIp)) {
                    byte[] llave = cargarClavePublica(dirBasePublica + letras[myPosition] + ".dat");
                    sockets[i].enviaLlave(llave, letras[myPosition]);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] cargarClavePublica(String fichero) {
        byte[] bytes = null;
        try {
            FileInputStream fis = new FileInputStream(fichero);
            int tamaño = fis.available();
            bytes = new byte[tamaño];
            fis.read(bytes);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static byte[] votar() {
        PublicKey publicKeyA = cifrador.cargarClavePublica(dirBasePublica + "A.dat");
        PublicKey publicKeyB = cifrador.cargarClavePublica(dirBasePublica + "B.dat");
        PublicKey publicKeyC = cifrador.cargarClavePublica(dirBasePublica + "C.dat");
        PublicKey publicKeyD = cifrador.cargarClavePublica(dirBasePublica + "D.dat");

        Scanner sc = new Scanner(System.in);

//        System.out.println("Elije tu voto (Si o No): ");
//        String v = sc.nextLine();
        String v = "Santiago Abascal";

        BigInteger r1 = new BigInteger(512, 100, sr);
        cifradorAES.encriptarYguardarCifrado(r1.toByteArray(), 1);
        String cadena = v + "#" + r1;

        System.out.println("\nENCRIPTANDO D");
        byte[] eD = cifrador.encriptar(publicKeyD, cadena.getBytes());
        almacenarFichero(eD, dirBaseEncriptados + "D");

        System.out.println("\nENCRIPTANDO C");
        byte[] eC = cifrador.encriptar(publicKeyC, eD);
        almacenarFichero(eC, dirBaseEncriptados + "C");

        System.out.println("\nENCRIPTANDO B");
        byte[] eB = cifrador.encriptar(publicKeyB, eC);
        almacenarFichero(eB, dirBaseEncriptados + "B");

        System.out.println("\nENCRIPTANDO A");
        byte[] eA = cifrador.encriptar(publicKeyA, eB);
        almacenarFichero(eA, dirBaseEncriptados + "A");

        BigInteger big = new BigInteger(eA);
        System.out.println("CREAMOS eA " + big);

        System.out.println("\nENCRIPTANDO D");
        BigInteger r2 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r2.toByteArray(), 2);
        byte[] nuevo = añadirCadena(eA, r2);
        byte[] eD2 = cifrador.encriptar(publicKeyD, nuevo);

        System.out.println("\nENCRIPTANDO C");
        BigInteger r3 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r3.toByteArray(),3);
        nuevo = añadirCadena(eD2, r3);
        byte[] eC2 = cifrador.encriptar(publicKeyC, nuevo);

        System.out.println("\nENCRIPTANDO B");
        BigInteger r4 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r4.toByteArray(), 4);
        nuevo = añadirCadena(eC2, r4);
        byte[] eB2 = cifrador.encriptar(publicKeyB, nuevo);

        System.out.println("\nENCRIPTANDO A");
        BigInteger r5 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r5.toByteArray(), 5);
        nuevo = añadirCadena(eB2, r5);
        byte[] eA2 = cifrador.encriptar(publicKeyA, nuevo);

        return eA2;
    }

    private static void almacenarFichero(byte[] bytes, String fichero) {
        try {
            FileOutputStream fos = new FileOutputStream(fichero);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static byte[] leerFichero(String fichero) {
        File f = new File(fichero);
        int longitud = (int) f.length();
        byte[] bytes = new byte[longitud];
        try {
            FileInputStream fis=new FileInputStream(fichero);
            for(int i=0; i<longitud; i++) {
                int valor = fis.read();
                bytes[i] = (byte) valor;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static byte[] añadirCadena(byte[] v, BigInteger c) {
        byte[] bytes = c.toByteArray();

        byte[] nuevo = new byte[v.length + 65];
        for (int i=0; i<v.length; i++) {
            nuevo[i] = v[i];
        }
        for (int i=0; i<65; i++) {
            nuevo[i + v.length] = bytes[i];
        }

        return nuevo;
    }

    private static ArrayList<byte[]> eliminarCadena(byte[] v) {
        byte[] v1 = new byte[v.length - 65];
        for (int i=0; i<v1.length; i++) {
            v1[i] = v[i];
        }

        byte[] v2 = new byte[65];
        for (int i=0; i<65; i++) {
            v2[i] = v[i + v1.length];
        }

        ArrayList<byte[]> r = new ArrayList<byte[]>();

        r.add(v1);
        r.add(v2);

        return r;
    }

    private static boolean comprobarCadenas(byte[] vDesc, byte[] vAlm) {
        BigInteger descifrado = new BigInteger(vDesc);
        BigInteger almacenado = new BigInteger(vAlm);

        return descifrado.equals(almacenado);
    }

    private static void recuento(byte[] votoOriginal) {
        enviaMensaje(votoOriginal, 0, 1);
    }

    private static void enviaMensaje(byte[] m, int dest, int indice) {
        try {
            crearSockets();
            System.out.println("ENVIA A " + dest);
            sockets[dest].enviaMensaje(m, indice);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
