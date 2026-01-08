package free.svoss.tools.jChatRAG.ui;

import free.svoss.tools.jChatRAG.App;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class Splash extends JFrame {
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Color bgColor = new Color(37, 37, 37);
    Color fgColor = new Color(183, 183, 183);

    JLabel msgLabel = new JLabel();

    public Splash(String s) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        Container cp = getContentPane();

        cp.setBackground(bgColor);

        URL url = null;
        try {
            url = App.class.getClassLoader().getResource("img/cubes.gif");
        } catch (Exception ignored) {
        }
        Image image = null;
        if (url != null)
            try {
                image = new ImageIcon(url).getImage();
            } catch (Exception ignored) {
            }

        if (image != null) {
            JLabel label = new JLabel(new ImageIcon(image));
            label.setBorder(BorderFactory.createEmptyBorder(0, 20, -10, 20));
            cp.add(label, BorderLayout.CENTER);
        }
        msgLabel.setText(s == null ? " " : s);
        cp.add(msgLabel, BorderLayout.SOUTH);

        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        //msgLabel.setBackground(bgColor);
        msgLabel.setForeground(fgColor);
        msgLabel.setBorder(BorderFactory.createEmptyBorder(-8, 12, 12, 12));

        msgLabel.setFont(msgLabel.getFont().deriveFont(24f));

        setUndecorated(true);


        pack();
        setResizable(false);
        setLocationRelativeTo(null);


        setVisible(image != null);
    }


    public void setMessage(String msg) {
        setVisible(true);
        msgLabel.setText(msg == null ? " " : msg);
    }
}
