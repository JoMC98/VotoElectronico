package ei1034.votoElectronico.votoElectronico;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

public class AES {
    Cipher cifrador;
    private static String dirBasePrivada = "src/main/resources/claves/llavePrivada.dat";
    private static String dirBaseSalt = "src/main/resources/claves/salt.dat";
    private static String dirBaseCadenas = "src/main/resources/cadenas/r";

    public AES() {
        try {
            cifrador = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public SecretKey generarLlave(byte[] salt, String password) {
        try {
            int numeroDeIteraciones = 10000;

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, numeroDeIteraciones,64*2);
            SecretKey derivedKey = factory.generateSecret(spec);
            SecretKey secretKey = new SecretKeySpec(derivedKey.getEncoded(), "AES");
            return secretKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void encriptarYguardarCifrado(byte[] bytes, int ind, SecretKey key){
        try {
            cifrador.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] encriptado = null;
        try {
            encriptado = cifrador.doFinal(bytes);
        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }

        try {
            FileOutputStream fos = null;
            if (ind == 0) {
                fos = new FileOutputStream(dirBasePrivada);
            } else {
                fos = new FileOutputStream(dirBaseCadenas + ind + ".dat");
            }

            fos.write(encriptado);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey leerYdesencriptarCifrado(){
        SecretKey key = HomepageController.getSecretKey();
        byte[] bytes = null;
        try {
            FileInputStream fis = new FileInputStream(dirBasePrivada);
            int tamaño = fis.available();
            bytes = new byte[tamaño];
            fis.read(bytes);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            cifrador.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] desencriptado = null;
        try {
            desencriptado = cifrador.doFinal(bytes);
        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }

        PrivateKey privateKey = null;
        try {

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new PKCS8EncodedKeySpec(desencriptado);
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return privateKey;
    }

    public byte[] leerYdesencriptarCadena(int indice){
        SecretKey key = HomepageController.getSecretKey();
        byte[] bytes = null;
        try {
            FileInputStream fis = new FileInputStream(dirBaseCadenas + indice + ".dat");
            int tamaño = fis.available();
            bytes = new byte[tamaño];
            fis.read(bytes);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            cifrador.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] desencriptado = null;
        try {
            desencriptado = cifrador.doFinal(bytes);
        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }

        return desencriptado;
    }
}
