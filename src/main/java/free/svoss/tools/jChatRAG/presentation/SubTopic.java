package free.svoss.tools.jChatRAG.presentation;

import free.svoss.tools.jChatRAG.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SubTopic extends AbstractTopic {

    String[] slidesHtml;
    String[] slidesMD;

    public SubTopic(@NotNull String fullTopic) {
        super(fullTopic);
    }

    public void setContent(@NotNull String content) {
        // preprocess sub-topic content
        //  - clean
        //  - separate slides
        //  - convert to html

        slidesMD = separateSlides(cleanContent(content));
        slidesHtml = convertToHtml(slidesMD);
    }

    private String[] convertToHtml(String[] slidesContentMD) {
        String[] html = new String[slidesContentMD.length];
        for (int index = 0; index < slidesContentMD.length; index++)
            html[index] = Utils.convertMDtoHtml(slidesContentMD[index]);

        return html;
    }


    private String[] separateSlides(String cleanedContent) {
        String[] split = cleanedContent.split("\n----");
        String[] output = new String[split.length];
        for (int index = 0; index < split.length; index++) {
            String s = split[index].trim();
            while (s.startsWith("-")) s = s.substring(1).trim();
            output[index] = s;
        }
        return output;
    }

    private String cleanContent(@NotNull String content) {
        content = content.trim();
        while (content.startsWith("`")) content = content.substring(1).trim();
        while (content.endsWith("`")) content = content.substring(0, content.length() - 1).trim();
        if (content.startsWith("markdown")) content = content.substring(8).trim();
        while (content.contains("-----")) content = content.replace("-----", "----");
        while (content.contains("</details>")) content = content.replace("</details>", " ").trim();
        while (content.contains("  ")) content = content.replace("  ", " ");
        while (content.contains("\n\n\n")) content = content.replace("\n\n\n", "\n\n");

        //remove slide number
        content = content.trim();
        if (content.toLowerCase(Locale.ROOT).startsWith("slide")) {

            //remove the word 'slide'
            content = content.substring(5).trim();

            //remove leading digits
            while (!content.isEmpty() && Character.isDigit(content.charAt(0)))
                content = content.substring(1).trim();

            //remove leading :
            while (content.startsWith(":"))
                content = content.substring(1).trim();

            // make it a level 1 headline
            if (!content.startsWith("#"))
                content = "# " + content;
        }

        //Log.f("DUMMY\n"+content);
        //System.exit(1);
        //Log.d("Cleaned content:\n" + content);
        return content;
    }
}
