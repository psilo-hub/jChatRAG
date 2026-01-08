package free.svoss.tools.jChatRAG.ui;

import ca.fredperr.customtitlebar.titlebar.TBJFrame;
import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import ca.fredperr.customtitlebar.titlebar.win.WindowFrameType;
import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Config;
import free.svoss.tools.jChatRAG.Log;
import free.svoss.tools.jChatRAG.Utils;
import free.svoss.tools.jChatRAG.conversation.Conversation;
import free.svoss.tools.jChatRAG.conversation.ConversationStorage;
import free.svoss.tools.jChatRAG.ui.tabPanels.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

public class MainWindow extends TBJFrame {
    JLabel title;
    //private final CustomJMenuItem loadChat;
    private final CustomJMenu loadChat;
    private final CustomJMenu[] menu;
    private CustomJMenu menuChat;
    public MainWindow() {
        super("jChatRAG", WindowFrameType.NORMAL, App.theme, 32);

        InputStream fis = getClass().getResourceAsStream("/img/logo/logo_32.png");
        try {
            setTitleBarIcon(ImageIO.read(fis));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 480));
        setLocationRelativeTo(null);

        // Setting UI attributes

        Font fontTitle = new Font ("SansSerif", Font.BOLD, 18);

        // Adding some components
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder());
        menuChat = new CustomJMenu("Chat","chat",this);
        CustomJMenu menuTools = new CustomJMenu("Tools","tools",this);
        CustomJMenu menuSettings = new CustomJMenu("Settings","set",this);
        CustomJMenu menuInfo = new CustomJMenu("Info","info",this);


        // ------------------ Chat
        CustomJMenuItem newChat=new CustomJMenuItem("New Chat","new_chat",this);
        menuChat.add(newChat);
         //loadChat=new CustomJMenuItem("Load previous chat","load_chat",this);
         loadChat=new CustomJMenu("Load previous chat","load_chat",this);
        //loadChat.setForeground(App.theme.getTextColor());
        //loadChat.setBackground(App.theme.getFrameBackground());

        updatePreviousChatOptions();
        menuChat.add(loadChat);



        //menu=new CustomJMenu[]{menuChat,menuSlideGen,menuDocVault,menuSettings,menuInfo};
        menu=new CustomJMenu[]{menuChat,menuTools,menuSettings,menuInfo};

        // ------------------ Tools

        CustomJMenuItem menuSlideGen = new CustomJMenuItem("SlideGen","pres",this);
        CustomJMenuItem menuDocVault = new CustomJMenuItem("DocVault","doc",this);
        CustomJMenuItem menuPlagiarism = new CustomJMenuItem("Plagiarism checker","plagiarism",this);

        menuTools.add(menuSlideGen);
        menuTools.add(menuDocVault);
        menuTools.add(menuPlagiarism);

        //      ------------------ SlideGen
        //      ------------------ DocVault
        // ------------------ Settings
        // ------------------ Info
        CustomJMenuItem about = new CustomJMenuItem("About","empty",this);
        menuInfo.add(about);
        CustomJMenuItem issues = new CustomJMenuItem("Known Issues","empty",this);
        menuInfo.add(issues);



        newChat.addActionListener(e -> {
            newChat.setSelected(false);
            newChat.mouseInside=false;
            newChat.updateColors();
            newChat.invalidate();
            openChat(null);
        });
        about.addActionListener(e -> {
            about.setSelected(false);
            about.mouseInside=false;
            about.updateColors();
            about.invalidate();
            openAbout();
        });
        issues.addActionListener(e -> {
            issues.setSelected(false);
            issues.mouseInside=false;
            issues.updateColors();
            issues.invalidate();
            openIssues();
        });
        menuSlideGen.addActionListener(e -> {
            menuSlideGen.setSelected(false);
            menuSlideGen.mouseInside=false;
            menuSlideGen.updateColors();
            menuSlideGen.invalidate();
            openSlideGen();
        });
        menuDocVault.addActionListener(e -> {
            menuDocVault.setSelected(false);
            menuDocVault.mouseInside=false;
            menuDocVault.updateColors();
            menuDocVault.invalidate();
            openDocVault();
        });
        menuPlagiarism.addActionListener(e -> {
            menuPlagiarism.setSelected(false);
            menuPlagiarism.mouseInside=false;
            menuPlagiarism.updateColors();
            menuPlagiarism.invalidate();
            openPlagiarismChecker();
        });


        menuSettings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                menuSettings.setSelected(false);
                menuSettings.mouseInside=false;
                menuSettings.updateColors();
                menuSettings.invalidate();
                SwingUtilities.invokeLater(MainWindow.this::openSettings);
            }
        });


        menuBar.add(menuChat);
        menuBar.add(menuTools);
        menuBar.add(menuSettings);
        menuBar.add(menuInfo);


         title = new JLabel(getTitle());
        title.setFont(fontTitle);
        //title.setForeground(Color.GRAY);
        title.setForeground(Color.CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));

        getCustomAreaPanel().add(title);
        getCustomAreaPanel().add(menuBar);

        pack();
        setVisible(true);





        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(currentTab!=null)
                currentTab.disposeMWTab();
        }));

        addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                super.windowStateChanged(e);
                if(resizeOnStateChange){
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            resizeOnStateChange=false;
                            if(getState()==JFrame.MAXIMIZED_BOTH||getExtendedState()==JFrame.MAXIMIZED_BOTH){
                             Dimension dim = getSize();
                             setSize(dim.width-1,dim.height-1);
                             setState(JFrame.MAXIMIZED_BOTH);
                            }
                            resizeOnStateChange=true;
                        }
                    });
                }
            }
        });


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e("sleep interrupted");
                }
                reapplyTheme(App.theme);
            }
        });
    }

    private boolean resizeOnStateChange=true;

    private MWTab currentTab=null;
    private SlideGenPanel slideGenPanel=null;
    private MWTabFromMD docVaultPanel=null;
    private SettingsPanel settingsPanel=null;

    private void emptyContentPane(){
        Container cp =getContentPane();
        for(Component c : cp.getComponents())
            if(c instanceof MWTab) {
                cp.remove(c);
                ((MWTab) c).disposeMWTab();
            }

        cp.invalidate();
        cp.repaint();
    }

    private void openSettings(){
        //empty contentPane
        emptyContentPane();

        if(settingsPanel==null){settingsPanel=new SettingsPanel(this);Utils.applyTheme(settingsPanel);}

        currentTab=settingsPanel;
        Container cp=getContentPane();
        cp.add(settingsPanel);
        SwingUtilities.invokeLater(() -> {
            cp.invalidate();
            settingsPanel.invalidate();
            SwingUtilities.invokeLater(cp::repaint);
            reapplyTheme(App.theme);
        });
    }

    MWTabFromMD plagiarismCheckerPanel=null;

    private void openPlagiarismChecker(){
        //empty contentPane
        emptyContentPane();

        if(plagiarismCheckerPanel==null)plagiarismCheckerPanel=new MWTabFromMD("plagiarism");

        currentTab=plagiarismCheckerPanel;
        getContentPane().add(currentTab);
    }

    private void openDocVault(){
        //empty contentPane
        emptyContentPane();

        if(docVaultPanel==null)docVaultPanel=new MWTabFromMD("docVault");

        currentTab=docVaultPanel;
        getContentPane().add(currentTab);
    }

    private void openSlideGen(){
        //empty contentPane
        emptyContentPane();

        if(slideGenPanel==null)slideGenPanel=new SlideGenPanel();

        currentTab=slideGenPanel;
        getContentPane().add(currentTab);


        final Dimension dim =getSize();
        new Thread(() -> {

            Utils.waitMs(150);

            Dimension rsDim = new Dimension(dim.width+1,dim.height+1);
            SwingUtilities.invokeLater(() -> {
                try {
                    setSize(rsDim);
                }catch (Exception ignored){}
            });
        }).start();

    }


    private void openAbout() {
        //empty contentPane
        emptyContentPane();

        currentTab=new MWTabFromMD("about");
        getContentPane().add(currentTab);
    }
    private void openIssues() {
        //empty contentPane
        emptyContentPane();

        currentTab=new MWTabFromMD("issues");
        getContentPane().add(currentTab);
    }

    private void updatePreviousChatOptions() {
        ConversationStorage conversationStorage=ConversationStorage.getInstance();
        Conversation[] conversations=conversationStorage.getSortedConversations();
        if(conversations==null||conversations.length==0)loadChat.setEnabled(false);
        else{
            // clean up loadChat content
            for(Component c : loadChat.getComponents())
                loadChat.remove(c);


            for(Conversation c : conversations)if(c!=null){
                CustomJMenuItem i = new CustomJMenuItem((c.getTimeString()+" "+c.getTitle()).trim(),"empty",this);

                i.addActionListener(e -> openChat(c));

                loadChat.add(i);
            }

        }
    }

    private void openChat(Conversation c) {
        //empty contentPane
        emptyContentPane();

        currentTab=c==null?new ChatPanel():new ChatPanel(c);
        getContentPane().add(currentTab);
    }


    public void updateMenuColors(){
        for(CustomJMenu cm : menu)
            cm.updateColors();

    }

    public void updateConversationList() {
        updatePreviousChatOptions();
    }

    public void themeSwitched() {
        reapplyTheme(Config.getInstance().getTheme());
    }

    private void reapplyTheme(TBTheme theme) {
        if (theme != null) {
            App.theme = theme;
            Utils.reapplyTheme(this, theme);

            SwingUtilities.invokeLater(() -> title.setForeground(Color.CYAN));

            Utils.applyTheme(loadChat);
            loadChat.setFont(loadChat.getFont().deriveFont(16f));

        }
    }
}
