package clientechat;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.apache.log4j.Logger;


public class VentanaConfiguracion extends JDialog{

    private Logger log = Logger.getLogger(VentanaConfiguracion.class);

    public VentanaTutorial(JFrame padre) {
        super(padre, "Configuracion inicial", true);

        URL url = new URL("C:\\gif");
        Icon icon = new ImageIcon(url);
        JLabel lbGif = new JLabel(icon);

        JFrame f = new JFrame("Animation");
        f.getContentPane().add(lbGIF);

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

        gbc.insets = new Insets(20, 20, 0, 20);

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
