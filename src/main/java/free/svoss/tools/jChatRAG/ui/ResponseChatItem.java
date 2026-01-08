package free.svoss.tools.jChatRAG.ui;

import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ResponseChatItem extends AbstractChatItem {
    public ResponseChatItem(String response, Container parent) {
        super(response, parent);
        //System.out.println(response+"\n\n");
        //add(new JLabel(response));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    doPop(e);
            }
        });
    }

    private void doPop(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        CustomJMenuItem copyPlain = new CustomJMenuItem("copy", "copy", null);
        CustomJMenuItem copyHtml = new CustomJMenuItem("copy html", "html", null);

        menu.add(copyPlain);
        menu.add(copyHtml);

        menu.show(e.getComponent(), e.getX(), e.getY());

        copyPlain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                Utils.copyTextToClipboard(ResponseChatItem.super.originalText);
            }
        });

        copyHtml.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menu.setVisible(false);
                Utils.copyTextToClipboard(
                        "<html>\n<body>\n"
                                + Utils.convertMDtoHtml(ResponseChatItem.super.originalText)
                                + "\n</body>\n</html>"
                );
            }
        });


    }

    public void updateText(String lastResponse) {
        this.originalText=lastResponse;
        l.setText(htmlFormatText(lastResponse, this.getSize().width));
        l.invalidate();
        l.repaint();
    }
}
