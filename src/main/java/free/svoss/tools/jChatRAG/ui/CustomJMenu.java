package free.svoss.tools.jChatRAG.ui;

import free.svoss.tools.jChatRAG.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

public class CustomJMenu extends JMenu {

    private final static Color fontColorGreen = new Color(43, 247, 63);
    private final MainWindow parent;
    boolean mouseInside = false;

    final ImageIcon finalIconHovered;
    final ImageIcon finalIconDisabled;
    final ImageIcon finalIconSelected;

    public CustomJMenu(String txt, String iconName, MainWindow parent) {
        super(txt);
        this.parent = parent;

        if (iconName != null) {

            ImageIcon iconDisabled = null;
            ImageIcon iconSelected = null;
            ImageIcon iconHovered = null;


            try {
                iconDisabled = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/icons_32/" + iconName + "_gray.png")));
            } catch (Exception ne) {
                Log.e("no gray " + iconName);
            }

            try {
                iconSelected = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/icons_32/" + iconName + "_green.png")));
            } catch (Exception ne) {
                Log.e("no green " + iconName);
            }

            try {
                iconHovered = new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/icons_32/" + iconName + "_hover.png")));
            } catch (Exception ne) {
                Log.e("no hovered " + iconName);
            }

            setIcon(iconDisabled);
            //setSelectedIcon(iconSelected);
            //setRolloverIcon(iconSelected);
            //setRolloverSelectedIcon(iconSelected);


            /*//
            setIcon();
            setDisabledIcon();
            setPressedIcon();
            setRolloverIcon();
            setDisabledSelectedIcon();
            setRolloverSelectedIcon();
            setSelectedIcon();

            //*/

            //setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/icons_32/" + iconName + "_gray.png"))));


            //setEnabled(!txt.equalsIgnoreCase("chat"));

            //setSelected(true);


            finalIconHovered = iconHovered;
            finalIconDisabled = iconDisabled;
            finalIconSelected = iconSelected;

            //*//
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {

                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseInside = true;
                    parent.updateMenuColors();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouseInside = false;
                    parent.updateMenuColors();
                }
            });

            //*/
        } else {

            finalIconHovered = null;
            finalIconDisabled = null;
            finalIconSelected = null;
        }

        addChangeListener(e -> updateColors());
        //updateColors();
    }

    public void updateColors() {
        SwingUtilities.invokeLater(() -> {
            if (mouseInside&&!isSelected()) {
                setIcon(finalIconHovered);
            }else if (isSelected()) {
                setIcon(finalIconSelected);
            } else   {
                setIcon(finalIconDisabled);
            }
        });

    }
}
