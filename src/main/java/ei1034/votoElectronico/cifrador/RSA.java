package ei1034.votoElectronico.cifrador;

import javax.crypto.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class RSA {
    Cipher cifrador;
    Signature sign;

    public RSA() {
        try {
            cifrador = Cipher.getInstance("RSA");
            sign = Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey generarLlaves(String letra) {
        final int keySize = 2048;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            guardarClave(keyPair.getPublic(), "src/main/resources/static/claves/llavePublica" + letra + ".dat");

            return keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PublicKey cargarClavePublica(String fichero) {
        PublicKey key = null;
        try {
            FileInputStream fis = new FileInputStream(fichero);
            int tamaño = fis.available();
            byte[] bytes = new byte[tamaño];

            fis.read(bytes);
            fis.close();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(bytes);
            key = keyFactory.generatePublic(keySpec);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return key;
    }

    private void guardarClave(Key clave, String fichero) {
        byte[] bytes = clave.getEncoded();

        try {
            FileOutputStream fos = new FileOutputStream(fichero);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] encriptar(PublicKey publicKey, byte[] bytes){
        try {
            cifrador.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        int numBloques = ((bytes.length -1 ) / 245) + 1;
        byte[] encriptado = new byte[256 * numBloques];

        try {
            ArrayList<byte[]> vectorBloques = new ArrayList<byte[]>();

            for (int i = 0; i < numBloques; i++) {
                byte[] bloque = null;
                if (i == numBloques - 1) {
                    bloque = new byte[bytes.length - (i * 245)];
                } else {
                    bloque = new byte[245];
                }

                for (int j = 0; j < bloque.length; j++) {
                    bloque[j] = bytes[j + (245 * i)];
                }
                vectorBloques.add(bloque);
            }

            for (int i = 0; i < numBloques; i++) {
                byte[] bloque = cifrador.doFinal(vectorBloques.get(i));
                for (int j = 0; j < 256; j++) {
                    encriptado[j + (256 * i)] = bloque[j];
                }
            }

        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }

        return encriptado;
    }

    public byte[] desencriptar(PrivateKey privateKey, byte[] encriptado){
        try {
            cifrador.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        int numBloques = ((encriptado.length-1) / 256) + 1;

        byte[] bytes = null;

        try {
            ArrayList<byte[]> vectorBloques = new ArrayList<byte[]>();

            for (int i = 0; i < numBloques; i++) {
                byte[] bloque = null;
                if (i == numBloques - 1) {
                    bloque = new byte[encriptado.length - (i * 256)];
                } else {
                    bloque = new byte[256];
                }

                for (int j = 0; j < bloque.length; j++) {
                    bloque[j] = encriptado[j + (256 * i)];
                }
                vectorBloques.add(bloque);
            }

            ArrayList<byte[]> vectorDescifrados = new ArrayList<byte[]>();
            for (int i = 0; i < numBloques; i++) {
                byte[] bloque = cifrador.doFinal(vectorBloques.get(i));
                vectorDescifrados.add(bloque);
            }
            int longitud = 0;
            for (int i=0; i<numBloques; i++) {
                longitud += vectorDescifrados.get(i).length;
            }

            bytes = new byte[longitud];
            for (int i=0; i<numBloques; i++) {
                byte[] bloque = vectorDescifrados.get(i);

                for (int j = 0; j < bloque.length; j++) {
                    bytes[j + (245 * i)] = bloque[j];
                }
            }
        } catch(BadPaddingException e){
            e.printStackTrace();
        } catch(IllegalBlockSizeException e){
            e.printStackTrace();
        }

        return bytes;
    }

    public byte[] firmar(PrivateKey key, byte[] bytes) {
        byte[] signature = null;
        try {
            sign.initSign(key);
            sign.update(bytes);
            signature = sign.sign();
        } catch(InvalidKeyException e) {
            e.printStackTrace();
        } catch(SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public boolean comprobarFirma(PublicKey key, byte[] firma, byte[] bytes) {
        boolean retorno = false;
        try {
            sign.initVerify(key);
            sign.update(bytes);
            retorno = sign.verify(firma);
        } catch(InvalidKeyException e) {
            e.printStackTrace();
        } catch(SignatureException e) {
            e.printStackTrace();
        }
        return retorno;
    }
}
