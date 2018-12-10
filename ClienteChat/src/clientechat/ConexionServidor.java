
package clientechat;

import org.apache.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ConexionServidor implements ActionListener {

    private Logger log = Logger.getLogger(ConexionServidor.class);
    private Socket socket;
    private JTextField tfMensaje;
    private String usuario;
    private DataOutputStream salidaDatos;
    private int rand=(int)(Math.random() * 50 + 1);
    private int id;
    private boolean isMaster;
    private Key RSA_pub;
    private Key RSA_priv;
    private SecretKey AES_secr;
    private IvParameterSpec spec;

    public ConexionServidor(Socket socket, JTextField tfMensaje, String usuario) {
        this.socket = socket;
        this.tfMensaje = tfMensaje;
        this.usuario = usuario;
        this.id = rand;
        this.isMaster = false;
        log.info("Se esta creando la ConexionServidor");
        try {
            this.salidaDatos = new DataOutputStream(socket.getOutputStream());
            salidaDatos.writeUTF("ID: "+id + " " + usuario);
        } catch (IOException ex) {
            log.error("Error al crear el stream de salida : " + ex.getMessage());
        } catch (NullPointerException ex) {
            log.error("El socket no se creo correctamente. ");
        }
    }

    public void enviar_mensaje() {
        try {
            log.info("Se esta enviando el mensaje");
            salidaDatos.writeUTF("Se ha enviado el mensaje");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void crear_AES() {
        isMaster = true;
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
        log.info("He generado la clave AES");
        AES_secr = secr;
        log.info("CLAVE SECRETA AES GENERADA EN KEY: " + AES_secr);
        String aesStr = Base64.getEncoder().encodeToString(AES_secr.getEncoded());
        log.info("CLAVE SECRETA AES GENERADA EN STRING: " + aesStr);
        spec = ivspec;

        try {
            salidaDatos.writeUTF("El usuario "+usuario+" se ha conectado");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void enviar_puk(String[] splitStr) {
        usuario = splitStr[1];
        Key pub;
        Key priv;
        String pubstrKey;
        KeyPairGenerator genpar = null; //Se instancia un generador de par de claves

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
        pubstrKey="PUK " + this.id + " " + pubstrKey;
        RSA_pub = pub;
        RSA_priv = priv;
        try {
            log.info("Esto es lo que enviamos a servidor: "+pubstrKey);
            salidaDatos.writeUTF(pubstrKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gestionar_puk(String[] splitStr) {
        if(isMaster){
            log.info("Entra en gestionar la puk del cliente con id: " + splitStr[1]);

            Cipher cifrado = null;
            byte[] claveSecrCifrada = null;

            try {
                cifrado = Cipher.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }



            byte[] publicBytes;
            publicBytes = Base64.getDecoder().decode(splitStr[2]);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = null;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            PublicKey clavePub = null;
            try {
                clavePub = keyFactory.generatePublic(keySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

            try {
                cifrado.init(Cipher.ENCRYPT_MODE, clavePub);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            try {
                claveSecrCifrada = cifrado.doFinal(AES_secr.getEncoded());
                String claveSecrCifradaStr = Base64.getEncoder().encodeToString(claveSecrCifrada);
                try {
                    log.info("ESTA ES LA CLAVE SECRETA CIFRADA: " + claveSecrCifradaStr);
                    salidaDatos.writeUTF("CSCIFRADA " + splitStr[1] + " " + claveSecrCifradaStr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }
        }
    }

    public void descifrar_AES(String[] splitStr) {
        if(splitStr[1].equalsIgnoreCase(Integer.toString(id))) {
            log.info("CLAVE SECRETA AES RECIBIDA EN STRING: " + splitStr[2]);

            byte[] secreta_descrifrada = Base64.getDecoder().decode(splitStr[2]);
            SecretKey originalKey = new SecretKeySpec(secreta_descrifrada, 0, secreta_descrifrada.length, "AES");


            Cipher rsa=null;
            try {
                rsa = Cipher.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            try {
                rsa.init(Cipher.DECRYPT_MODE, RSA_priv);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            try {
                byte[] secretBytes = rsa.doFinal(secreta_descrifrada);
                AES_secr = new SecretKeySpec(secretBytes, 0, secretBytes.length, "AES");
                String aesStr = Base64.getEncoder().encodeToString(AES_secr.getEncoded());
                log.info("SOY EL USUARIO: "+ splitStr[1] + " y tengo la clave AES " + aesStr);

            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

            try {
                salidaDatos.writeUTF("El usuario "+usuario+" se ha conectado");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String descifrarmensaje(String mensaje) {
        log.info("A descifrar el mensaje " + mensaje);

        String textoplano=null;
        byte[] mnsj = Base64.getDecoder().decode(mensaje);

        Cipher cifrado = null;
        try {
            cifrado = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            cifrado.init(Cipher.DECRYPT_MODE, AES_secr);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        try {
            textoplano = new String(cifrado.doFinal(mnsj));
            log.info("Se ha densencriptado: " + textoplano);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    return textoplano;

    }

    public void cambio_master(String[] splitStr) {
        if(splitStr[1].equalsIgnoreCase(this.usuario)) {
            isMaster = true;
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        try {

            //Cifrar el mensaje
            String textoplano = usuario+": "+ tfMensaje.getText();
            log.info("Vamos a cifrar el mensaje: " + textoplano);

            Cipher cifrado = null;
            try {
                cifrado = Cipher.getInstance("AES/ECB/PKCS5Padding");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            } catch (NoSuchPaddingException e1) {
                e1.printStackTrace();
            }

            try {
                cifrado.init(Cipher.ENCRYPT_MODE, AES_secr);
            } catch (InvalidKeyException e1) {
                e1.printStackTrace();
            }

            try {
                String textoCifrado = Base64.getEncoder().encodeToString(cifrado.doFinal(textoplano.getBytes("UTF-8")));
                log.info("Se ha cifrado el mensaje: " + textoCifrado);
                salidaDatos.writeUTF(textoCifrado);
            } catch (IllegalBlockSizeException e1) {
                e1.printStackTrace();
            } catch (BadPaddingException e1) {
                e1.printStackTrace();
            }

            tfMensaje.setText("");

        } catch (IOException ex) {
            log.error("Error al intentar enviar un mensaje: " + ex.getMessage());
        }
    }

}
