
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
    private static int idMaster=0;
    private int id;

    public ConexionCliente (Socket socket, MensajesChat mensajes, boolean isMaster, int identificador){
        this.socket = socket;
        this.mensajes = mensajes;
        this.isMaster = isMaster;
        this.id = identificador;


        try {
            entradaDatos = new DataInputStream(socket.getInputStream());
            salidaDatos = new DataOutputStream(socket.getOutputStream());

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
                log.info("Se ha recibido el mensaje: "+mensajeRecibido);
                String[] splitStr = mensajeRecibido.trim().split("\\s+");
                if(splitStr[0].equalsIgnoreCase("ID:")) {
                    log.info("Hemos detectado el id");

                    if(idMaster==0) {
                        idMaster=Integer.parseInt(splitStr[1]);
                        id=idMaster;
                        log.info("Hemos asignado el id del master");
                        salidaDatos.writeUTF("Master");
                    }
                    else {
                        salidaDatos.writeUTF("Nomaster");
                        id=Integer.parseInt(splitStr[1]);
                        log.info("Ya esta asignado, mi id es: "+id);
                        log.info("Y el id del master asginado es: "+idMaster);
                    }
                }

                else if(splitStr[0].equalsIgnoreCase("PUK")) {
                    log.info("Me han enviado una clave publica, con ID: "+splitStr[1]);
                    mensajes.setMensaje(mensajeRecibido);


                } else if(splitStr[0].equalsIgnoreCase("CSCIFRADA")){
                    log.info("Ha llegado la clave AES cifrada para ID: "+splitStr[1]);
                    mensajes.setMensaje(mensajeRecibido);
                }
                else {
                    mensajes.setMensaje(mensajeRecibido);
                }

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
            if(isMaster) {
                log.info("Ha entrado a update: "+arg.toString());
            }
            salidaDatos.writeUTF(arg.toString());
            log.info("ENTRA A CONEXIONCLIENTE, UPDATE");
        } catch (IOException ex) {
            log.error("Error al enviar mensaje al cliente (" + ex.getMessage() + ").");
        }
    }
}
