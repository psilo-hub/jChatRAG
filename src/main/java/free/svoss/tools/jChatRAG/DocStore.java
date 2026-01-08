package free.svoss.tools.jChatRAG;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DocStore {
    private static DocStore instance=null;
    private final File docFolder;
    private DocStore(@NotNull File docFolder){
        this.docFolder=docFolder;
        if(!docFolder.exists()&&!docFolder.mkdirs()){
            Log.f("Failed to create folder : "+docFolder);
            System.exit(1);
        }
        if(!docFolder.isDirectory()){
            Log.f("Invalid document folder : "+docFolder);
            System.exit(1);
        }
        Log.d("document folder : "+docFolder);
    }

    public static DocStore getInstance(){
        if(instance==null){
            instance=load();
            if(instance==null)instance=new DocStore(new File(Config.getPersistenceFolder()+File.separator+".docStore"));

            Runtime.getRuntime().addShutdownHook(new Thread(instance::save));
        }
        return instance;
    }

    private static DocStore load(){
        Log.w("DUMMY");
        //todo
        return null;
    }

    private void save(){
        Log.w("DUMMY");
        //todo
    }
}
