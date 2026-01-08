package free.svoss.tools.jChatRAG;

import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import free.svoss.tools.jChatRAG.presentation.Presentation;
import free.svoss.tools.jChatRAG.ui.CustomJMenu;
import free.svoss.tools.jChatRAG.ui.MsgFrame;
import free.svoss.tools.jChatRAG.ui.tabPanels.MWTab;
import jollama.OllamaClient;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Utils {
    private final static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    public static void waitMs(int millis) {
        if (millis > 0 && !SwingUtilities.isEventDispatchThread()) {
            long end = new Date().getTime() + millis;
            while (new Date().getTime() < end)
                try {
                    //noinspection BusyWait
                    Thread.sleep(Math.max(5, (end - new Date().getTime()) / 8));
                } catch (Exception ignored) {
                }
        }
    }

    public static String convertMDtoHtml(String markdownContent) {
        MutableDataSet options = new MutableDataSet();
        return HtmlRenderer.builder(options).build().render(Parser.builder(options).build().parse(markdownContent));
    }

    public static String getSubTopicListGenerationPrompt(String topic) {
        // try to load it from disk
        File f = new File(Config.getPersistenceFolder() + File.separator + "subTopicListGenPrompt.txt");
        String prompt = loadTextFromFile(f);
        if (prompt != null && prompt.contains("${topic}")) {
            while (prompt.contains("${topic}"))
                prompt = prompt.replace("${topic}", topic);

            return removeCommentLines(prompt);
        } else {
            prompt = loadTextResourceFromJar("subTopicListGenPrompt.txt");
            if (prompt == null) {
                Log.f("Failed to load prompt from jar");
                System.exit(1);
                return null;
            } else {
                saveTextToFile(prompt, f);

                while (prompt.contains("${topic}"))
                    prompt = prompt.replace("${topic}", topic);

                return removeCommentLines(prompt);
            }
        }
    }

    private static String loadTextFromFile(File f) {
        if (f == null || !f.exists() || !f.isFile() || !f.canRead()) return null;

        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Log.e("Failed to load " + f + "\n" + ex.getMessage());
            return null;
        }

    }

    private static String removeCommentLines(String prompt) {

        String[] lines = prompt.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (!line.startsWith("#"))
                sb.append(line).append("\n");
        }

        prompt = sb.toString().trim();

        while (prompt.contains("\n\n\n"))
            prompt = prompt.replace("\n\n\n", "\n\n").trim();

        return prompt;

    }

    public static String loadTextResourceFromJar(String name) {
        byte[] data = loadResourceFromJar(name);
        return data == null ? null : new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] loadResourceFromJar(String name) {

        try {
            InputStream is = getFileAsIOStream("./" + name);
            return readAllBytesFromIS(is);
        } catch (Exception e) {
            try {
                InputStream is = getFileAsIOStream(name);
                return readAllBytesFromIS(is);
            } catch (Exception ex) {
                Log.f("Failed to load " + name + " from jar");
                System.exit(1);
                return null;//            throw new RuntimeException(e);
            }
        }

    }

    private static byte[] readAllBytesFromIS(InputStream is) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1)
            buffer.write(data, 0, nRead);

        return buffer.toByteArray();

    }

    public static InputStream getFileAsIOStream(final String fileName) {
        InputStream ioStream = App.class
                //.getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) ioStream = App.class
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) ioStream = Utils.class
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) ioStream = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null)
            throw new IllegalArgumentException(fileName + " is not found");

        return ioStream;

    }


    private static void saveTextToFile(String text, File f) {

        if (f != null && text != null) {

            File parent = f.getParentFile();

            if (!parent.exists() && !parent.mkdirs()) {
                Log.f("Failed to create folder " + parent);
                System.exit(1);
            }

            if (!parent.isDirectory()) {
                Log.f("Invalid folder " + parent);
                System.exit(1);
            }

            if (f.exists() && !f.isFile()) {
                Log.f("Invalid file " + f);
                System.exit(1);
            }

            try {
                Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
                Log.d("Text saved to " + f);
            } catch (IOException e) {
                Log.f("Failed to save text to " + f + "\n" + e.getMessage());
                System.exit(1);
            }

        }

    }

    public static void copyTextToClipboard(String s) {

        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        if (clipboard != null)
            clipboard.setContents(stringSelection, null);

    }

    public static void showErrorMessageAndWait(String msg, Dimension dimension) {
        MsgFrame msgFrame = new MsgFrame("<html><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ERROR</b>", msg, Color.red, "error.png");
        while (msgFrame.isVisible()) waitMs(200);
    }

    public static String[] parseSTListResponse(String response) {

        if (response == null) return null;

        LinkedList<String> st = new LinkedList<>();

        for (String line : response.split("\n")) {
            line = line.trim();
            if (line.contains(":")) {
                while (line.contains("[")) line = line.replace("[", " ");
                while (line.contains("]")) line = line.replace("]", " ");
                line = line.trim();
                while (line.contains("  ")) line = line.replace("  ", " ").trim();

                st.add(line);
            }
        }

        return st.isEmpty() ? null : st.toArray(new String[0]);
    }

    public static String getSubTopicGenerationPrompt(String topic, String[] subTopics, int stIndex) {
        // try to load it from disk
        File f = new File(Config.getPersistenceFolder() + File.separator + "subTopicGenPrompt.txt");
        String prompt = loadTextFromFile(f);
        if (prompt != null &&
                prompt.contains("${topic}") &&
                prompt.contains("${subTopics}") &&
                prompt.contains("${currentSubTopic}")) {

            return editSubTopicGenPrompt(prompt, topic, subTopics, stIndex);
        } else {
            prompt = loadTextResourceFromJar("subTopicGenPrompt.txt");
            if (prompt == null) {
                Log.f("Failed to load prompt from jar");
                System.exit(1);
                return null;
            } else {
                saveTextToFile(prompt, f);
                return editSubTopicGenPrompt(prompt, topic, subTopics, stIndex);
            }
        }
    }

    private static String editSubTopicGenPrompt(String prompt, String topic, String[] subTopics, int stIndex) {
        String currentSubTopic = subTopics[stIndex];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subTopics.length; i++)
            sb.append((i + 1)).append(". ").append(subTopics[i]).append("\n\n");

        String sTopics = sb.toString().trim();


        while (prompt.contains("${topic}"))
            prompt = prompt.replace("${topic}", topic);

        while (prompt.contains("${subTopics}"))
            prompt = prompt.replace("${subTopics}", sTopics);

        while (prompt.contains("${currentSubTopic}"))
            prompt = prompt.replace("${currentSubTopic}", currentSubTopic);

        return removeCommentLines(prompt);
    }

    public static void generateOutput(String topic, String[] subTopics, String[] subTopicContent) {

        if (topic != null) topic = topic.trim();
        if (topic == null || topic.isEmpty()) {
            Log.f("No topic provided");
            System.exit(1);
        } else if (subTopics == null || subTopics.length == 0) {
            Log.f("No sub-topic list provided");
            System.exit(1);
        } else if (subTopicContent == null || subTopicContent.length == 0) {
            Log.f("No sub-topic content provided");
            System.exit(1);
        } else if (subTopics.length != subTopicContent.length) {
            Log.f("Sub-topic list doesn't match content");
            System.exit(1);
        } else {

            // create output folder
            File desktopFolder = FileSystemView.getFileSystemView().getHomeDirectory();

            File outputFolder = new File(desktopFolder + File.separator + new SimpleDateFormat("yyyy-MM-dd_hhmm").format(new Date()));
            if (!outputFolder.exists() && !outputFolder.mkdirs()) {
                Log.f("Failed to create folder " + outputFolder);
                System.exit(1);
            }
            if (!outputFolder.isDirectory()) {
                Log.f("Invalid folder " + outputFolder);
                System.exit(1);
            }

            Log.i("Generating output in " + outputFolder);

            generateOutput(topic, subTopics, subTopicContent, outputFolder);
        }
    }


    private static void generateOutput(@NotNull String topic, @NotNull String[] subTopics, @NotNull String[] subTopicContent, @NotNull File outputFolder) {

        Presentation p = new Presentation(topic);
        for (int index = 0; index < Math.min(subTopics.length, subTopicContent.length); index++)
            if (subTopics[index] != null && subTopicContent[index] != null)
                p.addSubTopic(subTopics[index], subTopicContent[index]);

        p.saveMD(outputFolder);
        p.saveHtmlPieces(outputFolder);
        File slideShow = p.saveHtmlSlideShow(outputFolder);

        //*//

        File htmlDoc = p.saveHtmlDoc(outputFolder);

        openInBrowser(htmlDoc);

        waitMs(2000);
        //*/

        openInBrowser(slideShow);


    }

    private static void openInBrowser(File htmlFile) {
        if (htmlFile.exists() && htmlFile.isFile() && htmlFile.canRead()) {
            Desktop desk = Desktop.getDesktop();
            if (desk == null) Log.e("Desktop not available");
            else {
                boolean opened = false;

                if (desk.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desk.browse(htmlFile.toURI());
                        opened = true;
                    } catch (IOException e) {
                        Log.w("BROWSE failed\n" + e.getMessage());
                    }
                }

                if (!opened && desk.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desk.open(htmlFile);
                        opened = true;
                    } catch (IOException e) {
                        Log.w("OPEN failed\n" + e.getMessage());
                    }
                }

                if (opened) Log.d("html file opened: " + htmlFile);
                else Log.e("Failed to open html file " + htmlFile);

            }
        }
    }

    public static void reapplyTheme(Component comp, TBTheme theme) {
        if (comp != null && theme != null) {
            comp.setForeground(App.theme.getTextColor());
            comp.setBackground(App.theme.getFrameBackground());


            if (comp instanceof MWTab)
                ((MWTab) comp).reapplyTheme(theme);

            if (comp instanceof Container) {
                Component[] components = ((Container) comp).getComponents();
                if (components != null)
                    for (Component c : components)
                        reapplyTheme(c, theme);

                if (comp instanceof JPanel) {
                    JPanel jp = (JPanel) comp;
                    Border b = jp.getBorder();
                    if (b instanceof TitledBorder) {
                        TitledBorder tb = (TitledBorder) b;
                        tb.setTitleColor(theme.getTextColor());
                        b = tb.getBorder();
                        if (b instanceof LineBorder) {
                            LineBorder lb = (LineBorder) b;
                            tb.setBorder(BorderFactory.createLineBorder(theme.getTextColor(), lb.getThickness()));
                        }
                    } else if (b instanceof LineBorder) {
                        LineBorder lb = (LineBorder) b;
                        ((JPanel) comp).setBorder(BorderFactory.createLineBorder(theme.getTextColor(), lb.getThickness()));
                    }
                }
            }

            if (comp instanceof CustomJMenu) {
                CustomJMenu m = (CustomJMenu) comp;
                applyTheme(m);
                Component[] menuItems = m.getMenuComponents();
                if (menuItems != null)
                    for (Component c : menuItems)
                        reapplyTheme(c, theme);

            }

            //if(comp instanceof CustomJMenuItem){
            //    Log.f("CustomJMenuItem");
            //    System.exit(1);
            //}


        }
    }


    /**
     * Checks if a connection to ollama is possible
     * returns -1 if connecting fails
     * returns the number of available models otherwise
     */
    static int checkOllamaConfig() {
        //----------------------------------------------------------------------------
        List<String> modNames = null;
        try {
            modNames = new OllamaClient().listModelNamesThrowing();
        } catch (Exception e) {
            Log.e("Failed to connect to ollama\n" + e.getMessage());
            return -1;
        }
        return modNames == null ? -1 : modNames.size();
    }


    public static void applyTheme(Component c) {
        c.setForeground(App.theme.getTextColor());
        c.setBackground(App.theme.getFrameBackground());
        c.setFont(c.getFont().deriveFont(24f));
    }

    public static String loadTextFileIntoString(File f) {
        if (!f.exists() || !f.isFile() || !f.canRead()) return null;
        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("Failed to read text file\n" + e.getMessage());
            return null;
        }
    }

    public static void hideFile(File f) {
        if (isWindows()) hideFileWindows(f);
        else Log.w("DUMMY - not implemented for your OS (" + OS + ")");
    }

    private static void hideFileWindows(File f) {
        if (f != null && (f.isFile() || f.isDirectory())) {

            Path path = f.toPath();
            DosFileAttributeView view = Files.getFileAttributeView(path, DosFileAttributeView.class);
            DosFileAttributes attributes = null;
            try {
                attributes = view.readAttributes();
            } catch (Exception ex) {
                Log.e("Failed to get file attributes\n" + ex.getMessage());
            }
            if (attributes != null && !attributes.isHidden()) {
                try {
                    view.setHidden(true);
                } catch (IOException e) {
                    Log.e("Failed to set file attributes\n" + e.getMessage());
                }
            }
        }
    }
}
