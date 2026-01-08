package free.svoss.tools.jChatRAG.ui.tabPanels;

import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Log;
import free.svoss.tools.jChatRAG.ResponseBean;
import free.svoss.tools.jChatRAG.Utils;
import free.svoss.tools.jChatRAG.conversation.Conversation;
import free.svoss.tools.jChatRAG.conversation.ConversationStorage;
import free.svoss.tools.jChatRAG.ui.ResponseChatItem;
import free.svoss.tools.jChatRAG.ui.SelectOptionFrame;
import free.svoss.tools.jChatRAG.ui.UserPromptChatItem;
import free.svoss.tools.jChatRAG.ui.VerticalLayout;
import jollama.OllamaClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;

public class ChatPanel extends MWTab implements OllamaClient.StreamJSONCallback {
    private JComboBox<String> llmCombo;

    @Override
    public void disposeMWTab() {
        final Conversation conv = this.conversation;
        if (conv != null) {
            new Thread(() -> {
                SelectOptionFrame optionFrame = new SelectOptionFrame("Save conversation?", "Do you want to save this conversation?", Color.cyan, "qm.png", new String[]{"yes", "no"});

                while (optionFrame.isVisible()) Utils.waitMs(200);

                String selected = optionFrame.getSelectedOption();

                if ("yes".equalsIgnoreCase(selected))
                    ConversationStorage.getInstance().storeConversation(conv);

                try {
                    optionFrame.dispose();
                } catch (Exception ignored) {
                }

            }).start();
        }
    }




    public ChatPanel() {
        this(null);
    }

    public ChatPanel(Conversation conv) {
        this.conversation = conv;

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        JPanel centralContainer = new JPanel(new BorderLayout());
        JPanel footer = new JPanel();

        JPanel ccInput = createInputPanel();
        JPanel ccChatHistory = createChatHistoryContainer();
        centralContainer.add(ccChatHistory, BorderLayout.CENTER);
        centralContainer.add(ccInput, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(centralContainer, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        //  model selection
        initComboBox();
        JLabel llmLabel = new JLabel("<html>&nbsp;&nbsp;<b>LLM :&nbsp;</b>");
        header.setLayout(new BorderLayout());
        header.add(llmLabel, BorderLayout.WEST);
        header.add(llmCombo, BorderLayout.CENTER);



        Log.i("CHAT PANEL CREATED");
        //  scrollable area for chat history
        //  textArea for prompt
        //  button to submit prompt
        //  buttons to copy prompt/response (markup/html/plain)
        //  dispose -> ask if chat should be saved (depending on config)
        // todo
        //  maybe generate prompt suggestions
        //  button to go back


        for (Component c : new Component[]{header, centralContainer, footer, llmCombo, llmLabel})
            Utils.applyTheme(c);

        llmCombo.addActionListener(e -> updateSubmitButtonState());


        if (conv != null) {
            llmCombo.setSelectedItem(conv.llm);
            llmCombo.setEnabled(false);
            updateChatHistoryPanel(false);
        }

        updateSubmitButtonState();
    }

    JPanel chatHistoryContainer;
    JScrollPane chatScrollPane;
    JPanel chatScrollPaneContainer;

    private JPanel createChatHistoryContainer() {
        chatHistoryContainer = new JPanel(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));
        //chatHistoryContainer.setSize(new Dimension(400,300));

        //FlowLayout flowLayout=new FlowLayout(FlowLayout.LEADING,400,20);
        //BoxLayout layout = new BoxLayout(chatHistoryContainer, BoxLayout.Y_AXIS);
        //chatHistoryContainer.setLayout(layout);
        // chatHistoryContainer.setLayout(flowLayout);

        chatScrollPane = new JScrollPane(chatHistoryContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //chatScrollPane = new JScrollPane(chatHistoryContainer);

        JPanel p = new JPanel(new GridLayout(1, 1));
        p.add(chatScrollPane);
        //p.add(chatHistoryContainer);


        Utils.applyTheme(p);
        Utils.applyTheme(chatHistoryContainer);
        Utils.applyTheme(chatScrollPane);

        //p.setMinimumSize(new Dimension(320, 180));
        //chatHistoryContainer.setMinimumSize(new Dimension(320, 180));
        //chatHistoryContainer.setPreferredSize(new Dimension(1600, 1000));
        //chatScrollPane.setMinimumSize(new Dimension(320, 180));


        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        chatScrollPaneContainer = p;
        return p;

    }

    JButton submitButton = new JButton("submit");
    JTextArea userPromptTextArea = new JTextArea(4, 30);


    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setMinimumSize(new Dimension(64, 64));
        JPanel buttonPanel = new JPanel();
        JPanel textPanel = new JPanel(new BorderLayout());

        buttonPanel.add(submitButton);

        inputPanel.add(buttonPanel, BorderLayout.EAST);
        inputPanel.add(textPanel, BorderLayout.CENTER);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));


        //In a container that uses a BorderLayout:

        JScrollPane scrollPane = new JScrollPane(userPromptTextArea);

        textPanel.add(scrollPane, BorderLayout.CENTER);
        userPromptTextArea.setBorder(BorderFactory.createLineBorder(App.theme.getControlButtonHoverBackground()));
        userPromptTextArea.setFont(userPromptTextArea.getFont().deriveFont(22f));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        userPromptTextArea.setWrapStyleWord(true);


        userPromptTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSubmitButtonState();
            }
        });

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Utils.applyTheme(buttonPanel);
                Utils.applyTheme(textPanel);
                Utils.applyTheme(submitButton);
                Utils.applyTheme(inputPanel);
                Utils.applyTheme(userPromptTextArea);

                submitButton.setBorderPainted(false);
                submitButton.setFocusPainted(false);

                try {
                    submitButton.setRolloverEnabled(true);
                    submitButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/img/icon_64/arrow_up_gray.png"))));
                    submitButton.setRolloverIcon(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/img/icon_64/arrow_up_hover.png"))));
                    submitButton.setPressedIcon(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/img/icon_64/arrow_up_green.png"))));
                    submitButton.setText("");
                    submitButton.setContentAreaFilled(false);
                } catch (Exception ignored) {
                }
            }
        });


        submitButton.addActionListener(e -> submitButtonClicked());

        return inputPanel;
    }

    private void updateSubmitButtonState() {
        String selectedLLM = (String) llmCombo.getSelectedItem();

        if (selectedLLM == null || selectedLLM.trim().isEmpty()) {
            Log.d("No LLM selected");
            submitButton.setEnabled(false);
        } else if (conversation != null && conversation.getState() != Conversation.State.WAITING_FOR_USER_PROMPT) {
            Log.d("conversation state : " + conversation.getState().name());
            submitButton.setEnabled(false);
        } else if (userPromptTextArea.getText().trim().isEmpty()) {
            Log.d("user prompt empty");
            submitButton.setEnabled(false);
        } else submitButton.setEnabled(true);

    }

    private Conversation conversation = null;

    private void submitButtonClicked() {
        String llm = (String) llmCombo.getSelectedItem();
        if (conversation == null) {
            if (llm == null || llm.trim().isEmpty()) {
                Log.f("No LLM selected!!! Submit button should have been disabled");
                System.exit(1);
            } else conversation = new Conversation(llm);
        }

        llmCombo.setEnabled(false);
        submitButton.setEnabled(false);
        String userPrompt = userPromptTextArea.getText();
        userPromptTextArea.setText("");
        userPromptTextArea.setEnabled(false);
        conversation.addUserPrompt(userPrompt);
        OllamaClient ollama = conversation.getOllamaClient(llm);

        ollama.streamGenerateResponseJSON(llm, userPrompt, conversation.getLatestContext(), this);

        updateChatHistoryPanel(false);


    }

    private void initComboBox() {
        LinkedList<String> llmList = new LinkedList<>();
        llmList.add(" ");
        if (App.modelNames != null)
            Collections.addAll(llmList, App.modelNames);

        if (llmList.size() < 3) llmList.removeFirst();

        String[] asArray = llmList.toArray(new String[0]);

        llmCombo = new JComboBox<>(asArray);

        if(asArray.length==1)llmCombo.setSelectedItem(asArray[0]);

        llmCombo.setEnabled(asArray.length > 1);
    }

    private final static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @Override
    public void nextJSON(String json) {


        ResponseBean responseBean = null;

        try {
            responseBean = gson.fromJson(json, ResponseBean.class);
        } catch (Exception ex) {
            Log.f("Failed to get response\n\n" + ex.getMessage());
        }

        if (responseBean == null) System.exit(1);
        else {
            conversation.handleStreamResponse(responseBean);
           SwingUtilities.invokeLater(() -> updateChatHistoryPanel(true));

        }
    }

    private void updateChatHistoryPanel(boolean justLastResponse) {

        Component[] subComponents = chatHistoryContainer.getComponents();
        Component lastComponent = (subComponents == null || subComponents.length == 0) ? null : subComponents[subComponents.length - 1];
        ResponseChatItem last = lastComponent == null ? null : (ResponseChatItem) lastComponent;

        String[] responses1 = last == null ? null : conversation.getResponses();
        String lastResponse = (responses1 == null || responses1.length == 0) ? "..." : responses1[responses1.length - 1];

        if (justLastResponse && last != null && !lastResponse.isEmpty() && !lastResponse.equalsIgnoreCase("...")) {
            last.updateText(lastResponse);
            last.invalidate();
            last.repaint();
        } else {
            //clear chatHistoryContainer
            if (subComponents != null)
                for (Component c : subComponents)
                    chatHistoryContainer.remove(c);

            String[] userPrompts = conversation.getUserPrompts();
            String[] responses = conversation.getResponses();

            Log.d("up " + userPrompts.length + " re " + responses.length);

            // fill chatHistoryContainer
            for (int index = 0; index < userPrompts.length; index++) {
                chatHistoryContainer.add(new UserPromptChatItem(userPrompts[index], chatScrollPaneContainer));
                chatHistoryContainer.add(new ResponseChatItem(index < responses.length ? responses[index] : "...", chatScrollPaneContainer));
            }


            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    JScrollBar vbar = chatScrollPane.getVerticalScrollBar();
                    vbar.setValue(vbar.getMaximum());


                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {


                            chatHistoryContainer.revalidate();

                            chatHistoryContainer.repaint();


                            chatScrollPane.revalidate();
                            chatScrollPane.repaint();


                        }
                    });


                }
            });


        }

    }

    @Override
    public void streamFinished() {
        userPromptTextArea.setEnabled(true);
    }
}
