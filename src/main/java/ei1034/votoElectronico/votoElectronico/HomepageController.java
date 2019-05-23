package ei1034.votoElectronico.votoElectronico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequestMapping("/")
public class HomepageController {
    public static final int port = 3000;
    private static String ficheroIps = "src/main/resources/static/ips.txt";
    private static String dirBasePublica = "src/main/resources/static/claves/llavePublica";
    private static String dirBaseEncriptados = "src/main/resources/static/encriptados/e";
    private static String dirBaseSalt = "src/main/resources/static/claves/salt.dat";

    private static ArrayList<String> listaIps;
    private static String myIp;
    private static int myPosition;
    private static String[] letras = {"A", "B", "C", "D"};

    private static SecureRandom sr = new SecureRandom();
    private static RSA cifrador = new RSA();
    private static AES cifradorAES = new AES();

    private static AuxiliarCliente[] sockets = new AuxiliarCliente[4];

    private static List<String> votosFinales = null;

    private static SecretKey secretKey = null;

    private static DetectaAlteracion detectaAlteracion;

    private static AtomicInteger llavesRecibidas;

    @RequestMapping("/")
    public String index(Model model) {
        detectaAlteracion = new DetectaAlteracion();
        List<String> votos = new ArrayList<String>();
        votosFinales = Collections.synchronizedList(votos);
        llavesRecibidas = new AtomicInteger();;

//        Thread hilo = new Thread(new HiloActivarServidor(detectaAlteracion, votosFinales, llavesRecibidas));
//        hilo.start();

        model.addAttribute("paso", 1);
        return "index";
    }

    class HiloActivarServidor implements Runnable {
        DetectaAlteracion detectaAlteracion;
        List<String> votosFinales;
        AtomicInteger llavesRecibidas;

        public HiloActivarServidor(DetectaAlteracion detectaAlteracion, List<String> votosFinales, AtomicInteger llavesRecibidas) {
            this.detectaAlteracion = detectaAlteracion;
            this.votosFinales = votosFinales;
            this.llavesRecibidas = llavesRecibidas;
        }

        @Override
        public void run() {
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

                    Thread hilo = new Thread(new HiloServidor(myDataSocket, sockets, mensajesFase1,
                            detectaAlteracion, votosFinales, llavesRecibidas));
                    hilo.start();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @RequestMapping(value="/crearLlaves", method = RequestMethod.POST)
    public String crearLlaves(HttpSession session, Model model, @RequestParam("password") String password) {
//        crearSockets();
//        secretKey = cifradorAES.generarLlave(getSalt(), password);
//        PrivateKey privateKey = cifrador.generarLlaves(letras[myPosition]);
//        cifradorAES.encriptarYguardarCifrado(privateKey.getEncoded(), 0, secretKey);
//
//        enviarLlavesPublicas();

        model.addAttribute("paso", 2);
        return "index";
    }

    private static byte[] getSalt() {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        try {
            FileInputStream fis = new FileInputStream(dirBaseSalt);
            int tamaño = fis.available();
            salt = new byte[tamaño];
            fis.read(salt);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return salt;
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



    @RequestMapping(value="/votar", method = RequestMethod.POST)
    public String votar(Model model, HttpSession session, @RequestParam("eleccion") String voto) {
        if (llavesRecibidas.get() == 3) {
            byte[] cifrado = votar(voto, secretKey);

            session.setAttribute("cifrado", cifrado);
            secretKey = null;

            model.addAttribute("paso", 3);
            return "index";
        } else {
            model.addAttribute("paso", 2);
            model.addAttribute("llavesNoRecibidas", true);
            return "index";
        }
    }

    private static byte[] votar(String v, SecretKey secretKey) {
        PublicKey publicKeyA = cifrador.cargarClavePublica(dirBasePublica + "A.dat");
        PublicKey publicKeyB = cifrador.cargarClavePublica(dirBasePublica + "B.dat");
        PublicKey publicKeyC = cifrador.cargarClavePublica(dirBasePublica + "C.dat");
        PublicKey publicKeyD = cifrador.cargarClavePublica(dirBasePublica + "D.dat");

        BigInteger r1 = new BigInteger(512, 100, sr);
        cifradorAES.encriptarYguardarCifrado(r1.toByteArray(), 1, secretKey);
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

        cifradorAES.encriptarYguardarCifrado(r2.toByteArray(), 2, secretKey);
        byte[] nuevo = añadirCadena(eA, r2);
        byte[] eD2 = cifrador.encriptar(publicKeyD, nuevo);

        System.out.println("\nENCRIPTANDO C");
        BigInteger r3 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r3.toByteArray(),3, secretKey);
        nuevo = añadirCadena(eD2, r3);
        byte[] eC2 = cifrador.encriptar(publicKeyC, nuevo);

        System.out.println("\nENCRIPTANDO B");
        BigInteger r4 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r4.toByteArray(), 4, secretKey);
        nuevo = añadirCadena(eC2, r4);
        byte[] eB2 = cifrador.encriptar(publicKeyB, nuevo);

        System.out.println("\nENCRIPTANDO A");
        BigInteger r5 = new BigInteger(512, 100, sr);

        cifradorAES.encriptarYguardarCifrado(r5.toByteArray(), 5, secretKey);
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



    @RequestMapping(value="/recuento", method = RequestMethod.POST)
    public String iniciarRecuento(Model model, HttpSession session, @RequestParam("password") String password) {
        byte[] salt = leerSalt();
        secretKey = cifradorAES.generarLlave(salt, password);

        byte[] votoOriginal = (byte[]) session.getAttribute("cifrado");
        enviaMensaje(votoOriginal, 0, 1);

        while (votosFinales.size() < 4) {
            if (detectaAlteracion.getEstado()) {
                return "alteracion";
            }
        }

        int votosSi=0;
        for (String voto: votosFinales) {
            if (voto.equals("Si")) {
                votosSi++;
            }
        }

        model.addAttribute("paso", 4);
        model.addAttribute("votosSi", votosSi);
        model.addAttribute("votosNo", 4 - votosSi);
        return "index";
    }

    private byte[] leerSalt() {
        byte[] salt = null;
        try {
            FileInputStream fis = new FileInputStream(dirBaseSalt);
            int tamaño = fis.available();
            salt = new byte[tamaño];
            fis.read(salt);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return salt;
    }

    public static SecretKey getSecretKey() {
        return secretKey;
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


    //METODOS PA QUE NO FALLE EL GET

    @RequestMapping(value="/crearLlaves", method = RequestMethod.GET)
    public String crearLlavesGet() {
        return "redirect:/";
    }

    @RequestMapping(value="/votar", method = RequestMethod.GET)
    public String votarGet() {
        return "redirect:/";
    }

    @RequestMapping(value="/recuento", method = RequestMethod.GET)
    public String recuentoGet() {
        return "redirect:/";
    }

}
