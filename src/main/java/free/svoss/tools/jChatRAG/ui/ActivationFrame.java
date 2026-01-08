package free.svoss.tools.jChatRAG.ui;

import ca.fredperr.customtitlebar.titlebar.TBJFrame;
import ca.fredperr.customtitlebar.titlebar.win.WindowFrameType;
import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Config;
import free.svoss.tools.jChatRAG.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.InputStream;

public class ActivationFrame extends TBJFrame {

    JTextField tf = new JTextField(20);
    JButton button = new JButton("OK");
    private final Config config = Config.getInstance();

    public ActivationFrame(String title, String msg) {
        super(//"<html><span style=\"font-weight:700;font-size:18px\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                title
                //+ "</span></html>"
                ,
                WindowFrameType.TOOL, App.theme, App.logoSize
        );

        Font font = new Font("SansSerif", Font.PLAIN, 14);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        if (msg != null) label.setText(MsgFrame.prepareMsg(msg));

        label.setForeground(App.theme.getTextColor());

        Container cp = getContentPane();
        JPanel container1 = new JPanel(new BorderLayout());
        container1.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        container1.setBackground(App.theme.getFrameBackground());
        cp.add(container1);
        container1.add(label, BorderLayout.NORTH);

        JPanel container2 = new JPanel(new BorderLayout());
        container1.add(container2, BorderLayout.CENTER);

        JPanel tfContainer = new JPanel();
        tfContainer.add(tf);
        tfContainer.setBorder(BorderFactory.createLineBorder(App.theme.getFrameBackground(), 16));
        container2.add(tfContainer, BorderLayout.NORTH);
        container2.add(button, BorderLayout.CENTER);
        tf.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(font);
        titleLabel.setForeground(Color.cyan);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        getCustomAreaPanel().add(titleLabel);

        setLocationRelativeTo(null);

        try {
            InputStream fis = getClass().getResourceAsStream("/img/icon_48/activation.png");
            setTitleBarIcon(ImageIO.read(fis));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        pack();


        button.setEnabled(false);

        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged();
            }
        });

        button.addActionListener(e -> setVisible(false));

        Utils.applyTheme(container1);
        Utils.applyTheme(container2);
        Utils.applyTheme(tf);
        Utils.applyTheme(button);
        Utils.applyTheme(tfContainer);


        //SwingUtilities.invokeLater(() -> tf.requestFocus());
        //tf.requestFocus();


        //setVisible(true);

        SwingUtilities.invokeLater(() -> {
            setResizable(false);
            setVisible(true);
            tf.requestFocus();
        });
    }

    public String getActivationCode() {
        return tf.getText();
    }

    private void textChanged() {
        button.setEnabled(config.isValidActivationCode(tf.getText()));
    }
}
