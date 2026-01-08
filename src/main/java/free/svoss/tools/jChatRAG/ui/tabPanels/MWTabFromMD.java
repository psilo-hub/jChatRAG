package free.svoss.tools.jChatRAG.ui.tabPanels;

import free.svoss.tools.jChatRAG.Utils;

import javax.swing.*;

public class MWTabFromMD extends MWTab {
    public MWTabFromMD(String resourceName) {

        //
        // load md
        // convert to html
        // set label text

        String html = Utils.convertMDtoHtml(Utils.loadTextResourceFromJar("md/" + resourceName + ".md"));

        if (!html.startsWith("<html>")) html = "<html>" + html;

        add(new JLabel(html));
    }

    @Override
    public void disposeMWTab() {

    }
}
