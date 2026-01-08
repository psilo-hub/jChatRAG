package free.svoss.tools.jChatRAG.ui.tabPanels;

import free.svoss.tools.jChatRAG.*;
import free.svoss.tools.jChatRAG.ui.ModernScrollPane;
import jollama.OllamaClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class SlideGenPanel extends MWTab {
    private JComboBox<String> llmCombo;

    @Override
    public void disposeMWTab() {
        // Log.w("DUMMY");
    }

    private void initComboBox() {
        LinkedList<String> llmList = new LinkedList<>();
        llmList.add(" ");
        if (App.modelNames != null)
            Collections.addAll(llmList, App.modelNames);

        if (llmList.size() < 3) llmList.removeFirst();

        String[] asArray = llmList.toArray(new String[0]);

        llmCombo = new JComboBox<>(asArray);

        if (asArray.length == 1) llmCombo.setSelectedItem(asArray[0]);

        llmCombo.setEnabled(asArray.length > 1);
    }

    public SlideGenPanel() {
        setLayout(new BorderLayout());

        JPanel header = createHeaderPanel();
        JPanel center = createCenterPanel();
        JPanel footer = createFooterPanel();

        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        for (Component c : new Component[]{header, center, footer, llmCombo, llmLabel})
            Utils.applyTheme(c);


        updateStatus(null);

    }

    private boolean generating = false;

    private void updateStatus(String s) {
        statusLabel.setText("");

        if (llmCombo.getSelectedItem().toString().trim().isEmpty()) {
            statusLabel.setText("First choose a language model!");
            startStopButton.setEnabled(false);
        } else if (topicTextArea.getText().trim().isEmpty()) {
            statusLabel.setText("Choose a topic!");
            startStopButton.setEnabled(false);
        } else if (!generating) {
            if (subTopicMode == SubTopicMode.INTERACTIVE) {
                statusLabel.setText("⬅ Click to generate sub-topic suggestions");
                startStopButton.setEnabled(true);
            } else if (subTopicMode == SubTopicMode.AUTOMATIC) {
                statusLabel.setText("⬅ Click to generate slides. Sub-topics will be chosen automatically!");
                startStopButton.setEnabled(true);
            } else if (subTopicMode == SubTopicMode.PROVIDED) {
                statusLabel.setText("⬅ Click to generate slides. The provided sub-topics will be used!");
                startStopButton.setEnabled(true);
            }
        }

        if (statusLabel.getText().trim().isEmpty() && s != null) statusLabel.setText(s);
    }

    private JButton startStopButton = new JButton();
    private JLabel statusLabel = new JLabel();

    private Image start;
    private Image startHovered;
    private Image stop;
    private Image stopHovered;

    private void loadStartStopButtonImages() {

        InputStream fisStart = getClass().getResourceAsStream("/img/icon_64/start.png");
        try {
            start = ImageIO.read(fisStart);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream fisStartHovered = getClass().getResourceAsStream("/img/icon_64/start_hovered.png");
        try {
            startHovered = ImageIO.read(fisStartHovered);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        InputStream fisStop = getClass().getResourceAsStream("/img/icon_64/stop.png");
        try {
            stop = ImageIO.read(fisStop);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream fisStopHovered = getClass().getResourceAsStream("/img/icon_64/stop_hovered.png");
        try {
            stopHovered = ImageIO.read(fisStopHovered);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private JPanel createFooterPanel() {


        JPanel p = new JPanel();


        Utils.applyTheme(p);
        Utils.applyTheme(statusLabel);
        Utils.applyTheme(startStopButton);


        BorderLayout layout = new BorderLayout();
        layout.setHgap(4);
        p.setLayout(layout);


        loadStartStopButtonImages();

        startStopButton.setIcon(new ImageIcon(start));
        startStopButton.setRolloverEnabled(true);
        startStopButton.setRolloverIcon(new ImageIcon(startHovered));

        //startStopButton.setEnabled(false);

        startStopButton.setBorderPainted(false);
        startStopButton.setFocusPainted(false);


        p.add(startStopButton, BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.CENTER);


        //statusLabel.setText("DUMMY SLIDE_GEN FOOTER PANEL");
        statusLabel.setText(" ");


        statusLabel.setFont(statusLabel.getFont().deriveFont(32f));


        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startStopClicked();
            }
        });

        return p;
    }

    private void startStopClicked() {
        if (generating) {
            if (generationRunner != null) generationRunner.requestStop();
            generationRunner = null;
            generating = false;

            activateComponents();

            updateStatus("interrupted");
            updateSubTopicButtons();
        } else {
            if (generationRunner != null) generationRunner.requestStop();

            String llm = (String) llmCombo.getSelectedItem();
            String topic = topicTextArea.getText();
            String[] subTopics = parseSubtopics(subTopicTextArea.getText());

            generationRunner = new GenerationRunner(topic, llm, subTopics, subTopicMode);

            deactivateComponents();

            new Thread(generationRunner).start();
        }
    }

    private String[] parseSubtopics(String text) {

        String[] lines = subTopicTextArea.getText().split("\n");
        LinkedList<String> cleanedLines = new LinkedList<>();
        for (String line : lines) {
            String cleanedLine = cleanSubTopicLine(line);
            if (cleanedLine != null && !cleanedLine.isEmpty())
                cleanedLines.add(cleanedLine);
        }

        return cleanedLines.toArray(new String[0]);

    }

    private String cleanSubTopicLine(String line) {
        if (line == null) return null;
        line = line.trim();
        while (!line.isEmpty() && Character.isDigit(line.charAt(0))) line = line.substring(1).trim();
        while (line.startsWith(".") || line.startsWith(":") || line.startsWith("#") || line.startsWith("*"))
            line = line.substring(1).trim();
        while (line.contains("*")) line = line.replace("*", " ").trim();
        while (line.contains("#")) line = line.replace("#", " ").trim();
        while (line.contains("  ")) line = line.replace("  ", " ").trim();
        return line.trim();
    }

    private void activateComponents() {
        activateOrDeactivate(true);
    }

    private void deactivateComponents() {
        activateOrDeactivate(false);
    }

    private void activateOrDeactivate(boolean activate) {
        for (Component c : new Component[]{subTopicModeButtonAutomatic, subTopicModeButtonInteractive, subTopicModeButtonDisabled, llmCombo, topicTextArea, subTopicTextArea})
            c.setEnabled(activate);

        if(activate){
            startStopButton.setIcon(new ImageIcon(start));
            startStopButton.setRolloverIcon(new ImageIcon(startHovered));
        }else {
            startStopButton.setIcon(new ImageIcon(stop));
            startStopButton.setRolloverIcon(new ImageIcon(stopHovered));
        }
    }

    private GenerationRunner generationRunner = null;

    private class GenerationRunner implements Runnable {
        private boolean stopRequested = false;
        private final String topi;
        private String[] subTopis;
        private final SubTopicMode stm;
        private final String lm;
        private OllamaClient oc;
        private String[] subTopicContent = null;
        public GenerationRunner(String topic, String llm, String[] subTopics, SubTopicMode subTopicMode) {
            topi = topic;
            subTopis = subTopics;
            stm = subTopicMode;
            lm = llm;
        }

        public void requestStop() {
            stopRequested = true;
        }

        @Override
        public void run() {

            oc = new OllamaClient();
            boolean needSubTopics = subTopis == null || subTopis.length == 0;

            if (needSubTopics && stm == SubTopicMode.INTERACTIVE) {
                generating = true;
                deactivateComponents();
                generateSubTopicList();
                generating = false;
                activateComponents();
                updateSubTopicButtons();
                updateStatus(null);
            } else if (needSubTopics && stm == SubTopicMode.AUTOMATIC) {
                generating = true;
                deactivateComponents();
                generateSubTopicList();
                updateSubTopicButtons();
                updateStatus(null);
                deactivateComponents();
                generateSTContent();
                generateOutput();
                generating = false;
                activateComponents();
                updateSubTopicButtons();
                updateStatus(null);
            } else if (!needSubTopics) {
                deactivateComponents();
                generating = true;
                generateSTContent();
                generateOutput();
                generating = false;
                activateComponents();
                updateSubTopicButtons();
                updateStatus(null);
            }

        }

        private void generateOutput() {

            System.out.println("Topic:\n\n"+topi+"\n\n");
            System.out.println("Subtopics:\n--------------\n");
            for(String st : subTopis)
                System.out.println(st+"\n-----------\n");

            if(subTopicContent!=null) {
                System.out.println("Subtopics content:\n--------------\n");

                for (String stc : subTopicContent)
                    System.out.println(stc+"\n-----------\n");


            }

            Utils.generateOutput(topi,subTopis,subTopicContent);
        }

        private void generateSTContent() {
            generating=true;
            LinkedList<String> st = new LinkedList<>();
            for(String line : subTopicTextArea.getText().split("\n"))
                if(!line.trim().isEmpty())st.add(line.trim());

            String[] subTopics = st.toArray(new String[0]);
            subTopicContent = new String[subTopics.length];

            String lm = (String)llmCombo.getSelectedItem();


            for(int stIndex =0;!stopRequested&&generating&&stIndex<subTopics.length;stIndex++){
                String prompt = Utils.getSubTopicGenerationPrompt(topicTextArea.getText(),subTopics,stIndex);
                String stShort = subTopics[stIndex];
                if(stShort.length()>20)stShort=stShort.substring(0,19);

                updateStatus("Generating content for sub-topic "+(stIndex+1)+"/"+subTopics.length+" "+ subTopics[stIndex]);
                //updateStatus("Generating content for sub-topic "+(stIndex+1)+"/"+subTopics.length+" "+stShort);

                OllamaClient oc = new OllamaClient();

                ResponseAndContext rnc = oc.generateResponseAndContext(lm,prompt,null);

                subTopicContent[stIndex]= rnc.response;

            }


        }

        private void generateSubTopicList() {

            String prompt = Utils.getSubTopicListGenerationPrompt(topi);

            int attempt = 1;
            int maxAttempts = Config.getInstance().getMaxSubTopicListGenerationAttempts();
            String[] subTopics = null;

            while (subTopics == null && attempt <= maxAttempts&&generating) {

                updateStatus("Creating sub-topic list (attempt #" + attempt + " of " + maxAttempts + ")");
                ResponseAndContext rnc = oc.generateResponseAndContext(lm, prompt, null);
                String response = rnc.response;
                subTopics = Utils.parseSTListResponse(response);

                if(subTopics!=null&&subTopics.length==0)subTopics=null;

                if(subTopics!=null)
                    for(String subT : subTopics)
                        Log.d(subT);

                attempt++;
            }

            if (subTopics == null)
                updateStatus("Failed to create sub-topic list");
            else {

                StringBuilder sb = new StringBuilder();
                for(String subT : subTopics)
                    sb.append(subT).append("\n");

                subTopicTextArea.setText(sb.toString().trim());

                this.subTopis=subTopics;
            }
        }
    }

    private final static JLabel llmLabel = new JLabel("<html><b>LLM :&nbsp;</b>");

    private JPanel createHeaderPanel() {
        JPanel p = new JPanel(new BorderLayout());

        initComboBox();

        p.add(llmLabel, BorderLayout.WEST);
        p.add(llmCombo, BorderLayout.CENTER);

        llmCombo.addActionListener(e -> {
            updateSubTopicButtons();
            updateStatus(null);
        });

        return p;
    }

    private JPanel createCenterPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(createTopicPanel());
        p.add(createSubTopicPanel());
        return p;
    }

    private JPanel createSubTopicPanel() {
        Border lineBorder = BorderFactory.createLineBorder(Color.lightGray);
        TitledBorder titledBorder = new TitledBorder(lineBorder,
                "Sub-Topics :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP
        );
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(22f));
        titledBorder.setTitleColor(App.theme.getTextColor());

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(titledBorder);
        p.setBackground(App.theme.getFrameBackground());
        p.add(createSubTopicButtonPanel(), BorderLayout.NORTH);

        JPanel textContainer = new JPanel();


        ModernScrollPane scrollPane = new ModernScrollPane(subTopicTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(App.theme.getFrameBackground(), 1, false));

        textContainer.add(scrollPane);
        Utils.applyTheme(textContainer);
        p.add(textContainer, BorderLayout.CENTER);


        subTopicTextArea.setBackground(App.theme.getFrameBackground());
        subTopicTextArea.setForeground(App.theme.getTextColor());
        subTopicTextArea.setFont(subTopicTextArea.getFont().deriveFont(22f));

        subTopicTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                subTopicTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                subTopicTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                subTopicTextChanged();
            }
        });

        p.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Dimension dim = p.getSize();
                        Dimension spDim = new Dimension(dim.width - 40, dim.height - 85);
                        scrollPane.setMaximumSize(spDim);
                        scrollPane.setPreferredSize(spDim);
                        scrollPane.setSize(spDim);

                    }
                });
            }
        });
        Dimension dim = p.getSize();
        Dimension spDim = new Dimension(dim.width - 40, dim.height - 85);
        scrollPane.setMaximumSize(spDim);
        scrollPane.setPreferredSize(spDim);
        scrollPane.setSize(spDim);


        return p;
    }

    private void subTopicTextChanged() {
        boolean isEmpty = subTopicTextArea.getText().trim().isEmpty();

        if (isEmpty && subTopicMode == SubTopicMode.PROVIDED) {
            setSubtopicMode("automatic");
            updateSubTopicButtons();
        } else if (!isEmpty) {
            setSubtopicMode("disabled");
            updateSubTopicButtons();
        }
    }

    JButton subTopicModeButtonAutomatic = new JButton("automatic");
    JButton subTopicModeButtonInteractive = new JButton("interactive");
    JButton subTopicModeButtonDisabled = new JButton("provided");


    private void updateSubTopicButtons() {
        boolean gotSubTopics = !subTopicTextArea.getText().trim().isEmpty();
        Color bgSelected = new Color(20, 80, 20);
        if (gotSubTopics) {
            subTopicMode = SubTopicMode.PROVIDED;

            subTopicModeButtonDisabled.setEnabled(false);
            subTopicModeButtonAutomatic.setEnabled(true);
            subTopicModeButtonInteractive.setEnabled(true);

            subTopicModeButtonDisabled.setBackground(bgSelected);
            subTopicModeButtonAutomatic.setBackground(App.theme.getFrameBackground());
            subTopicModeButtonInteractive.setBackground(App.theme.getFrameBackground());


            subTopicModeButtonAutomatic.setForeground(App.theme.getTextColor());
            subTopicModeButtonDisabled.setForeground(Color.BLACK);
            subTopicModeButtonInteractive.setForeground(App.theme.getTextColor());


        } else {

            if (subTopicMode == SubTopicMode.AUTOMATIC) {

                subTopicModeButtonDisabled.setEnabled(false);
                subTopicModeButtonAutomatic.setEnabled(false);
                subTopicModeButtonInteractive.setEnabled(true);

                subTopicModeButtonDisabled.setBackground(App.theme.getFrameBackground());
                subTopicModeButtonAutomatic.setBackground(bgSelected);
                subTopicModeButtonInteractive.setBackground(App.theme.getFrameBackground());


                subTopicModeButtonAutomatic.setForeground(Color.BLACK);
                subTopicModeButtonDisabled.setForeground(App.theme.getTextColor());
                subTopicModeButtonInteractive.setForeground(App.theme.getTextColor());


            } else {//interactive

                subTopicModeButtonDisabled.setEnabled(false);
                subTopicModeButtonAutomatic.setEnabled(true);
                subTopicModeButtonInteractive.setEnabled(false);

                subTopicModeButtonDisabled.setBackground(App.theme.getFrameBackground());
                subTopicModeButtonAutomatic.setBackground(App.theme.getFrameBackground());
                subTopicModeButtonInteractive.setBackground(bgSelected);


                subTopicModeButtonAutomatic.setForeground(App.theme.getTextColor());
                subTopicModeButtonDisabled.setForeground(App.theme.getTextColor());
                subTopicModeButtonInteractive.setForeground(Color.BLACK);
            }
        }
    }


    private JPanel createSubTopicButtonPanel() {
        GridLayout layout = new GridLayout(1, 3);
        layout.setHgap(8);
        JPanel p = new JPanel(layout);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        JButton[] buttons = new JButton[]{
                subTopicModeButtonAutomatic, subTopicModeButtonInteractive, subTopicModeButtonDisabled
        };
        for (JButton b : buttons) {
            p.add(b);
            b.addActionListener(e -> {
                setSubtopicMode(b.getText());
                updateSubTopicButtons();
                updateStatus(null);
            });
            b.setBackground(App.theme.getFrameBackground());
            b.setFocusPainted(false);
            b.setContentAreaFilled(true);
            b.setForeground(App.theme.getTextColor());
        }
        Utils.applyTheme(p);
        updateSubTopicButtons();
        return p;

    }

    private void setSubtopicMode(String text) {
        switch (text) {
            case "automatic":
                subTopicMode = SubTopicMode.AUTOMATIC;
                break;
            case "interactive":
                subTopicMode = SubTopicMode.INTERACTIVE;
                break;
            default:
                subTopicMode = SubTopicMode.PROVIDED;
        }
    }

    private SubTopicMode subTopicMode = SubTopicMode.AUTOMATIC;

    private enum SubTopicMode {
        AUTOMATIC, INTERACTIVE, PROVIDED;
    }

    JTextArea topicTextArea = new JTextArea(1, 38);
    JTextArea subTopicTextArea = new JTextArea(2, 38);

    private JPanel createTopicPanel() {
        Border lineBorder = BorderFactory.createLineBorder(Color.lightGray);
        TitledBorder titledBorder = new TitledBorder(lineBorder,
                "Topic :", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP
        );
        titledBorder.setTitleFont(titledBorder.getTitleFont().deriveFont(22f));
        titledBorder.setTitleColor(App.theme.getTextColor());

        JPanel p = new JPanel();
        p.setBorder(titledBorder);
        p.setBackground(App.theme.getFrameBackground());

        ModernScrollPane scrollPane = new ModernScrollPane(topicTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(App.theme.getFrameBackground(), 1, false));

        p.add(scrollPane);

        topicTextArea.setBackground(App.theme.getFrameBackground());
        topicTextArea.setForeground(App.theme.getTextColor());
        topicTextArea.setFont(topicTextArea.getFont().deriveFont(22f));


        //*//
        p.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Dimension dim = p.getSize();
                        Dimension spDim = new Dimension(dim.width - 40, dim.height - 50);
                        scrollPane.setMaximumSize(spDim);
                        scrollPane.setPreferredSize(spDim);
                        scrollPane.setSize(spDim);

                    }
                });
            }
        });
        Dimension dim = p.getSize();
        Dimension spDim = new Dimension(dim.width - 40, dim.height - 50);
        scrollPane.setMaximumSize(spDim);
        scrollPane.setPreferredSize(spDim);
        scrollPane.setSize(spDim);
        //*/

        topicTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                topicTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                topicTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                topicTextChanged();
            }
        });

        return p;
    }

    private void topicTextChanged() {
        updateSubTopicButtons();
        updateStatus(null);
    }
}
