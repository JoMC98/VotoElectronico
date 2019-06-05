package ei1034.votoElectronico.votoElectronico;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.Scanner;

public class PruebaUpdate {
    private static ArrayList<String> listaIps;

    private static SecureRandom sr = new SecureRandom();

    private static String ficheroIps = "src/main/resources/static/ips.txt";
    private static String dirBasePublica = "src/main/resources/static/claves/llavePublica";
    private static Cipher cifrador;

    public static void main(String[] args) throws UnsupportedEncodingException {
        try {
            cifrador = Cipher.getInstance("RSA/ECB/NoPadding");
            KeyPairGenerator keyPairGeneratorA = KeyPairGenerator.getInstance("RSA");
            keyPairGeneratorA.initialize(2048);
            KeyPair keyPairA = keyPairGeneratorA.genKeyPair();

            KeyPairGenerator keyPairGeneratorB = KeyPairGenerator.getInstance("RSA");
            keyPairGeneratorB.initialize(2048);
            KeyPair keyPairB = keyPairGeneratorB.genKeyPair();

            KeyPairGenerator keyPairGeneratorC = KeyPairGenerator.getInstance("RSA");
            keyPairGeneratorC.initialize(2048);
            KeyPair keyPairC = keyPairGeneratorC.genKeyPair();

            KeyPairGenerator keyPairGeneratorD  = KeyPairGenerator.getInstance("RSA");
            keyPairGeneratorD.initialize(2048);
            KeyPair keyPairD = keyPairGeneratorD.genKeyPair();
            
            PublicKey publicKeyA = keyPairA.getPublic();
            PublicKey publicKeyB = keyPairB.getPublic();
            PublicKey publicKeyC = keyPairC.getPublic();
            PublicKey publicKeyD = keyPairD.getPublic();
    
            BigInteger r1 = new BigInteger(512, 100, sr);
            String v = "Si";
            String cadena = v + "#" + r1;

            System.out.println(cadena.getBytes().length);
    
            System.out.println("\nENCRIPTANDO D");
            byte[] eD = encriptar(publicKeyD, cadena.getBytes());

            System.out.println(eD.length);
    
            System.out.println("\nENCRIPTANDO C");
            byte[] eC = encriptar(publicKeyC, eD);

            System.out.println(eC.length);
    
            System.out.println("\nENCRIPTANDO B");
            byte[] eB = encriptar(publicKeyB, eC);

            System.out.println(eB.length);
    
            System.out.println("\nENCRIPTANDO A");
            byte[] eA = encriptar(publicKeyA, eB);

            System.out.println(eA.length);
    
            System.out.println("\nENCRIPTANDO D");
            BigInteger r2 = new BigInteger(512, 100, sr);
    
            byte[] nuevo = añadirCadena(eA, r2);
            byte[] eD2 = encriptar(publicKeyD, nuevo);

            System.out.println(eD2.length);
    
            System.out.println("\nENCRIPTANDO C");
            BigInteger r3 = new BigInteger(512, 100, sr);
    
            nuevo = añadirCadena(eD2, r3);
            byte[] eC2 = encriptar(publicKeyC, nuevo);

            System.out.println(eC2.length);
    
            System.out.println("\nENCRIPTANDO B");
            BigInteger r4 = new BigInteger(512, 100, sr);
    
            nuevo = añadirCadena(eC2, r4);
            byte[] eB2 = encriptar(publicKeyB, nuevo);

            System.out.println(eB2.length);
    
            System.out.println("\nENCRIPTANDO A");
            BigInteger r5 = new BigInteger(512, 100, sr);
    
            nuevo = añadirCadena(eB2, r5);
            byte[] eA2 = encriptar(publicKeyA, nuevo);

            System.out.println(eA2.length);

            System.out.println("\nDESENCRIPTANDO A");

            PrivateKey privateKeyA = keyPairA.getPrivate();
            PrivateKey privateKeyB = keyPairB.getPrivate();
            PrivateKey privateKeyC = keyPairC.getPrivate();
            PrivateKey privateKeyD = keyPairD.getPrivate();
            
            byte[] dA = desencriptar(privateKeyA, eA2);
    
            ArrayList<byte[]> conjunto = eliminarCadena(dA);


            System.out.println("\nDESENCRIPTANDO B");
            byte[] dB = desencriptar(privateKeyB, conjunto.get(0));
    
            conjunto = eliminarCadena(dB);

            System.out.println("\nDESENCRIPTANDO C");
            byte[] dC = desencriptar(privateKeyC, conjunto.get(0));
    
            conjunto = eliminarCadena(dC);
    
    
            System.out.println("\nDESENCRIPTANDO D");
            byte[] dD = desencriptar(privateKeyD, conjunto.get(0));
    
            conjunto = eliminarCadena(dD);

    
            System.out.println("\nDESENCRIPTANDO A");
            byte[] dA2 = desencriptar(privateKeyA, conjunto.get(0));

            System.out.println("\nDESENCRIPTANDO B");
            byte[] dB2 = desencriptar(privateKeyB, dA2);
    
            System.out.println("\nDESENCRIPTANDO C");
            byte[] dC2 = desencriptar(privateKeyC, dB2);
    
            System.out.println("\nDESENCRIPTANDO D");
            byte[] dD2 = desencriptar(privateKeyD, dC2);

            String cadenaFinal = new String(dD2);
            String[] vectorFinal = cadenaFinal.split("#");
    
            String votoFinal = vectorFinal[0];
    
            System.out.println("\n" + votoFinal);
            
            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encriptar(PublicKey publicKey, byte[] bytes){
        try {
            cifrador.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Antes de cifrar " + bytes.length);

            if (bytes.length <= 256)
                return cifrador.doFinal(bytes);
            else {
                cifrador.update(bytes, 0, 256);
                return cifrador.doFinal();
            }

        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] desencriptar(PrivateKey publicKey, byte[] bytes){
        try {
            cifrador.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("Antes de descifrar " + bytes.length);

            if (bytes.length <= 256)
                return cifrador.doFinal(bytes);
            else {
                cifrador.update(bytes, 0, 256);
                return cifrador.doFinal();
            }

        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }
        return null;
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

}
