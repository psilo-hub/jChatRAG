package free.svoss.tools.jChatRAG;

public class ArgParser {
    private static ArgParser instance=null;
    private final String[] args;
    private ArgParser(String[] args){
        this.args=args;
    }
    public static ArgParser getInstance(String[] args){
        if(instance==null)
            instance=new ArgParser(args);
        return instance;
    }

    public boolean portable(){
        if(args == null)return false;
        for(String arg : args)if(arg!=null&&arg.trim().equalsIgnoreCase("--portable"))
            return true;

        return false;
    }

    public boolean activation(){
        if(args == null)return false;
        for(String arg : args)if(arg!=null&&(arg.trim().equalsIgnoreCase("--activation")||arg.trim().equalsIgnoreCase("--activate")))
            return true;

        return false;
    }


}
