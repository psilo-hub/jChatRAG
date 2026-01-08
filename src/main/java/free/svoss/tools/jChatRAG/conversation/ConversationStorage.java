package free.svoss.tools.jChatRAG.conversation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import free.svoss.tools.jChatRAG.App;
import free.svoss.tools.jChatRAG.Config;
import free.svoss.tools.jChatRAG.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class ConversationStorage {
    Set<Conversation> conversationSet = new HashSet<>();
    private static ConversationStorage instance = null;

    private ConversationStorage() {
    }

    public static ConversationStorage getInstance() {
        if (instance == null) {
            instance = load();
            if (instance == null) instance = new ConversationStorage();
            Runtime.getRuntime().addShutdownHook(new Thread(instance::save));
        }

        return instance;
    }

    private static File getPersistenceFile() {
        return new File(Config.getPersistenceFolder() + File.separator + ".conversations.json");
    }


    private static ConversationStorage load() {
        File f = getPersistenceFile();
        if (!f.exists() || !f.isFile() || !f.canRead() || f.length() < 1) return null;

        byte[] data = null;

        try {
            data = Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            Log.e("Error loading previous conversations:\n" + e.getMessage());
        }

        if (data == null || data.length == 0) return null;
        else {

            String json = new String(data,StandardCharsets.UTF_8);

            try{
                return new GsonBuilder().create().fromJson(json,ConversationStorage.class);
            }catch (Exception ex){
                Log.e("Error parsing previous conversations:\n" + ex.getMessage());
                return null;
            }
        }
    }

    private void save() {
        if (!conversationSet.isEmpty()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            String json = gson.toJson(this, ConversationStorage.class);
            File pf = getPersistenceFile();
            try {
                Files.write(pf.toPath(), json.getBytes(StandardCharsets.UTF_8));
                Log.d("Conversations saved to " + pf);
            } catch (IOException e) {
                Log.e("Failed to write to " + pf + "\n" + e.getMessage());
            }
        }
    }


    public Conversation[] getSortedConversations() {
        Conversation[] convs = conversationSet.toArray(new Conversation[0]);
        Arrays.sort(convs, Comparator.comparing(o -> o.startDate));
        return convs;
    }

    public void storeConversation(Conversation conv) {
        conversationSet.add(conv);
        if (App.mainWindow != null)
            App.mainWindow.updateConversationList();

    }
}
