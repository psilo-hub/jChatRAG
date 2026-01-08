package free.svoss.tools.jChatRAG.ui;

import javax.swing.*;
import java.awt.*;

public class UserPromptChatItem extends AbstractChatItem {
    public UserPromptChatItem(String userPrompt,Container parent) {
        super(userPrompt,parent);
        System.out.println(userPrompt+"\n\n");
        //add(new JLabel(userPrompt));
    }


}
