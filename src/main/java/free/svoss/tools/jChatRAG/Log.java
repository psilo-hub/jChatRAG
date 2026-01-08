package free.svoss.tools.jChatRAG;

import free.svoss.tools.Ansi;

public class Log {
    private static Level globalLogLevel=Level.DEBUG;

    public static void setGlobalLogLevel(Level lvl){
        if(lvl!=null)globalLogLevel=lvl;
    }

    public enum Level{
        DEBUG(Ansi.LIGHT_GRAY_FG,1),
        INFO(Ansi.LIGHT_GREEN_FG,2),
        WARN(Ansi.LIGHT_YELLOW_FG,3),
        ERROR(Ansi.LIGHT_RED_FG,4),
        FATAL(Ansi.LIGHT_RED_BG+Ansi.WHITE_FG,5),
        OFF("",6);


        private final String ansiColor;
        private final int index;
        Level(String ansiColor,int i) {
            this.ansiColor=ansiColor;
            this.index=i;
        }
    }

    private static void log(Level lvl,String msg){
        if(msg!=null&&!msg.endsWith("\n")&&Level.DEBUG==globalLogLevel)msg=msg.trim()+"\n ";

        if(lvl!=null&&lvl.index>= globalLogLevel.index) {
            if (msg == null) msg = "";
            String[] lines = msg.split("\n");
            String lvlPrefix = "[ " + lvl.ansiColor + Ansi.BOLD + lvl.name() + (lvl.name().length() == 4 ? " " : "") + Ansi.RESET + " ] ";
            String emptyPrefix = "          ";
            for (int i = 0; i < lines.length; i++)
                System.out.println((i == 0 ? lvlPrefix : emptyPrefix) + lines[i].trim() + (i == (lines.length - 1) ? ((msg.endsWith("\n ")?"":" ")+"@ " + new RuntimeException().getStackTrace()[2] + "\n") : ""));

        }
    }

    public static void e(String msg){
        log(Level.ERROR,msg);
    }
    public static void f(String msg){
        log(Level.FATAL,msg);
    }
    public static void w(String msg){
        log(Level.WARN,msg);
    }
    public static void i(String msg){ log(Level.INFO,msg); }
    public static void d(String msg){
        log(Level.DEBUG,msg);
    }

}
