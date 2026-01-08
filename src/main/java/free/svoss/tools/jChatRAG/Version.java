package free.svoss.tools.jChatRAG;

import java.util.Date;

public class Version {
    //date	version	what's new	jar hash	release url
    public final Date date;
    public final String version;
    public final String whatsNew;
    public final String jarHash;
    public final String releaseUrl;

    public Version(Date date, String version, String whatsNew, String jarHash, String releaseUrl) {
        this.date = date;
        this.version = version;
        this.whatsNew = whatsNew;
        this.jarHash = jarHash;
        this.releaseUrl = releaseUrl;
    }

    public int hashCode() {
        return ("" + version).hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Version)) return false;
        Version other = (Version) o;
        return date.equals(other.date) && version.equals(other.version);
    }
}
