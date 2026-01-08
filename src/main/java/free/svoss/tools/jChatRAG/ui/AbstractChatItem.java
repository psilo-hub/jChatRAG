package free.svoss.tools.jChatRAG.ui;

import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class AbstractChatItem extends JPanel {
    protected String originalText;
    JLabel l;
    private final Container parent;

    protected AbstractChatItem(String text, Container parent) {

        this.parent = parent;
        System.out.println("PARENT: " + parent.getClass().getName());


        //setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setBorder(BorderFactory.createLineBorder(App.theme.getFrameBackground(), 8));

        setBackground(App.theme.getFrameBackground());

        setLayout(new BorderLayout());

        this.originalText = text;

        //CustomJLabel l =new CustomJLabel(htmlFormatText(text),22);
        //JLabel l =new JLabel(text);
        l = new JLabel(htmlFormatText(text, parent.getSize().width));
        l.setFont(l.getFont().deriveFont(42f));
        l.setForeground(App.theme.getTextColor());
        l.setBackground(App.theme.getFrameBackground());
        l.setHorizontalAlignment(SwingConstants.LEFT);

        l.setBorder(BorderFactory.createLineBorder(
                (this instanceof ResponseChatItem) ? Color.lightGray : Color.cyan
                , 1));

        add(l, BorderLayout.CENTER);

        this.parent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                Dimension pSize = parent.getSize();
                setMaximumSize(pSize);
                l.setMaximumSize(pSize);
                l.setText(htmlFormatText(originalText, pSize.width));
            }
        });

        //super.setOpaque(true);
        //l.setOpaque(true);

        //l.setText("<html>Test<b>Test</b></html>");
        Dimension pSize = parent.getSize();
        setMaximumSize(pSize);
        l.setMaximumSize(pSize);

    }

    protected String htmlFormatText(String text, int px) {
        px = Math.max(0, px - 280);

        if (text == null) return "";

        text = text.trim();

        //System.out.println("user : "+(this instanceof UserPromptChatItem));
        //System.out.println("resp : "+(this instanceof ResponseChatItem));

        if (this instanceof ResponseChatItem) text = replaceSimpleMarkdown(text);

        while (text.contains("\n")) text = text.replace("\n", "<br>");


        //if(!text.startsWith("<html>"))
        //    text="<html><body style=\"text-align: justify; font-size: 22px; text-justify: inter-word; width: 1spx\">"+text+"</body></html>";
        //text="<html><body style=\"text-align: justify; font-size: 22px; text-justify: inter-word;\">"+text+"</body></html>";
        //<html><body style='width: %1spx'>
        final String s = text;
        final String html = "<html><body style='text-align: justify; font-size: 18px; text-justify: inter-word; width: %1spx'>%1s";
        text = String.format(html, px, s);
        //text="<html>"+text;//+"</html>";

        //System.out.println(text);

        return text;
    }


    private String replaceSimpleMarkdown(String text){
        return Utils.convertMDtoHtml(text);

    }

    private String replaceSimpleMarkdown2(String text) {
        if (text == null || text.trim().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n"))
            sb.append(replaceSimpleMarkdownInLine(line.trim())).append("\n");

        return sb.toString().trim();
    }

    private String replaceSimpleMarkdownInLine(String line) {
        //headings
        if (line.startsWith("# ")) return "<h1>" + line.substring(2).trim() + "</h1>";
            //else if (line.startsWith("## ")) return "<h2>" + line.substring(3).trim() + "</h2>";
            //else if (line.startsWith("### ")) return "<h3>" + line.substring(4).trim() + "</h3>";
        else if (line.startsWith("## ")) return "<h1>" + line.substring(3).trim() + "</h1>";
        else if (line.startsWith("### ")) return "<h1>" + line.substring(4).trim() + "</h1>";
        else if (line.contains("*")) {

            if (line.contains("***")) return replaceSimpleMarkdownInLine(replaceBoldItalic(line));
            else if (line.contains("**")) return replaceSimpleMarkdownInLine(replaceBold(line));
            else return replaceSimpleMarkdownInLine(replaceItalic(line));

        }

        return line;
    }

    private String replaceBoldItalic(String line) {
        return alternatingReplace(line, "***", "<b><i>", "</i></b>");
    }

    private String replaceBold(String line) {
        return alternatingReplace(line, "**", "<b>", "</b>");
    }

    private String replaceItalic(String line) {
        return alternatingReplace(line, "*", "<i>", "</i>");
    }

    private String alternatingReplace(String line, String s, String r1, String r2) {
        if (line == null || s == null||s.isEmpty() || !line.contains(s)) return line;

        boolean first=true;

        while (line.contains(s)){

            line=replaceFirstOccurance(line,s,first?r1:r2);
            first=!first;

        }

        return line;
    }

    private String replaceFirstOccurance(String line, String s, String r) {
        if (line == null || s == null||s.isEmpty() || !line.contains(s)) return line;

        //System.out.println("Line : "+line);
        //System.out.println("   s : "+s);
        //System.out.println("   r : "+r);
        int pos = line.indexOf(s);

        //System.out.println(" pos : "+pos);

        String pref=pos==0?"":line.substring(0,pos);
        //System.out.println("pref : "+pref);

        String suff=pos==0?"":line.substring(pos+s.length());
        //System.out.println("suff : "+suff);

        return pref+r+suff;

    }


}
