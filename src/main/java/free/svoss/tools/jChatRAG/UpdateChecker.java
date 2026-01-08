package free.svoss.tools.jChatRAG;

import free.svoss.tools.jChatRAG.ui.MsgFrame;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UpdateChecker implements Runnable {


    public static Version getLatestVersion() {
        String jarVersions = Utils.loadTextResourceFromJar("versions.txt");
        File versionsFile = Config.getRagVersionsFile();
        String hfVersions = Utils.loadTextFileIntoString(versionsFile);

        Set<Version> jarVersionSet = parseVersions(jarVersions);
        Set<Version> hfVersionSet = parseVersions(hfVersions);

        return getLatest(jarVersionSet, hfVersionSet);
    }

    private static Version getLatest(Set<Version> jarVersionSet, Set<Version> hfVersionSet) {
        Version l1 = getLatest(jarVersionSet);
        Version l2 = getLatest(hfVersionSet);

        if (l1 == null) return l2;
        else if (l2 == null) return l1;
        else {
            Set<Version> s = new HashSet<>();
            s.add(l1);
            s.add(l2);
            return getLatest(s);
        }
    }


    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static Version getLatest(Set<Version> versionSet) {
        if (versionSet == null) return null;
        versionSet.remove(null);
        if (versionSet.isEmpty()) return null;

        Version latest = null;
        Date latestDate = null;

        for (Version v : versionSet)
            if (v != null && v.date != null) {
                if(latest==null||latestDate==null||latestDate.getTime()<v.date.getTime()){
                    latest=v;
                    latestDate=v.date;
                }
            }

        return latest;
    }

    @Override
    public void run() {
        String jarVersions = Utils.loadTextResourceFromJar("versions.txt");
        File versionsFile = new File(System.getProperty("user.home") + File.separator + ".jChatRAG_versions");
        String hfVersions = Utils.loadTextFileIntoString(versionsFile);
        Set<Version> jarVersionSet = parseVersions(jarVersions);
        Set<Version> hfVersionSet = parseVersions(hfVersions);

        Version jarVersion = getLatest(jarVersionSet, null);
        Version latestVersion = getLatest(jarVersionSet, hfVersionSet);

        if (!latestVersion.equals(jarVersion)) showMessage(latestVersion);
        else {

            Date nextUpdateCheck = Config.getInstance().getNextUpdateCheckDate();
            if (nextUpdateCheck == null || nextUpdateCheck.getTime() < new Date().getTime())
                doUpdateCheck(versionsFile);
        }
    }

    private void showMessage(Version latestVersion) {


        String title="<html><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;NEW VERSION AVAILABLE</b>";
        String msg;
        if(latestVersion==null) msg="A newer version has been released.";
        else{
            msg = "Version "+latestVersion.version+" has been released.";
            if(latestVersion.releaseUrl!=null)msg=msg+"\n"+latestVersion.releaseUrl;
        }


        MsgFrame msgFrame = new MsgFrame(title, msg, Color.red, "notice.png");
        while (msgFrame.isVisible()) Utils.waitMs(200);

    }

    private void doUpdateCheck(File versionsFile) {
        new Thread(new RawGitHubFetcher()).start();
    }

    private static Set<Version> parseVersions(String versionsString) {
        Set<Version> out = new HashSet<>();
        if(versionsString==null)return out;
        versionsString=versionsString.trim();
        if(versionsString.isEmpty())return out;

        for(String line : versionsString.split("\n")){
            Version parsed = parseVersion(line);
            if(parsed!=null)out.add(parsed);
        }
        return out;
    }

    private static Version parseVersion(String line) {
        if(line==null)return null;
        line=line.trim();
        if(line.isEmpty()||line.startsWith("#"))return null;
        String[] token = line.split("\t");

        Date date = null;
        String version = null;
        String whatsNew = null;
        String hash = null;
        String url = null;

        try{
            date=sdf.parse(token[0]);
        }catch (Exception ex){
            Log.w("Failed to parse date from "+line);
        }
        if(date==null)return null;
        if(token.length>1)version=token[1];
        if(token.length>2)whatsNew=token[2];
        if(token.length>3)hash=token[3];
        if(token.length>4)url=token[4];


        return new Version(date,version,whatsNew,hash,url);
    }


}
