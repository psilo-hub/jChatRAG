package free.svoss.tools.jChatRAG.presentation;

import free.svoss.tools.jChatRAG.Log;
import free.svoss.tools.jChatRAG.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;

public class Presentation {
    public final Topic topic;
    private final LinkedList<SubTopic> subTopics = new LinkedList<>();

    public Presentation(String fullTopic) {
        topic = new Topic(fullTopic);
    }

    public void addSubTopic(@NotNull String subTopic, @NotNull String content) {
        SubTopic st = new SubTopic(subTopic);
        st.setContent(content);
        subTopics.add(st);
    }

    public File saveHtmlDoc(@NotNull File outputFolder) {


        String htmlPrefix = Utils.loadTextResourceFromJar("page/plain-prefix.html");
        String htmlSuffix = Utils.loadTextResourceFromJar("page/plain-suffix.html");
        String htmlContent = Utils.loadTextResourceFromJar("page/plain-content.html");

        htmlPrefix=addTitleToPrefix(htmlPrefix);

        LinkedList<String> contentHtml = new LinkedList<>();

        contentHtml.add(topic.getCoverHtml());
        contentHtml.add(getSubTopicOverviewHtml());

        for (SubTopic subTopic : subTopics) {
            contentHtml.add(subTopic.getCoverHtml());
            contentHtml.addAll(Arrays.asList(subTopic.slidesHtml));
        }



        LinkedList<String> wrappedContent = wrapSlideContent(htmlContent,contentHtml);


        //LinkedList<String> contentHtmlWithSlideNumbers = setSlideNumbers(wrappedContent);

        StringBuilder sb = new StringBuilder();
        sb.append(htmlPrefix);
        for (String slideContent : wrappedContent) sb.append(slideContent.trim()).append("\n");
        sb.append(htmlSuffix);

        //save page
        File htmlOutput = new File(outputFolder + File.separator + "plain.html");
        try {
            Files.write(htmlOutput.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            Log.d("Plain HTML saved");
        } catch (IOException e) {
            Log.f("Failed to save plain HTML to " + outputFolder);
            System.exit(1);
        }



        return htmlOutput;

    }

    public File saveHtmlSlideShow(@NotNull File outputFolder) {

        try {
            Files.write(new File(outputFolder + File.separator + "slides.css").toPath(), Utils.loadTextResourceFromJar("page/slides.css").getBytes(StandardCharsets.UTF_8));
            Log.d("Slideshow CSS saved");
        } catch (IOException e) {
            Log.f("Failed to save CSS to " + outputFolder);
            System.exit(1);
        }

        String htmlPrefix = Utils.loadTextResourceFromJar("page/slides-prefix.html");
        String htmlSuffix = Utils.loadTextResourceFromJar("page/slides-suffix.html");
        String htmlContent = Utils.loadTextResourceFromJar("page/slides-content.html");

        htmlPrefix=addTitleToPrefix(htmlPrefix);

        LinkedList<String> contentHtml = new LinkedList<>();

        contentHtml.add(topic.getCoverHtml());
        contentHtml.add(getSubTopicOverviewHtml());

        for (SubTopic subTopic : subTopics) {
            contentHtml.add(subTopic.getCoverHtml());
            contentHtml.addAll(Arrays.asList(subTopic.slidesHtml));
        }



        LinkedList<String> wrappedContent = wrapSlideContent(htmlContent,contentHtml);


        LinkedList<String> contentHtmlWithSlideNumbers = setSlideNumbers(wrappedContent);

        StringBuilder sb = new StringBuilder();
        sb.append(htmlPrefix);
        for (String slideContent : contentHtmlWithSlideNumbers) sb.append(slideContent.trim()).append("\n");
        sb.append(addEnoughDots(htmlSuffix,wrappedContent.size()));

        //save page
        File htmlOutput = new File(outputFolder + File.separator + "slides.html");
        try {
            Files.write(htmlOutput.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            Log.d("Slideshow HTML saved");
        } catch (IOException e) {
            Log.f("Failed to save HTML to " + outputFolder);
            System.exit(1);
        }

        //save bg image
        byte[] imgData = Utils.loadResourceFromJar("img/img1.jpg");
        File imgTarget = new File(outputFolder + File.separator + "img1.jpg");
        try {
            Files.write(imgTarget.toPath(), imgData);
            Log.d("Slideshow background image 1 saved");
        } catch (IOException e) {
            Log.e("Failed to save background image to " + outputFolder);
        }
        imgData = Utils.loadResourceFromJar("img/img2.jpg");
        imgTarget = new File(outputFolder + File.separator + "img2.jpg");
        try {
            Files.write(imgTarget.toPath(), imgData);
            Log.d("Slideshow background image 2 saved");
        } catch (IOException e) {
            Log.e("Failed to save background image to " + outputFolder);
        }



        return htmlOutput;
    }

    private String addEnoughDots(String htmlSuffix, int amount) {
        if(htmlSuffix==null) return null;
        final String prefix="\t<span class=\"dot\" onclick=\"currentSlide(";
        final String suffix=")\"></span>\n";

        if(
                htmlSuffix.contains("span")&&
                        htmlSuffix.contains("class")&&
                        htmlSuffix.contains("dot")&&
                        htmlSuffix.contains("onclick")&&
                        htmlSuffix.contains("currentSlide")
        ){
            String[] lines = htmlSuffix.split("\n");
            StringBuilder output = new StringBuilder();
            for(String line : lines)
                if(!line.contains("currentSlide(1)"))
                    output.append(line).append("\n");
                else{
                    StringBuilder sb = new StringBuilder();
                    for(int i=1;i<=amount;i++)
                        sb.append(prefix).append(i).append(suffix);

                    output.append(sb).append("\n");
                }

            return output.toString();

        }else return htmlSuffix;
    }

    private String addTitleToPrefix(String htmlPrefix) {
        if(htmlPrefix!=null&&htmlPrefix.contains("<title></title>"))
            htmlPrefix=htmlPrefix.replace("<title></title>","<title>"+topic.topic+"</title>");

        return htmlPrefix;
    }

    private String getSubTopicOverviewHtml() {

        StringBuilder out = new StringBuilder("\n<ul>\n");

        for (SubTopic st : subTopics) {
            out.append("<li><h3>").append(st.topic).append("</h3>");
            if (st.topicAddendum != null && !st.topicAddendum.isEmpty())
                out.append("<p>").append(st.topicAddendum).append("</p>");
            out.append("</li>\n");
        }
        out.append("</ul>\n");

        return out.toString();

    }

    private LinkedList<String> setSlideNumbers(LinkedList<String> contentHtml) {

        int index = 1;
        int total = contentHtml.size();
        LinkedList<String> numbered = new LinkedList<>();

        for(String slide : contentHtml){
            while (slide.contains("${numbertext}"))
                slide=slide.replace("${numbertext}",index+" / "+total);

            numbered.add(slide);
            index++;
        }

        return numbered;

    }

    private LinkedList<String> wrapSlideContent(String htmlContent, LinkedList<String> contents) {

        LinkedList<String> wrapped = new LinkedList<>();
        boolean changeImgNr=false;

        for(String content : contents) {
            wrapped.add(wrapSlideContent(htmlContent, content,changeImgNr));
            changeImgNr=!changeImgNr;
        }

        return wrapped;
    }

    private String wrapSlideContent(String htmlContent, String slideContent,boolean changeImgNr) {
        while (htmlContent.contains("${content}"))
            htmlContent=htmlContent.replace("${content}",slideContent);

        if(changeImgNr&&htmlContent.contains("img src=\"img1.jpg\""))
            htmlContent=htmlContent.replace("img src=\"img1.jpg\"","img src=\"img2.jpg\"");

        return htmlContent;
    }

    public void saveHtmlPieces(@NotNull File outputFolder) {

        File piecesFolder = new File(outputFolder + File.separator + "pieces");
        if (!piecesFolder.exists() && !piecesFolder.mkdirs()) {
            Log.f("Failed to create folder\n" + piecesFolder);
            System.exit(1);
        }

        for (int subTopicIndex = 0; subTopicIndex < subTopics.size(); subTopicIndex++) {

            SubTopic st = subTopics.get(subTopicIndex);

            for (int slideIndex = 0; slideIndex < st.slidesHtml.length; slideIndex++) {
                File f = new File(piecesFolder + File.separator + (subTopicIndex + 1) + "_" + (slideIndex + 1) + ".html");

                try {
                    Files.write(f.toPath(), st.slidesHtml[slideIndex].getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    Log.f("Failed to write to " + f);
                    System.exit(1);
                }
            }
        }

        Log.d("HTML pieces saved to " + piecesFolder);
    }

    public void saveMD(@NotNull File outputFolder) {

        File mdFolder = new File(outputFolder + File.separator + "md");
        if (!mdFolder.exists() && !mdFolder.mkdirs()) {
            Log.f("Failed to create folder\n" + mdFolder);
            System.exit(1);
        }

        String topicSummary = topic + "\n";

        for (int subTopicIndex = 0; subTopicIndex < subTopics.size(); subTopicIndex++) {
            File stFile = new File(mdFolder + File.separator + "st_" + (subTopicIndex + 1) + ".md");
            SubTopic st = subTopics.get(subTopicIndex);

            try {
                Files.write(stFile.toPath(), combineSlidesMD(st.slidesMD).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                Log.f("Failed to write MD to " + stFile);
                System.exit(1);
            }

            topicSummary = topicSummary + "\n\n" + (subTopicIndex + 1) + ". " + st;

        }


        File topicFile = new File(mdFolder + File.separator + "topic.txt");
        try {
            Files.write(topicFile.toPath(), topicSummary.trim().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.f("Failed to write to " + topicFile);
            System.exit(1);
        }


        Log.d("Markdown saved to " + mdFolder);

    }

    private String combineSlidesMD(String[] slidesMD) {
        StringBuilder sb = new StringBuilder();

        for (String slide : slidesMD)
            sb.append(slide).append("\n\n\n");

        return sb.toString().trim();
    }
}
