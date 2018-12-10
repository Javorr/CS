
package clientechat;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClienteChat extends JFrame {

    private Logger log = Logger.getLogger(ClienteChat.class);
    private JTextArea mensajesChat;
    private JTextArea usuariosChat;
    private Font font;
    private Socket socket;

    private int puerto;
    private String host;
    private String usuario;
    private ConexionServidor cs;

    public ClienteChat(){
        super("Cliente Chat");

        // Elementos de la ventana
        mensajesChat = new JTextArea();
        font = new Font("Arial", Font.BOLD, 20);
        mensajesChat.setFont(font);
        mensajesChat.setForeground(Color.BLUE);
        mensajesChat.setEnabled(false); // El area de mensajes del chat no se debe de poder editar
        mensajesChat.setLineWrap(true); // Las lineas se parten al llegar al ancho del textArea
        mensajesChat.setWrapStyleWord(true); // Las lineas se parten entre palabras (por los espacios blancos)

        usuariosChat = new JTextArea();
        usuariosChat.setFont(font);
        usuariosChat.setEnabled(false); // El area de mensajes del chat no se debe de poder editar
        usuariosChat.setLineWrap(true); // Las lineas se parten al llegar al ancho del textArea
        usuariosChat.setWrapStyleWord(true); // Las lineas se parten entre palabras (por los espacios blancos)

        JScrollPane scrollMensajesChat = new JScrollPane(mensajesChat);

        JScrollPane scrollusuariosChat = new JScrollPane(usuariosChat);

        JTextField tfMensaje = new JTextField("");
        JButton btEnviar = new JButton("Enviar");
        tfMensaje.setForeground(Color.BLUE);

        // Colocacion de los componentes en la ventana
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.setBackground(Color.pink);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(20, 20, 20, 380);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(scrollMensajesChat, gbc);
        // Restaura valores por defecto
        gbc.gridwidth = 1;
        gbc.weighty = 0;

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 20, 20, 10);

        gbc.gridx = 0;
        gbc.gridy = 1;
        c.add(tfMensaje, gbc);


        // Restaura valores por defecto
        gbc.weightx = 0;

        gbc.gridx = 1;
        gbc.gridy = 1;
        c.add(btEnviar, gbc);


        gbc.insets = new Insets(20, 850, 20, -40);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(scrollusuariosChat, gbc);

        this.setBounds(400, 100, 1200, 600);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Ventana de configuracion inicial
        VentanaConfiguracion vc = new VentanaConfiguracion(this);
        host = vc.getHost();
        puerto = vc.getPuerto();
        usuario = vc.getUsuario();

        //VentanaTutorial vt = new VentanaTutorial(this);

        log.info("Quieres conectarte a " + host + " en el puerto " + puerto + " con el nombre de ususario: " + usuario + ".");

        // Se crea el socket para conectar con el Sevidor del Chat
        try {
            socket = new Socket(host, puerto);
        } catch (UnknownHostException ex) {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        } catch (IOException ex) {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        }

        // Accion para el boton enviar
        cs = new ConexionServidor(socket, tfMensaje, usuario);
        btEnviar.addActionListener(cs);
        tfMensaje.addActionListener(cs);
    }

    /**
     * Recibe los mensajes del chat reenviados por el servidor
     */
    public void recibirMensajesServidor(){
        // Obtiene el flujo de entrada del socket
        DataInputStream entradaDatos = null;
        String mensaje;
        try {
            entradaDatos = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            log.error("Error al crear el stream de entrada: " + ex.getMessage());
        } catch (NullPointerException ex) {
            log.error("El socket no se creo correctamente. ");
        }

        // Bucle infinito que recibe mensajes del servidor
        boolean conectado = true;
        while (conectado) {
            try {
                mensaje = entradaDatos.readUTF();
                String[] splitStr = mensaje.trim().split("\\s+");

                switch (splitStr[0]){
                    case "Master":
                        cs.crear_AES();
                        break;

                    case "Nomaster":
                        cs.enviar_puk(splitStr);
                        break;

                    case"PUK":
                        cs.gestionar_puk(splitStr);
                        break;

                    case "CSCIFRADA":
                        cs.descifrar_AES(splitStr);
                        break;

                    case "CAMBIO":
                        log.info("VAMOS A CAMBIAR DE MASTER " + mensaje);
                        cs.cambio_master(splitStr);
                        break;

                    case "LISTA":
                        usuariosChat.setText(" USUARIOS CONECTADOS:" + System.lineSeparator() + System.lineSeparator());
                        for(int i = 0; i<splitStr.length-1; i++){
                            usuariosChat.append(" " + splitStr[i+1] + System.lineSeparator());
                        }
                        break;
                    case "El":
                        mensajesChat.append(mensaje+System.lineSeparator());
                        break;
                    default:
                        log.info("DEFAULT " + mensaje);
                        mensaje = cs.descifrarmensaje(mensaje);



                        mensaje = mensaje.replace(":happy:", ":)");
                        mensaje = mensaje.replace(":sad:", ":(");
                        mensaje = mensaje.replace(":angry:", "ì_í");
                        mensaje = mensaje.replace(":surprised:", "owo");
                        mensaje = mensaje.replace(":wink:", "ewe");
                        mensaje = mensaje.replace(":sick:", "uwu");
                        mensaje = mensaje.replace(":confused:", ".-.");
                        mensaje = mensaje.replace(":cry:", ":_(");
                        mensaje = mensaje.replace(":challenge:", ":P");
                        mensaje = mensaje.replace(":laughing:", "xD");
                        mensaje = mensaje.replace(":sunglasses:", "8^)");
                        mensaje = mensaje.replace(":cat:", "^o.o^");
                        mensaje = mensaje.replace(":kiss:", "<3");
                        mensaje = mensaje.replace(":thinking:", ":v");
                        mensaje = mensaje.replace(":friendly:", ":D");
                        mensaje = mensaje.replace(":poker:", ":|");
                        mensaje = mensaje.replace(":annoyed:", ">.<");
                        mensaje = mensaje.replace(":hug:", "(>o.o)>");
                        mensaje = mensaje.replace(":mood:", "( ͡° ͜ʖ ͡°)");
                        mensaje = mensaje.replace(":nice:", ":3");
                        mensaje = mensaje.replace(":joyful:", "/(^~^)/");
                        mensaje = mensaje.replace(":regulinchis:", ":/");
                        mensaje = mensaje.replace(":toto:", "I BLESS THE RAINS DOWN IN AFRICA \n ░░░░░░▄▄▓██▓░░░░░░░░░░░░░░░░ \n ░░░░▄████████▓░▄▓▄▄▄░░░░░░░░ \n░░░▄████████████████░░░░░░░░ \n░░▓██████████████████░░░░░░░ \n░░███████████████████▓░░░░░░ \n░░████████████████████▓░░░░░ \n░░▄█████████████████████▄▄░░ \n░░░▒▓███▓▓████████████████░░ \n░░░░░░░░░░░▄████████████▓░░░ \n░░░░░░░░░░░▄███████████░░░░░ \n░░░░░░░░░░░░▄█████████░░░░░░ \n░░░░░░░░░░░░░█████████▓░░░░░ \n░░░░░░░░░░░░▄█████████▓░░▄░░ \n░░░░░░░░░░░░▄████████░░░██░░ \n░░░░░░░░░░░░░███████▓░░░█░░░ \n ░░░░░░░░░░░░░░▄███▓ ");





                                String[] splitStr2 = mensaje.trim().split("\\s+");
                        if(splitStr2.length>1) mensajesChat.append(mensaje + System.lineSeparator());
                        break;
                }




            } catch (IOException ex) {
                log.error("Error al leer del stream de entrada: " + ex.getMessage());
                conectado = false;
            } catch (NullPointerException ex) {
                log.error("El socket no se creo correctamente. ");
                conectado = false;
            }
        }
    }

    public void enviarLog() {
        log.info("Me he conectado, fin del main");
    }
    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        // Carga el archivo de configuracion de log4J
        PropertyConfigurator.configure("log4j.properties");

        ClienteChat c = new ClienteChat();
        c.recibirMensajesServidor();
        c.enviarLog();
    }

}
