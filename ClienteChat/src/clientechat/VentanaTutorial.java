package clientechat;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;


public class VentanaTutorial extends JDialog{

    private Logger log = Logger.getLogger(VentanaConfiguracion.class);

    public VentanaTutorial(JFrame padre) {
        super(padre, "Configuracion inicial", true);

        URL url = null;
        try {
            url = new URL("file:///C:/cs.gif");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Icon icon = new ImageIcon(String.valueOf(url));
        JLabel lbGif = new JLabel(icon);

        JFrame f = new JFrame("Animation");

        f.getContentPane().add(lbGif);

        JButton btAceptar = new JButton("Aceptar");
        btAceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(1000, 1000, 1000, 1000);

        gbc.gridx = 0;
        gbc.gridy = 0;
        c.add(lbGif, gbc);

        gbc.ipadx = 100;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        c.add(btAceptar, gbc);

        this.pack(); // Le da a la ventana el minimo tamaño posible
        this.setLocation(450, 200); // Posicion de la ventana
        this.setResizable(false); // Evita que se pueda estirar la ventana
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Deshabilita el boton de cierre de la ventana
        this.setVisible(true);
    }
}
