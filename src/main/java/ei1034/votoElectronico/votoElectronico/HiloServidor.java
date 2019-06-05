package ei1034.votoElectronico.votoElectronico;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


class HiloServidor implements Runnable {
    MyStreamSocket myDataSocket;
    private static String dirBasePublica = "src/main/resources/static/claves/llavePublica";
    private static String dirBaseEncriptados = "src/main/resources/static/encriptados/e";
    private static String ficheroIps = "src/main/resources/static/ips.txt";

    private static ArrayList<String> listaIps;
    private static String myIp;
    private static int myPosition;
    private static String[] letras = {"A", "B", "C", "D"};

    private static RSA cifrador;
    private static AES cifradorAES;
    private static PrivateKey privateKey = null;
    private static SecureRandom sr = new SecureRandom();
    private static DetectaAlteracion detectaAlteracion;

    private static List<byte[]> mensajesFase1;
    private static List<byte[]> mensajesFase2 = new ArrayList<byte[]>();
    private static List<byte[]> mensajesFase3 = new ArrayList<byte[]>();
    private static List<byte[]> mensajesFase3Firmados = new ArrayList<byte[]>();
    private static List<byte[]> mensajesFase4 = new ArrayList<byte[]>();
    private static List<byte[]> mensajesFase4Firmados = new ArrayList<byte[]>();
    private static List<byte[]> mensajesFase5 = new ArrayList<byte[]>();
    private static List<String> votosFinales;
    private static List<String> votosFinalesProvisional = new ArrayList<String>();
    private static List<AuxiliarCliente> sockets;

    private static AtomicInteger llavesRecibidas;


   HiloServidor(MyStreamSocket myDataSocket, List<AuxiliarCliente> sockets, List<byte[]> mensajesFase1,
                DetectaAlteracion detectaAlteracion, List<String> votosFinales, AtomicInteger llavesRecibidas) {
       this.myDataSocket = myDataSocket;
       this.sockets = sockets;
       this.mensajesFase1 = mensajesFase1;
       this.detectaAlteracion = detectaAlteracion;
       this.votosFinales = votosFinales;
       this.llavesRecibidas = llavesRecibidas;

       cifradorAES = new AES();
       cifrador = new RSA();

       listaIps = new ArrayList<String>();
       try {
           Scanner sc = new Scanner(new File(ficheroIps));
           while(sc.hasNextLine()) {
               listaIps.add(sc.nextLine());
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }

       try {
           myIp = InetAddress.getLocalHost().getHostAddress();
       }
       catch(Exception e) {
           e.printStackTrace();
       }

       for (int i=0; i<listaIps.size(); i++) {
           if (listaIps.get(i).equals(myIp)) {
               myPosition = i;
           }
       }

   }

   public void run( ) {
       boolean done = false;
       byte[] m;
       String l;
       String[] v;
       int op;
       int longitud;


       try {
            while (!done) {
                // Recibe una peticion del cliente
        	    // Extrae la operación y los argumentos
                l = myDataSocket.receiveLongitud();
                v = l.split("#");
                op = Integer.parseInt(v[0]);
                longitud = Integer.parseInt(v[1]);

                switch (op) {
                    //LLAVES
                    case 0:
                        String letra = v[2];
                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);

                        recibirLlave(m, letra);

                        llavesRecibidas.getAndIncrement();
                        break;
                    //Primer envio todos a Alice (Primera fase)
                    case 1:
                        if (privateKey == null && HomepageController.getSecretKey() != null)
                            privateKey = cifradorAES.leerYdesencriptarCifrado();
                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);
                        mensajesFase1.add(m);

                        if (mensajesFase1.size() == 4) {
                            boolean correcto = false;
                            for (int i=0; i<4; i++) {
                                if (desencriptar(i, 1, -1))
                                    correcto = true;
                            }
                            if (!correcto) {
                                enviaAviso();
                            } else {
                                ArrayList<byte[]> list = shuffle(mensajesFase1);
                                for (int i=0; i<4; i++) {
                                    enviaMensaje(list.get(i), 1, 2);
                                }
                            }
                        }
                        break;
                    //Segunda fase de envios
                    case 2:
                        if (privateKey == null && HomepageController.getSecretKey() != null)
                            privateKey = cifradorAES.leerYdesencriptarCifrado();
                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);
                        mensajesFase2.add(m);

                        if (mensajesFase2.size() == 4) {
                            boolean correcto = false;
                            for (int i=0; i<4; i++) {
                                if (desencriptar(i, 2, -1))
                                    correcto = true;
                            }
                            if (!correcto) {
                                enviaAviso();
                            } else {
                                ArrayList<byte[]> list = shuffle(mensajesFase2);
                                for (int i=0; i<4; i++) {
                                    if (myPosition == 3) {
                                        enviaMensaje(list.get(i), 0, 3);
                                    } else {
                                        enviaMensaje(list.get(i), myPosition + 1, 2);
                                    }
                                }
                            }
                        }
                        break;
                    //Dave envia todos a Alice (Tercera fase)
                    case 3:
                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);
                        mensajesFase3.add(m);

                        if (mensajesFase3.size() == 4) {
                            boolean correcto = false;
                            for (int i=0; i<4; i++) {
                                if (desencriptar(i, 3, -1))
                                    correcto = true;
                            }
                            if (!correcto) {
                                enviaAviso();
                            } else {
                                for (int i=0; i<4; i++) {
                                    for (int j=0; j<4; j++) {
                                        if (j != myPosition) {
                                            enviaMensajeFirmado(mensajesFase3.get(i),mensajesFase3Firmados.get(i), j, 4);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    //Cuarta fase de envio
                    case 4:
                        int turno = Integer.parseInt(v[3]);
                        int longitudFirma = Integer.parseInt(v[2]);

                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);
                        if (myPosition == turno + 1)
                            mensajesFase4.add(m);

                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        byte[] firma = myDataSocket.receiveMessage(longitudFirma);

                        PublicKey publicKey = cifrador.cargarClavePublica(dirBasePublica + letras[turno] + ".dat");
                        boolean correcto = cifrador.comprobarFirma(publicKey, firma, m);

                        if (!correcto) {
                            enviaAviso();
                        } else {
                            if (myPosition == turno + 1 && mensajesFase4.size() == 4) {
                                correcto = false;
                                for (int i=0; i<4; i++) {
                                    if (desencriptar(i, 4, turno))
                                        correcto = true;
                                }
                                if (!correcto) {
                                    enviaAviso();
                                } else {
                                    for (int i=0; i<4; i++) {
                                        for (int j=0; j<4; j++) {
                                            if (j != myPosition) {
                                                if (myPosition == 3) {
                                                    enviaMensajeFirmado(mensajesFase4.get(i), mensajesFase4Firmados.get(i), j, 5);
                                                } else {
                                                    enviaMensajeFirmado(mensajesFase4.get(i), mensajesFase4Firmados.get(i), j, 4);
                                                }
                                            } else {
                                                if (myPosition == 3) {
                                                    enviaMensajeFirmado(mensajesFase4.get(i), mensajesFase4Firmados.get(i), j, 5);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        break;
                    //Todos verifican y se recuentan votos
                    case 5:

                        turno = Integer.parseInt(v[3]);
                        longitudFirma = Integer.parseInt(v[2]);

                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        m = myDataSocket.receiveMessage(longitud);
                        mensajesFase5.add(m);

                        myDataSocket.sendLongitud("LONGITUD RECIBIDA");
                        firma = myDataSocket.receiveMessage(longitudFirma);

                        publicKey = cifrador.cargarClavePublica(dirBasePublica + letras[turno] + ".dat");
                        correcto = cifrador.comprobarFirma(publicKey, firma, m);

                        if (!correcto) {
                            enviaAviso();
                        } else {
                            if (mensajesFase5.size() == 4) {
                                correcto = false;
                                for (int i=0; i<4; i++) {
                                    if (desencriptar(i, 5, -1))
                                        correcto = true;
                                }
                                if (!correcto) {
                                    enviaAviso();
                                } else {
                                    for (int i=0; i<4; i++) {
                                        votosFinales.add(votosFinalesProvisional.get(i));
                                    }
                                }
                            }
                        }
                        break;
                    case 6:
                        detectaAlteracion.nuevaAlteracion();
//                        myDataSocket.close();
                }
            }
       }
       catch (Exception ex) {
           System.out.println("Exception caught in thread: " + ex);
       }
   }

    private static void enviaMensaje(byte[] m, int dest, int indice) {
        try {
            System.out.println("SE ENVIA A " + dest + " CON IP: " + listaIps.get(dest) + " EL MENSAJE CON INDICE: " + indice);
            sockets.get(dest).enviaMensaje(m, indice);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviaMensajeFirmado(byte[] m, byte[] firma, int dest, int indice) {
        try {
            System.out.println("SE ENVIA A " + dest + " CON IP: " + listaIps.get(dest) + " EL MENSAJE CON INDICE: " + indice);
            sockets.get(dest).enviaMensajeFirmado(m, firma, indice, myPosition);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enviaAviso() {
        try {
            detectaAlteracion.nuevaAlteracion();
            for (int i=0; i<4; i++) {
                sockets.get(i).enviaAviso();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<byte[]> shuffle(List<byte[]> anterior) {
        int primero = sr.nextInt(4) + 1;

        int segundo = sr.nextInt(4) + 1;
        while (segundo == primero) {
            segundo = sr.nextInt(4) + 1;
        }

        int tercero = sr.nextInt(4) + 1;
        while (tercero == primero || tercero == segundo) {
            tercero = sr.nextInt(4) + 1;
        }

        int cuarto = 10 - primero - segundo - tercero;

        ArrayList<byte[]> nueva = new ArrayList<byte[]>();

        nueva.add(anterior.get(primero - 1));
        nueva.add(anterior.get(segundo - 1));
        nueva.add(anterior.get(tercero - 1));
        nueva.add(anterior.get(cuarto - 1));

        return nueva;
    }
    
    private void recibirLlave(byte[] llave, String letra) {
        try {
            FileOutputStream fos = new FileOutputStream(dirBasePublica + letra + ".dat");
            fos.write(llave);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean desencriptar(int indice, int fase, int turno) {
        ArrayList<byte[]> conjunto = null;
        byte[] desencriptado = null;
        if (fase == 1) {
            byte[] des = cifrador.desencriptar(privateKey, mensajesFase1.get(indice));
            conjunto = eliminarCadena(des);

            desencriptado = cifradorAES.leerYdesencriptarCadena(5);
            mensajesFase1.set(indice, conjunto.get(0));
            return comprobarCadenas(conjunto.get(1), desencriptado);
        } else if (fase == 2) {
            byte[] des = cifrador.desencriptar(privateKey, mensajesFase2.get(indice));
            conjunto = eliminarCadena(des);

            int numCadena = (4 - myPosition) + 1;
            desencriptado = cifradorAES.leerYdesencriptarCadena(numCadena);

            mensajesFase2.set(indice, conjunto.get(0));
            return comprobarCadenas(conjunto.get(1), desencriptado);
        } else if (fase == 3) {
            byte[] des = cifrador.desencriptar(privateKey, mensajesFase3.get(indice));
            byte[] lectura = leerFichero(dirBaseEncriptados + "A");
            byte[] firma = cifrador.firmar(privateKey, des);

            byte[] enc = mensajesFase3.get(indice);

            mensajesFase3.set(indice, des);
            mensajesFase3Firmados.add(firma);
            return comprobarCadenas(enc, lectura);
        } else if (fase == 4) {
            byte[] des = cifrador.desencriptar(privateKey, mensajesFase4.get(indice));

            byte[] lectura = leerFichero(dirBaseEncriptados + letras[turno + 1]);
            byte[] firma = cifrador.firmar(privateKey, des);

            byte[] enc = mensajesFase4.get(indice);

            mensajesFase4.set(indice, des);
            mensajesFase4Firmados.add(firma);
            return comprobarCadenas(enc, lectura);
        } else {
            String cadenaFinal = new String(mensajesFase5.get(indice));
            String[] vectorFinal = cadenaFinal.split("#");

            String votoFinal = vectorFinal[0];
            BigInteger cadenaExtraida = new BigInteger(vectorFinal[1]);
            desencriptado = cifradorAES.leerYdesencriptarCadena(1);

            votosFinalesProvisional.add(votoFinal);
            return comprobarCadenas(cadenaExtraida.toByteArray(), desencriptado);
        }
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
