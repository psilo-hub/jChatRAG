package free.svoss.tools.jChatRAG.ui.tabPanels;

import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Config;
import free.svoss.tools.jChatRAG.Log;
import free.svoss.tools.jChatRAG.Utils;
import free.svoss.tools.jChatRAG.ui.MainWindow;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class SettingsPanel extends MWTab {
    final ImageIcon buttonLight;
    final ImageIcon buttonLightHovered;
    final ImageIcon buttonDark;
    final ImageIcon buttonDarkHovered;


    private final MainWindow mainWindow;

    @Override
    public void disposeMWTab() {
        // todo implement ollama config check
        Log.w("DUMMY");
    }

    public SettingsPanel(MainWindow mw) {

        // todo show license info

        mainWindow = mw;

        buttonLight = loadButtonImage("light");
        buttonLightHovered = loadButtonImage("light_hover");
        buttonDark = loadButtonImage("dark");
        buttonDarkHovered = loadButtonImage("dark_hover");

        setLayout(new BorderLayout());
        JPanel container1 = new JPanel(new BorderLayout());
        JPanel container2 = new JPanel(new BorderLayout());


        Config conf = Config.getInstance();
        Config.AppTheme theme = conf.getThemeE();

        JPanel bp = createButtonPanel();
        add(bp, BorderLayout.NORTH);
        add(container1, BorderLayout.CENTER);

        JPanel op = createOllamaPanel(conf);
        container1.add(op, BorderLayout.NORTH);
        container1.add(container2, BorderLayout.CENTER);

        JPanel ap = createAttemptsPanel(conf);
        container2.add(ap, BorderLayout.NORTH);

        for(Component c : new Component[]{themeButton,this,container1,container2,bp,op,ap})
            Utils.applyTheme(c);
    }


    private JTextField tfTimeout;


    private JPanel createAttemptsPanel(Config conf) {



        int confLlmTimeoutSecs = conf.getLlmTimeoutSecs();

        tfTimeout=new JTextField(4);



        tfTimeout.setText(confLlmTimeoutSecs+"");




        Border b = BorderFactory.createLineBorder(App.theme.getFrameBorder(),2);
        TitledBorder tb = new TitledBorder(b,"Timeout");
        JPanel outerContainer=new JPanel(new BorderLayout());

        JPanel p =new JPanel(new BorderLayout());
        JPanel container1 =new JPanel(new BorderLayout());


        p.setBorder(tb);
        JLabel lTimeout=new JLabel("seconds");


        p.add(tfTimeout,BorderLayout.WEST);
        p.add(container1,BorderLayout.CENTER);

        container1.add(lTimeout,BorderLayout.WEST);
        container1.add(new JLabel(),BorderLayout.CENTER);



        Utils.applyTheme(p);
        Utils.applyTheme(tfTimeout);
        Utils.applyTheme(lTimeout);
        Utils.applyTheme(container1);
        Utils.applyTheme(outerContainer);

        tb.setTitleColor(App.theme.getTextColor());
        tb.setTitleFont(tb.getTitleFont().deriveFont(24f));

        tfTimeout.setHorizontalAlignment(SwingConstants.CENTER);


        DocumentListener dl=new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                timeoutChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                timeoutChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                timeoutChanged();
            }
        };

        tfTimeout.getDocument().addDocumentListener(dl);

        outerContainer.setBorder(BorderFactory.createEmptyBorder(0,16,0,12));



            tfTimeout.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        lTimeout.setBorder(BorderFactory.createEmptyBorder(0,8,12,12));

        outerContainer.add(p,BorderLayout.WEST);


        return outerContainer;
    }

    private void timeoutChanged() {
        try {
            Config.getInstance().setLlmTimeoutSecs(Integer.parseInt(tfTimeout.getText()));
        }catch (Exception ex){
            Log.e("Failed to set new timeout value\n"+ex.getMessage());
        }
    }

    private JTextField ollamaHost;
    private JTextField ollamaPort;

    private JPanel createOllamaPanel(Config conf) {
        String confHost = conf.getOllamaHost();
        int confPort = conf.getOllamaPort();

        ollamaHost=new JTextField(23);
        ollamaHost.setText(confHost);
        ollamaPort=new JTextField(5);
        ollamaPort.setText(""+confPort);

        Border b = BorderFactory.createLineBorder(App.theme.getFrameBorder(),2);
        TitledBorder tb = new TitledBorder(b,"Ollama config");
        JPanel outerContainer=new JPanel(new BorderLayout());

        JPanel p =new JPanel(new BorderLayout());
        JPanel container1 =new JPanel(new BorderLayout());
        JPanel container2 =new JPanel(new BorderLayout());
        JPanel container3 =new JPanel(new BorderLayout());

        p.setBorder(tb);
        JLabel lHost=new JLabel("host :");
        JLabel lPort=new JLabel("port :");

        p.add(lHost,BorderLayout.WEST);
        p.add(container1,BorderLayout.CENTER);

        container1.add(ollamaHost,BorderLayout.WEST);
        container1.add(container2,BorderLayout.CENTER);

        container2.add(lPort,BorderLayout.WEST);
        container2.add(container3,BorderLayout.CENTER);

        container3.add(ollamaPort,BorderLayout.WEST);
        container3.add(new JLabel(),BorderLayout.CENTER);



        Utils.applyTheme(p);
        Utils.applyTheme(ollamaHost);
        Utils.applyTheme(ollamaPort);
        Utils.applyTheme(lHost);
        Utils.applyTheme(lPort);
        Utils.applyTheme(container1);
        Utils.applyTheme(container2);
        Utils.applyTheme(container3);
        Utils.applyTheme(outerContainer);

        tb.setTitleColor(App.theme.getTextColor());
        tb.setTitleFont(tb.getTitleFont().deriveFont(24f));

        ollamaHost.setHorizontalAlignment(SwingConstants.CENTER);
        ollamaPort.setHorizontalAlignment(SwingConstants.CENTER);


        DocumentListener dl=new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ollamaConfChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ollamaConfChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ollamaConfChanged();
            }
        };

        ollamaPort.getDocument().addDocumentListener(dl);
        ollamaHost.getDocument().addDocumentListener(dl);

        outerContainer.setBorder(BorderFactory.createEmptyBorder(0,16,0,12));

        for(JComponent c : new JComponent[]{lPort,ollamaHost,ollamaPort})
            c.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        lHost.setBorder(BorderFactory.createEmptyBorder(0,8,12,0));

        outerContainer.add(p,BorderLayout.WEST);


        return outerContainer;
    }

    private void ollamaConfChanged() {
        Config conf = Config.getInstance();
        conf.setOllamaHost(ollamaHost.getText());
        try{
            conf.setOllamaPort(Integer.parseInt(ollamaPort.getText()));
        }catch (Exception ex){
            Log.e("Invalid port");
        }
    }

    JButton themeButton = new JButton();

    private JPanel createButtonPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel("<html><b>Theme</b></html>");
        l.setFont(l.getFont().deriveFont(24f));
        l.setBorder(BorderFactory.createEmptyBorder(8,24,8,8));
        p.add(l, BorderLayout.WEST);
        JPanel p2 = new JPanel(new BorderLayout());
        p.add(p2, BorderLayout.CENTER);
        p2.add(themeButton, BorderLayout.WEST);
        updateThemeButton();
        themeButton.setRolloverEnabled(true);
        themeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Config.AppTheme theme = Config.getInstance().getThemeE();
                if (theme == Config.AppTheme.DARK) Config.getInstance().setTheme(Config.AppTheme.LIGHT);
                else Config.getInstance().setTheme(Config.AppTheme.DARK);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateThemeButton();
                        mainWindow.themeSwitched();
                    }
                });
            }
        });

        Utils.applyTheme(p2);
        themeButton.setFocusPainted(false);
        themeButton.setBorderPainted(false);
        themeButton.setContentAreaFilled(false);
        return p;
    }

    private void updateThemeButton() {
        Config.AppTheme theme = Config.getInstance().getThemeE();
        if(theme== Config.AppTheme.LIGHT){
            themeButton.setIcon(buttonDark);
            themeButton.setRolloverIcon(buttonDarkHovered);
       }else{

            themeButton.setIcon(buttonLight);
            themeButton.setRolloverIcon(buttonLightHovered);


        }
    }

    private ImageIcon loadButtonImage(String name) {
        try {
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/icon_64/" + name + ".png")));
        } catch (Exception ne) {
            Log.e("no " + name + " button image found");
            return null;
        }
    }

}
