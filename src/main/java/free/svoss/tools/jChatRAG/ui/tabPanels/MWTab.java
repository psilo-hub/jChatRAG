package free.svoss.tools.jChatRAG.ui.tabPanels;

import ca.fredperr.customtitlebar.titlebar.theme.DarkTBTheme;
import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import free.svoss.tools.jChatRAG.App;

import javax.swing.*;

public abstract class MWTab extends JPanel {
    public abstract void disposeMWTab();
    public MWTab(){
        setForeground(App.theme.getTextColor());
        setBackground(App.theme.getFrameBackground());
        SwingUtilities.invokeLater(this::revalidate);
    }

    public void reapplyTheme(TBTheme theme){
        setForeground(theme.getTextColor());
        setBackground(theme.getFrameBackground());
    }
}
