package free.svoss.tools.jChatRAG.conversation;

import free.svoss.tools.jChatRAG.Log;
import free.svoss.tools.jChatRAG.ResponseBean;
import jollama.OllamaClient;
import jollama.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Conversation// implements OllamaClient.StreamJSONCallback
{

    public int hashCode() {
        return startDate.hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof Conversation && ((Conversation) o).startDate.equals(this.startDate);
    }

    private State state = State.WAITING_FOR_USER_PROMPT;

    public final String llm;
    public final Date startDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String title;

    private OllamaClient ollamaClient = null;

    private final LinkedList<String> userPrompts = new LinkedList<>();
    private final LinkedList<String> responses = new LinkedList<>();
    private final LinkedList<Integer[]> contexts = new LinkedList<>();

    public String[] getUserPrompts() {
        return userPrompts.toArray(new String[0]);
    }

    public String[] getResponses() {
        return responses.toArray(new String[0]);
    }


    public void handleStreamResponse(ResponseBean responseBean) {
        if (responseBean != null //&& state == State.GENERATING
        ) {

            if (responses.size() < userPrompts.size()) responses.add("");

            String lastResponse = responses.removeLast();
            lastResponse = lastResponse + responseBean.response;
            responses.add(lastResponse);

            if (responseBean.done && responseBean.context != null) {
                contexts.add(responseBean.context);
                state = State.WAITING_FOR_USER_PROMPT;
            }
        }
    }


    public Conversation(String llm) {
        this.llm = llm;
        this.startDate = new Date();
        this.title = null;
    }

    public State getState() {
        return state;
    }

    public void addUserPrompt(String userPrompt) {
        userPrompts.add(userPrompt);
        if (userPrompts.size() == 1) {
            if (userPrompt.length() <= 16) this.title = userPrompt;
            else {
                new Thread(() -> setTitle(
                        new OllamaClient().generateResponse(llm,
                                "Please come up with a title for a conversation that started like this:\n\n" + userPrompt+"\n\n" +
                                        "Don't give me suggestions, just give me ONE title. It should not be longer than 30 characters."
                                , null))).start();
            }
        }
    }

    public OllamaClient getOllamaClient(String llm) {
        if (!this.llm.equalsIgnoreCase(llm)) Log.e("LLMs don't match");

        if (ollamaClient == null)
            ollamaClient = new OllamaClient();

        return ollamaClient;

    }

    public JSONArray getLatestContext() {
        if (contexts.isEmpty()) return null;
        Integer[] context = contexts.getLast();
        if (context == null) {
            contexts.removeLast();
            return getLatestContext();
        } else return new JSONArray(context);
    }

    private final static SimpleDateFormat justDate = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat justTime = new SimpleDateFormat("HH:mm");

    public String getTimeString() {

        String justDateNow = justDate.format(new Date());
        String justDateCon = justDate.format(startDate);

        if (!justDateCon.equalsIgnoreCase(justDateNow)) return justDateCon;

        return justTime.format(startDate);
    }

    public enum State {
        WAITING_FOR_USER_PROMPT, GENERATING
    }
}
