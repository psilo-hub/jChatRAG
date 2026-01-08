package free.svoss.tools.jChatRAG.presentation;

import free.svoss.tools.jChatRAG.Log;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTopic {
    public final String topic;
    public final String topicAddendum;
    public AbstractTopic(@NotNull String fullTopic){
        fullTopic=" "+fullTopic.trim()+" ";
        String prefix;
        String suffix;

        if(fullTopic.contains(":")){
            int index=fullTopic.indexOf(":");
            prefix = fullTopic.substring(0,index).trim();
            suffix=fullTopic.substring(index+1).trim();
        }else{
            prefix=fullTopic.trim();
            suffix=null;
        }

        if(prefix.isEmpty()){
            prefix=suffix;
            suffix=null;
        }

        if(prefix!=null)prefix=prefix.trim();
        if(suffix!=null)suffix=suffix.trim();

        topic=prefix;
        topicAddendum=(suffix==null||suffix.isEmpty())?null:suffix;

        if(topic==null||topic.isEmpty())throw new IllegalArgumentException();
    }

    public String toString(){
        return topic+":"+topicAddendum;
    }

    public String getCoverHtml() {

        String out = "<center><h1>"+topic+"</h1><br>";
        if(topicAddendum!=null&&!topicAddendum.isEmpty())
            out=out+"<h2>"+topicAddendum+"</h2><br>";
        out+="</center>";

        return "\n"+out+"\n";
    }
}
