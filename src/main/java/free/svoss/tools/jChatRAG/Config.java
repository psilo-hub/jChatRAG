package free.svoss.tools.jChatRAG;

import ca.fredperr.customtitlebar.titlebar.theme.DarkTBTheme;
import ca.fredperr.customtitlebar.titlebar.theme.LightTBTheme;
import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import com.google.gson.GsonBuilder;
import com.msiops.ground.crockford32.Crockford32;
import free.svoss.tools.jChatRAG.ui.ActivationFrame;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Config {
    private static Config instance = null;
    private String ollamaHost = "localhost";
    private int ollamaPort = 11434;
    private AppTheme theme = AppTheme.DARK;
    private int maxSubTopicListGenerationAttempts = 5;
    private int llmTimeoutSecs = 420;
    private int llmNrAttempts = 5;


    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = load();
            if (instance == null)
                instance = new Config();

            instance.checkUpdateTime();
            Runtime.getRuntime().addShutdownHook(new Thread(instance::save));
        }
        return instance;
    }

    private void checkUpdateTime() {
        // determine last update check time + sanity check
        if (luc == 0) {
            Version latest = UpdateChecker.getLatestVersion();
            if (latest != null && latest.date != null) luc = latest.date.getTime();
        }
        luc = Math.min(luc, new Date().getTime());
    }

    private static Config load() {
        File f = getConfigFile();
        if (!f.exists() || !f.isFile() || !f.canRead() || f.length() < 1) return null;

        byte[] data = null;

        try {
            data = Files.readAllBytes(f.toPath());
        } catch (IOException e) {
            Log.e("Error loading config file:\n" + e.getMessage());
        }

        if (data == null || data.length == 0) return null;
        else {

            String json = new String(data, StandardCharsets.UTF_8);

            try {
                return new GsonBuilder().create().fromJson(json, Config.class);
            } catch (Exception ex) {
                Log.e("Error parsing config file:\n" + ex.getMessage());
                return null;
            }
        }
    }

    private static File getConfigFile() {
        return new File(getPersistenceFolder() + File.separator + "config.json");
    }

    public static boolean portable() {
        if (ArgParser.getInstance(null).portable()) return true;
        File runningFrom = getRunningFrom();
        if (runningFrom == null || runningFrom.isDirectory()) return false;
        String n = runningFrom.getName().toLowerCase(Locale.ROOT).trim();
        return n.endsWith("-portable.jar");
    }

    private static File getSaltFile() {
        if (portable()) return new File(getPersistenceFolder() + File.separator + ".salt");
        else return new File(System.getProperty("user.home") + File.separator + ".jChatRAG_salt");
    }

    public byte[] getSalt() {
        File saltFile = getSaltFile();
        if (!saltFile.exists()) createSaltFile(saltFile);
        try {
            return Files.readAllBytes(saltFile.toPath());
        } catch (IOException e) {
            Log.e("Failed to get salt\n" + e.getMessage());
            return null;
        }
    }

    private void createSaltFile(File saltFile) {
        if ((saltFile != null && !saltFile.exists()) || (saltFile != null && saltFile.isFile() && saltFile.length() == 0)) {
            File parent = saltFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                Log.f("Failed to create folder: " + parent);
                System.exit(1);
            }
            if (!parent.isDirectory()) {
                Log.f("Invalid folder: " + parent);
                System.exit(1);
            }

            String rndContent = new Date().getTime() + "#";
            File userHome = new File(System.getProperty("user.home"));
            File[] sf = userHome.listFiles();
            rndContent = rndContent + (sf == null ? "#%#$" : sf.length);
            rndContent = rndContent + new Date().getTime();

            byte[] hash = Hashing.getSha256(rndContent.getBytes(StandardCharsets.UTF_8));

            try {
                Files.write(saltFile.toPath(), hash);
                Log.d("salt saved");
                Utils.hideFile(saltFile);
            } catch (IOException e) {
                Log.e("Failed to save salt\n" + e.getMessage());
            }
        }
    }

    public static File getPersistenceFolder() {
        File pf = new File(System.getProperty("user.home") + File.separator + ".jChatRAG");

        if (portable()) pf = new File(getRunningFromFolder() + File.separator + ".jChatRAG");

        if (!pf.exists() && !pf.mkdirs()) {
            Log.f("Failed to create folder " + pf);
            System.exit(1);
        }

        if (!pf.isDirectory()) {
            Log.f("Invalid folder " + pf);
            System.exit(1);
        }

        //Log.d("Persistence folder : "+ Ansi.DARK_GREEN_FG+pf+Ansi.RESET);
        return pf;
    }

    private static File getRunningFromFolder() {
        File rf = getRunningFrom();
        if (rf == null) {
            Log.f("Failed to determine jar folder");
            System.exit(1);
        }
        if (rf.isFile()) rf = rf.getParentFile();
        return rf;
    }

    public static File getRunningFrom() {

        ProtectionDomain pd = App.class.getProtectionDomain();
        CodeSource cs = pd == null ? null : pd.getCodeSource();
        URL location = cs == null ? null : cs.getLocation();
        return location == null ? null : new File(location.getFile());
    }

    public String getOllamaHost() {
        return ollamaHost;
    }

    public void setOllamaHost(String ollamaHost) {
        this.ollamaHost = ollamaHost;
    }

    public int getOllamaPort() {
        return ollamaPort;
    }

    public void setOllamaPort(int ollamaPort) {
        this.ollamaPort = ollamaPort;
    }

    private void save() {
        File configFile = getConfigFile();

        String json = new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(this, Config.class);

        //Log.d("Config to save:\n" + json);

        try {
            Files.write(configFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
            //Log.d("config saved");
        } catch (IOException e) {
            Log.e("Failed to save config!\n" + e.getMessage());
        }
    }

    public TBTheme getTheme() {
        if (this.theme == null) this.theme = AppTheme.DARK;
        if (this.theme == AppTheme.DARK)
            return new DarkTBTheme();
        else if (this.theme == AppTheme.LIGHT)
            return new LightTBTheme();

        Log.w("DUMMY - not implemented for theme " + this.theme.name());
        return new DarkTBTheme();
    }

    public AppTheme getThemeE() {
        if (this.theme == null) this.theme = AppTheme.DARK;
        return this.theme;
    }

    public void setTheme(AppTheme t) {
        if (t != null) this.theme = t;
    }

    public int getMaxSubTopicListGenerationAttempts() {
        return maxSubTopicListGenerationAttempts;
    }

    public void setMaxSubTopicListGenerationAttempts(int maxSubTopicListGenerationAttempts) {
        this.maxSubTopicListGenerationAttempts = maxSubTopicListGenerationAttempts;
    }

    private long luc = 0;// last update check
    private static final long ucInterval = 7L * 24L * 60L * 60L * 1000L; // millis in a week

    public int getLlmTimeoutSecs() {
        return llmTimeoutSecs;
    }

    public void setLlmTimeoutSecs(int secs) {
        llmTimeoutSecs = Math.max(15, Math.min(secs, 1200));
    }

    public int getLlmNrAttempts() {
        return llmNrAttempts;
    }

    public void setLlmNrAttempts(int attempts) {
        llmNrAttempts = Math.max(1, Math.min(attempts, 20));
    }

    public Date getNextUpdateCheckDate() {
        checkUpdateTime();
        return new Date(luc + ucInterval);
    }

    public static File getRagVersionsFile() {
        if (portable())
            return new File(getPersistenceFolder() + File.separator + ".jChatRAG_versions");
        else
            return new File(System.getProperty("user.home") + File.separator + ".jChatRAG_versions");
    }

    public void setLuc() {
        luc = new Date().getTime();
        checkUpdateTime();
    }

    private BigInteger license = null;

    public BigInteger getLicense() {
        return license;
    }

    public boolean isValidLicense(BigInteger storedLicense) {
        if (storedLicense == null) return false;


        // rsa decrypt stored license key (containing valid till (days since first release; 2 bytes), a few bytes from the salt)
        String salt = getCrockfordSalt();

        BigInteger decrypted = storedLicense.modPow(
                new BigInteger("13547015897714979165557135959"),
                new BigInteger("26272165929377230989024523873")
        );

        String decryptedString = new String(decrypted.toByteArray(), StandardCharsets.UTF_8);

        if (!decryptedString.endsWith("%" + salt)) return false;

        String dateString = "20" + decryptedString.substring(0, 6);

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyyMMdd").parse(dateString);
        } catch (ParseException e) {
            Log.d("Failed to parse date from " + dateString + "\n" + e.getMessage());
        }

        return date != null && date.getTime() >= new Date().getTime();

    }

    public String getCrockfordSalt() {
        byte[] salt = getSalt();
        if (salt == null) return null;
        String encoded = Crockford32.encode(new BigInteger(salt));

        if (encoded.length() > 5) {
            //Log.d("Full salt : " + encoded);
            encoded = encoded.substring(0, 5);
        }
        return encoded;
    }

    public void askForActivation(String msg) {

        ActivationFrame af = new ActivationFrame("ACTIVATION", msg);
        while (!af.isVisible()) Utils.waitMs(200);
        while (af.isVisible()) Utils.waitMs(200);

        String crockford = null;
        try {
            crockford = af.getActivationCode();
        } catch (Exception ignored) {
        }
        try {
            af.dispose();
        } catch (Exception ignored) {
        }
        if (crockford != null && isValidActivationCode(crockford))
            license = Crockford32.decode(crockford);

        else {
            Log.f("NO VALID LICENSE PROVIDED\n\nAsk Stefan for a license code");
            System.exit(0);
        }
    }

    public String getLicenseExpirationDate(BigInteger storedLicense) {

        if (storedLicense == null) return null;

        String salt = getCrockfordSalt();

        BigInteger decrypted = storedLicense.modPow(
                new BigInteger("13547015897714979165557135959"),
                new BigInteger("26272165929377230989024523873")
        );

        String decryptedString = new String(decrypted.toByteArray(), StandardCharsets.UTF_8);

        if (!decryptedString.endsWith("%" + salt)) return null;

        String dateString = "20" + decryptedString.substring(0, 6);

        Date date = null;

        try {
            date = new SimpleDateFormat("yyyyMMdd").parse(dateString);
        } catch (ParseException e) {
            Log.d("Failed to parse date from " + dateString + "\n" + e.getMessage());
        }

        return date == null ? null : new SimpleDateFormat("yyyy-MM-dd").format(date);

    }

    public boolean isValidActivationCode(String crockford) {
        BigInteger decoded = null;
        try {
            decoded = Crockford32.decode(crockford);
        } catch (Exception ignored) {
        }
        if (decoded == null) return false;
        return isValidLicense(decoded);
    }

    public enum AppTheme {
        DARK, LIGHT;
    }
}
