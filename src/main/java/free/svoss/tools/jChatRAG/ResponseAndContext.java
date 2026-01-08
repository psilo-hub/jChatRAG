package free.svoss.tools.jChatRAG;

import jollama.json.JSONArray;

public class ResponseAndContext {
    public final String response;
    public final JSONArray context;

    public ResponseAndContext(String response, JSONArray context) {
        this.response = response;
        this.context = context;
    }
}
