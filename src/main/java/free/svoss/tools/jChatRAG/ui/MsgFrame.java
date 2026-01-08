package free.svoss.tools.jChatRAG.ui;

import ca.fredperr.customtitlebar.titlebar.TBJFrame;
import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import ca.fredperr.customtitlebar.titlebar.win.WindowFrameType;
import free.svoss.tools.jChatRAG.App;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Locale;

public class MsgFrame extends TBJFrame {
    public MsgFrame(String title, String msg,Color titleColor,String icon) {
        super(title, WindowFrameType.TOOL, App.theme, App.logoSize);
        Font font = new Font ("SansSerif", Font.PLAIN, 14);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel label = new JLabel();
        if(msg!=null)label.setText(prepareMsg(msg));

        label.setForeground(App.theme.getTextColor());

        Container cp = getContentPane();
        JPanel container=new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        container.setBackground(App.theme.getFrameBackground());
        cp.add(container);
        container.add(label);


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

    public static String prepareMsg(String msg) {
        if(msg==null)return "";
        if(!msg.toLowerCase(Locale.ROOT).startsWith("<html>"))msg="<html>"+msg;
        while (msg.contains("\n"))msg=msg.replace("\n","<br>");
        return msg;
    }
}
