package ei1034.votoElectronico.votoElectronico;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Scanner;

public class PruebaAlteracion {
    private static ArrayList<String> listaIps;

    private static SecureRandom sr = new SecureRandom();

    private static String ficheroIps = "src/main/resources/static/ips.txt";
    private static String dirBasePublica = "src/main/resources/static/claves/llavePublica";

    public static void main(String[] args) throws UnsupportedEncodingException {
        RSA cifrador = new RSA();

        PublicKey publicKeyA = cifrador.cargarClavePublica(dirBasePublica + "A.dat");
        PublicKey publicKeyB = cifrador.cargarClavePublica(dirBasePublica + "B.dat");
        PublicKey publicKeyC = cifrador.cargarClavePublica(dirBasePublica + "C.dat");
        PublicKey publicKeyD = cifrador.cargarClavePublica(dirBasePublica + "D.dat");

        BigInteger r1 = new BigInteger(512, 100, sr);
        String v = "Si";
        String cadena = v + "#" + r1;

        System.out.println("\nENCRIPTANDO D");
        byte[] eD = cifrador.encriptar(publicKeyD, cadena.getBytes());

        System.out.println("\nENCRIPTANDO C");
        byte[] eC = cifrador.encriptar(publicKeyC, eD);

        System.out.println("\nENCRIPTANDO B");
        byte[] eB = cifrador.encriptar(publicKeyB, eC);

        System.out.println("\nENCRIPTANDO A");
        byte[] eA = cifrador.encriptar(publicKeyA, eB);

        System.out.println("\nENCRIPTANDO D");
        BigInteger r2 = new BigInteger(512, 100, sr);

        byte[] nuevo = añadirCadena(eA, r2);
        byte[] eD2 = cifrador.encriptar(publicKeyD, nuevo);

        System.out.println("\nENCRIPTANDO C");
        BigInteger r3 = new BigInteger(512, 100, sr);

        nuevo = añadirCadena(eD2, r3);
        byte[] eC2 = cifrador.encriptar(publicKeyC, nuevo);

        System.out.println("\nENCRIPTANDO B");
        BigInteger r4 = new BigInteger(512, 100, sr);

        nuevo = añadirCadena(eC2, r4);
        byte[] eB2 = cifrador.encriptar(publicKeyB, nuevo);

        System.out.println("\nENCRIPTANDO A");
        BigInteger r5 = new BigInteger(512, 100, sr);

        nuevo = añadirCadena(eB2, r5);
        byte[] eA2 = cifrador.encriptar(publicKeyA, nuevo);

        ArrayList<String> listaIps = new ArrayList<String>();
        try {
            Scanner sc = new Scanner(new File(ficheroIps));
            while(sc.hasNextLine()) {
                listaIps.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            AuxiliarCliente socket = new AuxiliarCliente(listaIps.get(0), 3000);
            socket.enviaMensaje(eA2, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
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


}
