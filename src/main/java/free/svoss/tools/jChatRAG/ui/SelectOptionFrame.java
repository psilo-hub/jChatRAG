package free.svoss.tools.jChatRAG.ui;

import ca.fredperr.customtitlebar.titlebar.TBJFrame;
import ca.fredperr.customtitlebar.titlebar.win.WindowFrameType;
import free.svoss.tools.jChatRAG.App;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

public class SelectOptionFrame extends TBJFrame {

    public SelectOptionFrame(String title, String msg, Color titleColor, String icon,String[] options) {
        super(title, WindowFrameType.TOOL, App.theme, App.logoSize);
        Font font = new Font ("SansSerif", Font.PLAIN, 14);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        if(msg!=null)label.setText(MsgFrame.prepareMsg(msg));

        label.setForeground(App.theme.getTextColor());

        Container cp = getContentPane();
        JPanel container=new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        container.setBackground(App.theme.getFrameBackground());
        cp.add(container);
        container.add(label,BorderLayout.CENTER);
        container.add(createButtonPanel(options),BorderLayout.SOUTH);


        JLabel titleLabel = new JLabel(getTitle());
        titleLabel.setFont(font);
        titleLabel.setForeground(titleColor);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));

        getCustomAreaPanel().add(titleLabel);

        setLocationRelativeTo(null);

        try {
            InputStream fis = getClass().getResourceAsStream("/img/" + icon);
            setTitleBarIcon(ImageIO.read(fis));
        }catch (Exception ex){ex.printStackTrace();}

        pack();

        setResizable(false);

        setVisible(true);
    }

    String selectedOption=null;

    private JPanel createButtonPanel(String[] options) {
        Color bg=App.theme.getFrameBackground();
        Color fg=App.theme.getTextColor();

        JPanel p = new JPanel(new GridLayout(1,options.length*2+1));
        p.setBackground(bg);
        JLabel fl = new JLabel(" ");
        fl.setBackground(bg);
        p.add(fl);
        for(String opt : options){
            JButton b = new JButton(opt);
            p.add(b);
            b.setBackground(bg);
            b.setForeground(fg);
            b.setFont(b.getFont().deriveFont(16f));


            JLabel l = new JLabel(" ");
            l.setBackground(bg);
            p.add(l);

            b.addActionListener(e -> {
                selectedOption=opt;
                setVisible(false);
            });
        }
        return p;
    }

    public String getSelectedOption() {return selectedOption;
    }
}
