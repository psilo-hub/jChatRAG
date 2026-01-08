package ca.fredperr.customtitlebar.titlebar.theme;

import javax.swing.*;
import java.awt.*;

public class DarkTBTheme implements TBTheme{
    public DarkTBTheme(){
        setupUIManager();
    }

    private void setupUIManager() {

        /*//
        try {
            //UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
//*/

        Font fontMenu = new Font ("SansSerif", Font.PLAIN, 15);
        Font fontLabel = new Font ("SansSerif", Font.PLAIN, 20);

        Color fontColorGreen = new Color(43, 247, 63);


        UIManager.put("Menu.selectionBackground", new Color(177, 96, 96));
        //UIManager.put("Menu.selectionForeground", Color.lightGray);
        UIManager.put("Menu.selectionForeground", fontColorGreen);
        UIManager.put("MenuBar.background", getFrameBackground());
        UIManager.put("Menu.background", getFrameBackground());
        UIManager.put("Menu.foreground", getTextColor());
        UIManager.put("Menu.border", BorderFactory.createEmptyBorder(5,2,5,2));
        //UIManager.put("Menu.margin", new Insets(0,0,0,0));
        UIManager.put("Menu.font", fontMenu);
        UIManager.put("Menu.borderPainted", true);


        UIManager.put("MenuItem.selectionBackground", new Color(177, 96, 96));
        UIManager.put("MenuItem.selectionForeground", fontColorGreen);
        UIManager.put("MenuItem.background", getFrameBackground());
        UIManager.put("MenuItem.borderPainted", true);
        UIManager.put("MenuItem.foreground", getTextColor());
        //UIManager.put("MenuItem.margin", new Insets(0,0,0,0));//BorderFactory.createEmptyBorder(0,0,0,0));//
        UIManager.put("MenuItem.border", BorderFactory.createEmptyBorder(5,8,5,8));
        UIManager.put("MenuItem.font", fontMenu);
        UIManager.put("Label.font", fontLabel);
        UIManager.put("Label.foreground", getTextColor());
        //UIManager.put("JLabel.font", fontLabel);



        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder( Color.lightGray,1,false));


        UIManager.put("PopupMenu.background", getFrameBackground());




    }

    @Override
    public Color getFrameBackground() {
        return new Color(62, 62, 62);
    }

    @Override
    public Color getFrameBorder() {
        return new Color(62, 62, 62);
    }

    @Override
    public Color getTitleBarColorBackground() {
        return new Color(62, 62, 62);
    }

    @Override
    public Color getTitleBarBorder() {
        return new Color(67, 67, 67);
    }

    @Override
    public Color getCloseButtonHoverBackground() {
        return new Color(173, 54, 54);
    }

    @Override
    public Color getControlButtonHoverBackground() {
        return new Color(90, 90, 90);
    }

    @Override
    public Color getControlButtonBackground() {
        return new Color(62, 62, 62);
    }

    @Override
    public Color getTextColor() {
        return Color.lightGray;
    }
}
