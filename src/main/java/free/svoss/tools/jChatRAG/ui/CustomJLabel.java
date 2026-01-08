package free.svoss.tools.jChatRAG.ui;

import javax.swing.*;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class CustomJLabel extends JLabel {
    private final float fontSize;
    public CustomJLabel(String text,float fontSize){
        super(text);
        this.fontSize=fontSize;
    }
    public CustomJLabel(float fontSize){
        super();
        this.fontSize=fontSize;
    }

    @Override
    public void setFont(Font f){
        if(f!=null)
            super.setFont(f.deriveFont(this.fontSize));

    }
    @Override
    public Font getFont(){
        Font f=super.getFont();
        return f==null?null:f.deriveFont(fontSize);

    }

    @Override
    protected void paintComponent(Graphics g) {
        // We're overriding the default painting behavior.
        super.paintComponent(g);
    }

    @Override
    public void setUI(LabelUI ui) {
        super.setUI(new BasicLabelUI());

    }

}
