package ei1034.votoElectronico.codigoBueno;

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

public class prueba {
    private static String dirBasePublica = "src/main/resources/claves/llavePublica";
    private static String dirBasePrivada = "src/main/resources/claves/llavePrivada";
    private static String dirBaseCadenas = "src/main/resources/cadenas/r";
    private static String dirBaseEncriptados = "src/main/resources/encriptados/e";

    private static SecureRandom sr = new SecureRandom();

    public static void main(String[] args) throws UnsupportedEncodingException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ei1034.votoElectronico.codigoBueno.RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            RSA rsa = new RSA();
            byte[] abascal = rsa.encriptar(keyPair.getPublic(), "abascal".getBytes());

            byte[] salt = null;
            try {
                salt = getSalt();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            int numeroDeIteraciones = 10000;

            String password = "hola";

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, numeroDeIteraciones,64*2);
            SecretKey derivedKey = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(derivedKey.getEncoded(), "ei1034.votoElectronico.codigoBueno.AES");


            byte[] encriptado = null;
            Cipher cifrador = Cipher.getInstance("ei1034.votoElectronico.codigoBueno.AES");
            cifrador.init(Cipher.ENCRYPT_MODE, secretKey);
            encriptado = cifrador.doFinal(keyPair.getPrivate().getEncoded());

            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            spec = new PBEKeySpec(password.toCharArray(), salt, numeroDeIteraciones,64*2);
            derivedKey = factory.generateSecret(spec);
            secretKey = new SecretKeySpec(derivedKey.getEncoded(), "ei1034.votoElectronico.codigoBueno.AES");

            cifrador.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] desencriptado = cifrador.doFinal(encriptado);

            KeyFactory keyFactory = KeyFactory.getInstance("ei1034.votoElectronico.codigoBueno.RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(desencriptado);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            byte[] abascal2 = rsa.desencriptar(privateKey, abascal);
            System.out.println(new String(abascal2));
        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
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
