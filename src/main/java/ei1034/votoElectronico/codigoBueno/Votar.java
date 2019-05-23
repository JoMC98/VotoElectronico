package ei1034.votoElectronico.codigoBueno;

import ei1034.votoElectronico.votoElectronico.RSA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Votar {
    private static String dirBasePublica = "src/main/resources/claves/llavePublica";
    private static String dirBasePrivada = "src/main/resources/claves/llavePrivada";
    private static String dirBaseCadenas = "src/main/resources/cadenas/r";
    private static String dirBaseEncriptados = "src/main/resources/encriptados/e";

    private static SecureRandom sr = new SecureRandom();


    public static void main(String[] args){
        RSA cifrador = new RSA();
        cifrador.generarLlaves("A");
        cifrador.generarLlaves("B");
        cifrador.generarLlaves("C");
        cifrador.generarLlaves("D");

        PublicKey publicKeyA = cifrador.cargarClavePublica(dirBasePublica + "A.dat");
        PublicKey publicKeyB = cifrador.cargarClavePublica(dirBasePublica + "B.dat");
        PublicKey publicKeyC = cifrador.cargarClavePublica(dirBasePublica + "C.dat");
        PublicKey publicKeyD = cifrador.cargarClavePublica(dirBasePublica + "D.dat");

        Scanner sc = new Scanner(System.in);

//        System.out.println("Elije tu voto (Si o No): ");
//        String v = sc.nextLine();
        String v = "Santiago Abascal";

        BigInteger r1 = new BigInteger(512, 100, sr);
        almacenarFichero(r1.toByteArray(), dirBaseCadenas + "1");

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


        System.out.println("\nENCRIPTANDO D");
        BigInteger r2 = new BigInteger(512, 100, sr);

        almacenarFichero(r2.toByteArray(), dirBaseCadenas + "2");
        byte[] nuevo = añadirCadena(eA, r2);
        byte[] eD2 = cifrador.encriptar(publicKeyD, nuevo);


        System.out.println("\nENCRIPTANDO C");
        BigInteger r3 = new BigInteger(512, 100, sr);

        almacenarFichero(r3.toByteArray(), dirBaseCadenas + "3");
        nuevo = añadirCadena(eD2, r3);
        byte[] eC2 = cifrador.encriptar(publicKeyC, nuevo);


        System.out.println("\nENCRIPTANDO B");
        BigInteger r4 = new BigInteger(512, 100, sr);

        almacenarFichero(r4.toByteArray(), dirBaseCadenas + "4");
        nuevo = añadirCadena(eC2, r4);
        byte[] eB2 = cifrador.encriptar(publicKeyB, nuevo);


        System.out.println("\nENCRIPTANDO A");
        BigInteger r5 = new BigInteger(512, 100, sr);

        almacenarFichero(r5.toByteArray(), dirBaseCadenas + "5");
        nuevo = añadirCadena(eB2, r5);
        byte[] eA2 = cifrador.encriptar(publicKeyA, nuevo);


        System.out.println("\nENVIO");

//        PrivateKey privateKeyA =
//
//        System.out.println("\nDESENCRIPTANDO A");
//        byte[] dA = cifrador.desencriptar(privateKeyA, eA2);
//
//        ArrayList<byte[]> conjunto = eliminarCadena(dA);
//        byte[] lectura = leerFichero(dirBaseCadenas + "5");
//        System.out.println("Comprobando cadena: " + comprobarCadenas(conjunto.get(1), lectura));
//
//
//        System.out.println("\nDESENCRIPTANDO B");
//        byte[] dB = cifrador.desencriptar(privateKeyB, conjunto.get(0));
//
//        conjunto = eliminarCadena(dB);
//        lectura = leerFichero(dirBaseCadenas + "4");
//        System.out.println("Comprobando cadena: " + comprobarCadenas(conjunto.get(1), lectura));
//
//
//        System.out.println("\nDESENCRIPTANDO C");
//        byte[] dC = cifrador.desencriptar(privateKeyC, conjunto.get(0));
//
//        conjunto = eliminarCadena(dC);
//        lectura = leerFichero(dirBaseCadenas + "3");
//        System.out.println("Comprobando cadena: " + comprobarCadenas(conjunto.get(1), lectura));
//
//
//        System.out.println("\nDESENCRIPTANDO D");
//        byte[] dD = cifrador.desencriptar(privateKeyD, conjunto.get(0));
//
//        conjunto = eliminarCadena(dD);
//        lectura = leerFichero(dirBaseCadenas + "2");
//        System.out.println("Comprobando cadena: " + comprobarCadenas(conjunto.get(1), lectura));
//
//
//        lectura = leerFichero(dirBaseEncriptados + "A");
//        System.out.println("Comprobando encriptado: " + comprobarCadenas(conjunto.get(0), lectura));
//
//
//        System.out.println("\nDESENCRIPTANDO A");
//        byte[] dA2 = cifrador.desencriptar(privateKeyA, conjunto.get(0));
//        System.out.println("\nFIRMANDO A");
//        byte[] fA = cifrador.firmar(privateKeyA, dA2);
//
//
//        System.out.println("\nA ENVIO fA y dA2 a todos");
//

//        System.out.println("Comprobando firma de A: " + cifrador.comprobarFirma(publicKeyA, fA, dA2));
//
//        lectura = leerFichero(dirBaseEncriptados + "B");
//        System.out.println("Comprobando encriptado: " + comprobarCadenas(dA2, lectura));
//
//        System.out.println("\nDESENCRIPTANDO B");
//        byte[] dB2 = cifrador.desencriptar(privateKeyB, dA2);
//        System.out.println("\nFIRMANDO B");
//        byte[] fB = cifrador.firmar(privateKeyB, dB2);
//
//
//        System.out.println("\nB ENVIO fB y dB2 a todos");
//
//
//        System.out.println("Comprobando firma de B: " + cifrador.comprobarFirma(publicKeyB, fB, dB2));
//
//        lectura = leerFichero(dirBaseEncriptados + "C");
//        System.out.println("Comprobando encriptado: " + comprobarCadenas(dB2, lectura));
//
//        System.out.println("\nDESENCRIPTANDO C");
//        byte[] dC2 = cifrador.desencriptar(privateKeyC, dB2);
//        System.out.println("\nFIRMANDO C");
//        byte[] fC = cifrador.firmar(privateKeyC, dC2);
//
//
//        System.out.println("\nB ENVIO fC y dC2 a todos");
//
//
//        System.out.println("Comprobando firma de C: " + cifrador.comprobarFirma(publicKeyC, fC, dC2));
//
//        lectura = leerFichero(dirBaseEncriptados + "D");
//        System.out.println("Comprobando encriptado: " + comprobarCadenas(dC2, lectura));
//
//        System.out.println("\nDESENCRIPTANDO D");
//        byte[] dD2 = cifrador.desencriptar(privateKeyD, dC2);
//        System.out.println("\nFIRMANDO D");
//        byte[] fD = cifrador.firmar(privateKeyD, dD2);
//
//
//        System.out.println("\nD ENVIO fD y dD2 a todos");
//
//
//        System.out.println("Comprobando firma de D: " + cifrador.comprobarFirma(publicKeyD, fD, dD2));
//
//
//        String cadenaFinal = new String(dD2);
//        String[] vectorFinal = cadenaFinal.split("#");
//
//        String votoFinal = vectorFinal[0];
//        BigInteger cadenaExtraida = new BigInteger(vectorFinal[1]);
//
//        lectura = leerFichero(dirBaseCadenas + "1");
//        System.out.println("Comprobando cadena: " + comprobarCadenas(cadenaExtraida.toByteArray(), lectura));
//
//        System.out.println("\n" + votoFinal);


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


}
