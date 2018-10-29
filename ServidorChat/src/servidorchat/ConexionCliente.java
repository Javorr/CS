
package servidorchat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;
import java.util.Arrays;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Random;
import java.security.SecureRandom;


public class ConexionCliente extends Thread implements Observer{

    private Logger log = Logger.getLogger(ConexionCliente.class);
    private Socket socket;
    private MensajesChat mensajes;
    private DataInputStream entradaDatos;
    private DataOutputStream salidaDatos;
    private boolean isMaster;
    private Key pub;
    private Key priv;
    private String pubstrKey;

    public ConexionCliente (Socket socket, MensajesChat mensajes, boolean isMaster){
        this.socket = socket;
        this.mensajes = mensajes;
        this.isMaster = isMaster;

        if(isMaster){ //Genera clave secreta AES
            KeyGenerator gensecreta = null; //Se instancia el generador de claves
            try {
                gensecreta = KeyGenerator.getInstance("AES");
                log.info(gensecreta);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            gensecreta.init(128); //Se inicializa de forma que se genere una clave de 128 bits
          SecretKey secr = gensecreta.generateKey(); //Se genera la clave secreta
            log.info(secr);

          SecureRandom random = new SecureRandom();
          byte[] iv = new byte[128/8]; //Generaremos el vector de inicializacion(iv)
          random.nextBytes(iv);
          IvParameterSpec ivspec = new IvParameterSpec(iv);
          log.info(iv);

        }
        else{ //Si no eres el master se genera el par de claves RSA
            KeyPairGenerator genpar = null; //Se instancia un generador de par de claves
            try {
                genpar = KeyPairGenerator.getInstance("RSA");
                log.info(genpar);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            genpar.initialize(2048); //La inicializamos con 2048 bits
          KeyPair par = genpar.generateKeyPair(); //Se genera el par de claves con la anterior instancia
            log.info(par);

          pub = par.getPublic(); //Ahora tenemos el par de claves, la publica y la privada
          priv = par.getPrivate();
          pubstrKey = Base64.getEncoder().encodeToString(pub.getEncoded());
        }


        try {
            entradaDatos = new DataInputStream(socket.getInputStream());
            salidaDatos = new DataOutputStream(socket.getOutputStream());
            if(isMaster) {

            }
            else {
                log.info(pub.getAlgorithm());
                log.info(priv);
                log.info(pubstrKey);
            }
        } catch (IOException ex) {
            log.error("Error al crear los stream de entrada y salida : " + ex.getMessage());
        }
    }

    @Override
    public void run(){
        String mensajeRecibido;
        boolean conectado = true;
        // Se apunta a la lista de observadores de mensajes
        mensajes.addObserver(this);

        while (conectado) {
            try {
                // Lee un mensaje enviado por el cliente
                mensajeRecibido = entradaDatos.readUTF();
                // Pone el mensaje recibido en mensajes para que se notifique
                // a sus observadores que hay un nuevo mensaje.
                mensajes.setMensaje(mensajeRecibido);
            } catch (IOException ex) {
                log.info("Cliente con la IP " + socket.getInetAddress().getHostName() + " desconectado.");
                conectado = false;
                // Si se ha producido un error al recibir datos del cliente se cierra la conexion con el.
                try {
                    entradaDatos.close();
                    salidaDatos.close();
                } catch (IOException ex2) {
                    log.error("Error al cerrar los stream de entrada y salida :" + ex2.getMessage());
                }
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            // Envia el mensaje al cliente
            salidaDatos.writeUTF(arg.toString());
            log.info("ENTRA A CONEXIONCLIENTE, UPDATE");
        } catch (IOException ex) {
            log.error("Error al enviar mensaje al cliente (" + ex.getMessage() + ").");
        }
    }
}
