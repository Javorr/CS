
package clientechat;

import org.apache.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;


public class ConexionServidor implements ActionListener {

    private Logger log = Logger.getLogger(ConexionServidor.class);
    private Socket socket;
    private JTextField tfMensaje;
    private String usuario;
    private DataOutputStream salidaDatos;
    static AtomicInteger sigId = new AtomicInteger();
    private int id;

    public ConexionServidor(Socket socket, JTextField tfMensaje, String usuario) {
        this.socket = socket;
        this.tfMensaje = tfMensaje;
        this.usuario = usuario;
        this.id=sigId.incrementAndGet(); //MIRAR ESTO LO DE LAS IDS ETC
        try {
            this.salidaDatos = new DataOutputStream(socket.getOutputStream());
            salidaDatos.writeUTF("Mensaje desde conexion servidor");
        } catch (IOException ex) {
            log.error("Error al crear el stream de salida : " + ex.getMessage());
        } catch (NullPointerException ex) {
            log.error("El socket no se creo correctamente. ");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Key pub;
            Key priv;
            String pubstrKey;
            KeyPairGenerator genpar = null; //Se instancia un generador de par de claves

        //if(isMaster){ //Genera clave secreta AES
            KeyGenerator gensecreta = null; //Se instancia el generador de claves
            try {
                gensecreta = KeyGenerator.getInstance("AES");
                log.info(gensecreta);
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }

            gensecreta.init(128); //Se inicializa de forma que se genere una clave de 128 bits
          SecretKey secr = gensecreta.generateKey(); //Se genera la clave secreta
            log.info(secr);

          SecureRandom random = new SecureRandom();
          byte[] iv = new byte[128/8]; //Generaremos el vector de inicializacion(iv)
          random.nextBytes(iv);
          IvParameterSpec ivspec = new IvParameterSpec(iv);
          log.info(iv);

        //}
        //else{ //Si no eres el master se genera el par de claves RSA
            genpar = null; //Se instancia un generador de par de claves
            try {
                genpar = KeyPairGenerator.getInstance("RSA");
                log.info(genpar);
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            }

            genpar.initialize(2048); //La inicializamos con 2048 bits
          KeyPair par = genpar.generateKeyPair(); //Se genera el par de claves con la anterior instancia
            log.info(par);

          pub = par.getPublic(); //Ahora tenemos el par de claves, la publica y la privada
          priv = par.getPrivate();
          pubstrKey = Base64.getEncoder().encodeToString(pub.getEncoded());

            salidaDatos.writeUTF(pubstrKey);
        //}

            tfMensaje.setText("");
            log.info("ENTRA EN CONEXIONSERVIDOR, ACTIONPERFORMED");
        } catch (IOException ex) {
            log.error("Error al intentar enviar un mensaje: " + ex.getMessage());
        }
    }

}
