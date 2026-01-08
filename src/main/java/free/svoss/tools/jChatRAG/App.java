package free.svoss.tools.jChatRAG;

import ca.fredperr.customtitlebar.titlebar.theme.TBTheme;
import com.msiops.ground.crockford32.Crockford32;
import free.svoss.tools.jChatRAG.conversation.ConversationStorage;
import free.svoss.tools.jChatRAG.ui.MainWindow;
import free.svoss.tools.jChatRAG.ui.MsgFrame;
import free.svoss.tools.jChatRAG.ui.SelectOptionFrame;
import free.svoss.tools.jChatRAG.ui.Splash;
import jollama.OllamaClient;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import javax.swing.*;
import java.awt.*;
import java.io.Console;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * Hello world!
 */
public class App {
    public static Splash splash = null;
    public static String[] modelNames = null;
    public static TBTheme theme;
    public final static int logoSize = 48;
    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    public static MainWindow mainWindow = null;

    // for the slideshow output
    // https://www.w3schools.com/howto/howto_js_slideshow.asp


    public static void listUiKeys() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keysEnumeration = defaults.keys();
        ArrayList<Object> keysList = Collections.list(keysEnumeration);

        LinkedList<String> keys = new LinkedList<>();
        for (Object o : keysList)
            if (o instanceof String)
                keys.add((String) o);
        String[] asArray = keys.toArray(new String[0]);
        Arrays.sort(asArray);

        for (String key : asArray) {
            Object value = defaults.get(key);
            if (value != null && value.getClass().getName().toLowerCase(Locale.ROOT).contains("color") && value.toString().contains("r=238,g=238,b=238"))
                System.out.println(key + "\t" + (value == null ? "null" : (value.getClass().getName() + "\t" + value)));
        }
    }

    public static void testMsgFrame() {
        MsgFrame mframe = new MsgFrame("Test", "Yes or yes?", Color.yellow, "qm.png");
        mframe.setVisible(true);
        while (mframe.isVisible()) Utils.waitMs(100);

        mframe.dispose();
        System.exit(0);

    }

    public static void testOptFrame() {
        SelectOptionFrame frame = new SelectOptionFrame("Test", "Yes or yes?", Color.yellow, "qm.png", new String[]{"Yes", "yes"});
        frame.setVisible(true);
        while (frame.isVisible()) Utils.waitMs(100);
        System.out.println(frame.getSelectedOption());
        frame.dispose();
        System.exit(0);

    }


    public static void main(String[] args) {
        System.out.println("Hello World!");


        printJarHash();


        AnsiConsole.systemInstall();
        ArgParser argParser = ArgParser.getInstance(args);
        if (argParser.activation()) generateActivationCode();

        //listUiKeys(); //System.exit(0);
        //testOutputGeneration();System.exit(0);


        //*//

        //*/

        /*//
        Log.d("Test");
        Log.i("Test");
        Log.w("Test");
        Log.e("Test");
        Log.f("Test");
        //*/

        //showErrorMessageAndWait("Test",new Dimension(240,160));
        //showErrorMessageAndWait("Failed to get model list from ollama.\n\nMake sure ollama is running and at least one LLM is installed.",new Dimension(320,0));

        // show splash
        splash = new Splash("loading ...");

        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));


        // load config
        Config config = Config.getInstance();
        theme = config.getTheme();


        /*//
        // testing the activation frame
        splash.dispose();
        ActivationFrame af = new ActivationFrame("title","msg");
        while (!af.isVisible())Utils.waitMs(200);
        while (af.isVisible())Utils.waitMs(200);
        System.exit(0);
        //*/

        //System.out.println("Salt : "+config.getCrockfordSalt()); System.exit(0);

        //Utils.checkOllamaConfig();


        updateCheck();
        checkLicense(splash);
        checkIntegrity();


        //todo
        // later - check locale
        // later - load translations


        // load list of saved conversations
        splash.setMessage("Loading list of saved conversations ...");
        ConversationStorage convStore = ConversationStorage.getInstance();
        //todo create titles for old conversations

        // load list of known documents
        //splash.setMessage("Loading list of known documents ...");
        //DocStore docStore = DocStore.getInstance();


        // check if ollama is running and get model list
        splash.setMessage("Checking ollama models ...");
        updateModelList();
        if (modelNames==null||modelNames.length == 0) {
            disposeSplash();
            Utils.showErrorMessageAndWait("Failed to get model list from Ollama.\n\nMake sure Ollama is running and at least one LLM is installed.", null);
            // todo open main window to show the settings panel instead
            System.exit(1);
        } else {
            System.out.println("Found " + modelNames.length + " models");
            Utils.waitMs(200);
            splash.setMessage("almost done ...");
            Utils.waitMs(400);

            // hide splash
            disposeSplash();


            // show main window
            mainWindow = new MainWindow();
            while (mainWindow.isVisible())
                Utils.waitMs(200);

            try {
                mainWindow.dispose();
                mainWindow = null;
            } catch (Exception ignored) {
            }

            System.exit(0);

        }
    }

    private static void generateActivationCode() {


        System.out.println(Ansi.ansi().eraseScreen().fgBrightGreen().a("--- ").fgBrightBlue().a("Activation code generator").fgBrightGreen().a(" ---\n\n").reset());
        System.out.println(Ansi.ansi().fgBrightMagenta().a("Enter password :").reset());


        Console cnsl = System.console();

        // Read password
        String pw;
        if (cnsl != null)
            pw = new String(cnsl.readPassword("> "));
        else
            pw = new Scanner(System.in).nextLine();

        byte[] pwHash = Hashing.getSha256(pw.getBytes(StandardCharsets.UTF_16));
        String hexHash = Hashing.bytesToHex(pwHash);

        if (!hexHash.contains("4BA1F65ABB7A2AE8C58F4568F087")) {
            System.out.println(Ansi.ansi().fgBrightRed().a("\nINVALID PASSWORD").reset());
            System.exit(1);
        } else {

            System.out.println(Ansi.ansi().fgBrightMagenta().a("Enter code :").reset());
            String salt;
            if (cnsl != null)
                salt = new String(cnsl.readLine("> "));
            else
                salt = new Scanner(System.in).nextLine();


            BigInteger eXor = new BigInteger("179955781");

            byte[] eXorBytes = eXor.toByteArray();

            byte[] eBytesBack = new byte[eXorBytes.length];
            for (int i = 0; i < eXorBytes.length; i++)
                eBytesBack[i] = (byte) (eXorBytes[i] ^ pwHash[i]);
            BigInteger eBack = new BigInteger(eBytesBack);


            BigInteger N = new BigInteger("26272165929377230989024523873");

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

            System.out.println(Ansi.ansi().fgBrightMagenta().a("\nEnter expiration date ").reset());
            System.out.println(Ansi.ansi().fgBrightMagenta().a("Use the following format:").reset());
            System.out.println(Ansi.ansi().fgCyan().a(sdf1.format(new Date()) + "\n").reset());
            String dateString;
            if (cnsl != null)
                dateString = new String(cnsl.readLine("> "));
            else
                dateString = new Scanner(System.in).nextLine();

            Date date = null;

            try {
                date = sdf1.parse(dateString.trim());
            } catch (Exception ignored) {
            }

            if (date == null) {
                Log.f("Invalid date format");
                System.exit(1);
            } else {
                String dateShort = new SimpleDateFormat("yyyyMMdd").format(date);
                dateShort = dateShort.substring(2);
                String activationUnencrypted = dateShort + "%" + salt;
                BigInteger activationBI = new BigInteger(activationUnencrypted.getBytes(StandardCharsets.UTF_8));
                BigInteger activationEnc = activationBI.modPow(eBack, N);

                String activationCrock = Crockford32.encode(activationEnc);

                System.out.println(activationCrock + "\n\n");

                try {
                    new ASCIIArtGenerator().printTextArt(activationCrock, ASCIIArtGenerator.ART_SIZE_SMALL, ASCIIArtGenerator.ASCIIArtFont.ART_FONT_DIALOG, "█");
                    System.out.println("\n");
                } catch (Exception ignored) {
                }

                System.exit(0);
            }
        }
    }

    private static String getJarHash() {
        File jarFile = Config.getRunningFrom();
        if (jarFile != null && jarFile.isFile() && jarFile.canRead() && jarFile.getName().endsWith(".jar"))
            return Hashing.bytesToHex(Hashing.getSha256(jarFile));

        return null;
    }


    private static void printJarHash() {
        String hash = getJarHash();
        if (hash != null) Log.d("jar hash : " + hash);
    }


    private static void checkIntegrity() {
        Log.w("DUMMY");//todo checkIntegrity - check if the jar hash matches the online hash for this version

    }

    private static void updateCheck() {
        new Thread(new UpdateChecker()).start();
    }

    private static void checkLicense(Splash splash) {

        Config config = Config.getInstance();
        BigInteger storedLicense = config.getLicense();
        if (storedLicense == null || !config.isValidLicense(storedLicense)) {
            String msg;
            if (storedLicense == null) msg = "Please enter an activation code!";
            else msg = "Your license has expired or is invalid.\nPlease enter a new activation code!";

            msg = msg + "\n\n<center>" + config.getCrockfordSalt() + "</center>";
            splash.setVisible(false);
            config.askForActivation(msg);
        } else Log.i("The app is activated till " + config.getLicenseExpirationDate(storedLicense));
    }

    private static void testOutputGeneration() {
        String topic = "Festivals and Holidays";
        String[] subTopics = new String[]{
                "Culture and Traditions : Explore the origins and significance of various festivals and holidays around the world.",
                "Food and Cuisine : Examine how different cultures prepare traditional dishes during their celebrations.",
                "Multimedia Celebrations : Discuss the role of music, dance, art, and other forms of media in festival and holiday traditions.",
                "Festival Economics : Analyze the economic impact of popular festivals on local communities and tourism.",
                "Environmental Impact : Investigate the environmental effects of hosting large-scale festivals and holidays.",
                "International Traditions : Highlight unique festivals celebrated internationally and their global significance."
        };
        String[] subTopicContent = new String[]{
                "```markdown\n" +
                        "--------------------------\n" +
                        "# Slide 1: Introduction\n" +
                        "- **Title:** Culture and Traditions: Exploring the Origins and Significance of Festivals and Holidays Around the World\n" +
                        "- **Objective:** Understand the diverse origins, meanings, and roles that festivals play in various cultures.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 2: Overview of Cultural Diversity in Festivals\n" +
                        "- **Diverse Celebrations:** Festivals are a reflection of cultural identities.\n" +
                        "- **Regional Differences:** Eastern vs. Western traditions vary significantly.\n" +
                        "- **Global Spread:** Many traditional festivals have gained international recognition and significance.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 3: Origins of Major World Religions' Festivals\n" +
                        "- **Christianity:** Christmas (Dec 25), Easter (varies)\n" +
                        "- **Islam:** Eid al-Fitr, Eid al-Adha\n" +
                        "- **Hinduism:** Diwali (Festival of Lights), Holi (Festival of Colors)\n" +
                        "- **Judaism:** Hanukkah, Passover\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 4: Indigenous and Traditional Festivals\n" +
                        "- **Native American:** Thanksgiving (United States)\n" +
                        "- **African Diaspora:** Carnival in Brazil, Juneteenth in the USA\n" +
                        "- **Asian:** Chinese New Year, Lantern Festival in Taiwan\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 5: Secular Festivals Celebrated Universally\n" +
                        "- **New Year's Eve/Day**\n" +
                        "- **Independence Day (various countries)**\n" +
                        "- **Labor Day**\n" +
                        "- **Mother’s and Father’s Day**\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 6: Mythological and Historical Origins of Festivals\n" +
                        "- **Roman Saturnalia:** Honored the god Saturn, celebrated with feasting and gift-giving.\n" +
                        "- **Greek Panathenaea:** Celebrated Athena, promoting unity and cultural pride.\n" +
                        "- **Chinese Mid-Autumn Festival:** Legends of Chang’e and the Moon Rabbit.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 7: Symbolism in Festivals\n" +
                        "- **Colors:** Red for good fortune (China), white for mourning (Japan).\n" +
                        "- **Symbols:** Holi colors represent joy; Christmas tree symbolizes everlasting life.\n" +
                        "- **Rituals:** Lighting lamps during Diwali signifies victory of light over darkness.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 8: Economic and Social Significance\n" +
                        "- **Community Building:** Festivals foster social bonds and unity.\n" +
                        "- **Economic Impact:** Tourism, local businesses thrive during festivals.\n" +
                        "- **Cultural Preservation:** Keeps traditions alive through generations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 9: Global Exchange of Festival Traditions\n" +
                        "- **Exchange Programs:** Sharing of cultural practices internationally.\n" +
                        "- **Cultural Tourism:** Travelers experience unique festival atmospheres.\n" +
                        "- **Hybrid Festivals:** Blending traditional and modern elements.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 10: Challenges in Preserving Traditional Festivals\n" +
                        "- **Urbanization:** Loss of traditional spaces for celebrations.\n" +
                        "- **Globalization:** Threat to local traditions by dominant cultures.\n" +
                        "- **Digital Influence:** Over-commercialization affecting authenticity.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "# Slide 11: Conclusion\n" +
                        "- **Importance of Cultural Diversity:** Celebrations enrich global cultural tapestry.\n" +
                        "- **Conservation Efforts:** Protecting and promoting traditional festivals globally.\n" +
                        "- **Future Trends:** Embracing technology to preserve traditions while making them accessible worldwide.\n" +
                        "\n" +
                        "```",
                "```markdown\n" +
                        "--------------------------\n" +
                        "Slide 1: Introduction to Food and Cuisine in Festivals\n" +
                        "- Introduction to the significance of food in cultural celebrations.\n" +
                        "- Overview of how traditional dishes play a role in festival traditions worldwide.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 2: Traditional Dishes from Asia\n" +
                        "- **Chinese New Year**: Dumplings (Jiaozi) symbolize wealth; longevity noodles represent long life.\n" +
                        "- **Japanese Setsubun**: Eating beans to ward off evil spirits and bring good luck.\n" +
                        "- **Indian Diwali**: Sweets like Ladoo and Gulab Jamun signify prosperity.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 3: Traditional Dishes from Europe\n" +
                        "- **Christmas in Italy**: Befana Cookies, symbolizing the gift-giver's visits.\n" +
                        "- **St. Patrick's Day in Ireland**: Corned Beef and Cabbage, representing Irish heritage.\n" +
                        "- **Easter in Spain**: Fabada Asturiana (Asturian Black Pudding Stew), a traditional Easter dish.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 4: Traditional Dishes from Africa\n" +
                        "- **Kwanzaa in the US**: Peanut Soup with Okra, celebrating unity and heritage.\n" +
                        "- **Mawlid Celebrations**: Sweet pastries like Maamoul, symbolizing generosity and sharing.\n" +
                        "- **Hari Raya Puasa**: Beef Rendang, a spicy and aromatic dish, marking religious festivals.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 5: Traditional Dishes from the Americas\n" +
                        "- **Thanksgiving in the US**: Turkey with stuffing, representing gratitude and harvest.\n" +
                        "- **Columbus Day Celebrations**: Empanadas, symbolizing indigenous flavors.\n" +
                        "- **Day of the Dead in Mexico**: Sugar Skulls (Calaveras de Azúcar), representing remembrance.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 6: Traditional Dishes from Oceania\n" +
                        "- **Easter in Australia**: Hot Cross Buns, symbolizing Christian tradition and history.\n" +
                        "- **Anzac Day Celebrations**: Anzac biscuits, honoring soldiers with a sweet twist.\n" +
                        "- **Matariki Festival in New Zealand**: Hangi (earth oven cooked food), celebrating the Māori culture.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 7: Traditional Dishes from the Middle East\n" +
                        "- **Nowruz Celebrations**: Sabzi Polo (Herb Rice), symbolizing spring and renewal.\n" +
                        "- **Eid al-Fitr**: Kebabs and Lamb Stew, marking the end of Ramadan fasting.\n" +
                        "- **Christmas in Lebanon**: Maamoul cookies filled with nuts or dates, representing hospitality.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 8: Cultural Significance of Traditional Festival Dishes\n" +
                        "- Emphasis on community bonding through shared meals.\n" +
                        "- Preservation of cultural heritage and identity.\n" +
                        "- Representation of historical and religious significance within the dishes.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 9: Impact of Globalization on Traditional Festive Cuisine\n" +
                        "- How globalization has influenced traditional festival food traditions.\n" +
                        "- Increased availability of ingredients and fusion dishes in celebrations worldwide.\n" +
                        "- Preservation vs. adaptation of traditional recipes across different regions.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 10: Conclusion - The Role of Food in Cultural Festivals\n" +
                        "- Summary of how food unites communities during festivals.\n" +
                        "- Importance of maintaining cultural culinary traditions.\n" +
                        "- Future trends in festival cuisine influenced by technology and global influences.\n" +
                        "\n" +
                        "```",
                "```markdown\n" +
                        "--------------------------\n" +
                        "Slide 1: Introduction to Multimedia Celebrations\n" +
                        "- **Defining Multimedia Celebrations**\n" +
                        "  - Integration of various media forms in festival traditions.\n" +
                        "  - Music, dance, art, and other artistic expressions enhance celebrations.\n" +
                        "- **Purpose**\n" +
                        "  - To provide a comprehensive overview of how different media elements contribute to the richness and diversity of festivals and holidays.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 2: The Role of Music\n" +
                        "- **Cultural Significance**\n" +
                        "  - Music reflects the history and values of a culture.\n" +
                        "  - Traditional instruments and musical styles unique to each festival.\n" +
                        "- **Examples**\n" +
                        "  - Mariachi in Mexican Day of the Dead celebrations.\n" +
                        "  - Samba during Rio Carnival.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 3: The Role of Dance\n" +
                        "- **Cultural Expression**\n" +
                        "  - Dances often tell stories or celebrate rituals.\n" +
                        "  - Different dance styles specific to various regions and traditions.\n" +
                        "- **Examples**\n" +
                        "  - Hula dancing in Hawaiian luaus.\n" +
                        "  - Salsa dancing at Cuban festivals.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 4: The Role of Art\n" +
                        "- **Visual Representation**\n" +
                        "  - Festivals showcase traditional arts like painting, sculpture, and crafts.\n" +
                        "  - Artists often interpret festival themes through their work.\n" +
                        "- **Examples**\n" +
                        "  - Sand art during the Holi festival in India.\n" +
                        "  - Mask-making in Indonesian festivals.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 5: Other Forms of Media\n" +
                        "- **Theatrical Performances**\n" +
                        "  - Plays, skits, and dramas performed during celebrations.\n" +
                        "  - Stories often rooted in folklore or history.\n" +
                        "- **Examples**\n" +
                        "  - Puppet shows in Chinese New Year festivities.\n" +
                        "  - Fireworks displays enhancing festival atmospheres.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 6: Technology's Impact on Multimedia Celebrations\n" +
                        "- **Digital Integration**\n" +
                        "  - Use of digital media to enhance traditional celebrations.\n" +
                        "  - Virtual reality experiences and live streaming for global audiences.\n" +
                        "- **Examples**\n" +
                        "  - Live streaming of festivals on social media platforms.\n" +
                        "  - Augmented reality apps adding interactive elements.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 7: Global Influence and Diversity in Media\n" +
                        "- **International Collaboration**\n" +
                        "  - Festivals attracting artists from around the world.\n" +
                        "  - Fusion of different artistic styles enriching celebrations.\n" +
                        "- **Examples**\n" +
                        "  - International dance troupes performing at global festivals.\n" +
                        "  - Multicultural art exhibitions during major events.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 8: Challenges and Solutions in Multimedia Celebrations\n" +
                        "- **Preserving Traditional Elements**\n" +
                        "  - Balancing modern media with cultural authenticity.\n" +
                        "  - Ensuring that traditional practices are not overshadowed by technology.\n" +
                        "- **Examples**\n" +
                        "  - Using digital screens alongside live performances.\n" +
                        "  - Incorporating interactive technologies without disrupting rituals.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 9: Future of Multimedia Celebrations\n" +
                        "- **Innovation in Festival Experiences**\n" +
                        "  - Advances in technology enabling more immersive experiences.\n" +
                        "  - Potential for virtual festivals as alternatives to traditional events.\n" +
                        "- **Examples**\n" +
                        "  - Virtual reality concerts during major festivals.\n" +
                        "  - Live interactive performances using AI and robotics.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 10: Conclusion\n" +
                        "- **Summary of Key Points**\n" +
                        "  - Multimedia elements enrich festival traditions by providing cultural depth and diversity.\n" +
                        "  - Technology plays a pivotal role in enhancing and expanding these celebrations globally.\n" +
                        "- **Final Thoughts**\n" +
                        "  - Emphasize the importance of preserving cultural heritage while embracing modern media for future generations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "```",
                "```markdown\n" +
                        "--------------------------\n" +
                        "Slide 1: Introduction to Festival Economics\n" +
                        "- Definition of festival economics\n" +
                        "- Importance of analyzing economic impact\n" +
                        "- Overview of key points to be covered\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 2: Direct Economic Contributions from Festivals\n" +
                        "- Sales generated during festivals (food, merchandise)\n" +
                        "- Tourism revenue and spending patterns\n" +
                        "- Employment opportunities created for local communities\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 3: Case Study – Oktoberfest in Munich\n" +
                        "- Historical background and significance\n" +
                        "- Estimated economic impact on the city\n" +
                        "- Factors contributing to its success as a tourist attraction\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 4: Economic Implications of Large-Scale Festivals\n" +
                        "- Infrastructure development and upgrades\n" +
                        "- Local businesses adapting to increased foot traffic\n" +
                        "- Potential strain on local resources and services\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 5: Challenges in Assessing Festival Economics\n" +
                        "- Methodologies for measuring economic impact\n" +
                        "- Limitations and biases in data collection\n" +
                        "- Need for comprehensive studies to provide accurate insights\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 6: Positive Economic Effects of Festivals\n" +
                        "- Increased foot traffic leading to higher sales\n" +
                        "- Boost in local business revenue\n" +
                        "- Improvement in the standard of living through job creation\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 7: Negative Economic Impacts of Festivals\n" +
                        "- Potential displacement of local businesses by larger chain stores\n" +
                        "- Short-term vs. long-term economic benefits\n" +
                        "- Environmental costs associated with increased activity\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 8: Tourism and Festivals: A Double-Edged Sword\n" +
                        "- How festivals attract tourists, boosting the local economy\n" +
                        "- Over-tourism leading to strain on resources and infrastructure\n" +
                        "- Strategies to balance economic growth with sustainability\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 9: Long-Term Economic Sustainability of Festivals\n" +
                        "- Reinvestment in local communities from festival profits\n" +
                        "- Diversification of economic activities beyond tourism\n" +
                        "- Planning for future growth while mitigating potential risks\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 10: Conclusion on Festival Economics\n" +
                        "- Summary of key findings\n" +
                        "- Importance of balanced and sustainable economic planning\n" +
                        "- Future directions for research and policy-making related to festivals and economics\n" +
                        "```",
                "```markdown\n" +
                        "--------------------------\n" +
                        "Slide 1: Introduction to Environmental Impact of Festivals and Holidays\n" +
                        "- **Title:** Environmental Impact of Festivals and Holidays\n" +
                        "- **Objective:** Understand the ecological footprint of large-scale celebrations.\n" +
                        "  \n" +
                        "--------------------------\n" +
                        "Slide 2: Types of Environmental Impacts\n" +
                        "- **Resource Consumption:** \n" +
                        "  - Water usage for ceremonies, food preparation, etc.\n" +
                        "  - Energy consumption from lighting, transportation, etc.\n" +
                        "- **Waste Generation:**\n" +
                        "  - Single-use plastics and packaging\n" +
                        "  - Food waste from excess consumption\n" +
                        "- **Pollution:**\n" +
                        "  - Air pollution from fireworks and combustion activities\n" +
                        "  - Noise pollution affecting local wildlife and communities\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 3: Case Study – Earth Day Celebrations\n" +
                        "- **Event:** Annual global event promoting environmental awareness.\n" +
                        "- **Impact:**\n" +
                        "  - Significant reduction in plastic usage through recycling initiatives.\n" +
                        "  - Promotion of sustainable practices among participants.\n" +
                        "  - Increased awareness leading to long-term behavioral changes.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 4: Environmental Mitigation Strategies\n" +
                        "- **Resource Efficiency:**\n" +
                        "  - Use of renewable energy sources like solar-powered stages and lighting systems.\n" +
                        "  - Water conservation measures such as rainwater harvesting.\n" +
                        "- **Waste Management:**\n" +
                        "  - Implementation of zero-waste policies with composting stations.\n" +
                        "  - Encouraging the use of reusable materials over single-use items.\n" +
                        "- **Pollution Control:**\n" +
                        "  - Reduction in fireworks usage or opting for eco-friendly alternatives.\n" +
                        "  - Noise barriers to minimize disturbance.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 5: Economic vs. Environmental Considerations\n" +
                        "- **Economic Benefits:** \n" +
                        "  - Boost to local businesses through increased footfall during festivals.\n" +
                        "  - Tourism revenue generated from visitors attending cultural events.\n" +
                        "- **Environmental Costs:**\n" +
                        "  - Cleanup expenses related to waste management.\n" +
                        "  - Health impacts due to pollution, affecting tourism and community well-being.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 6: Policy Implications\n" +
                        "- **Regulatory Frameworks:** \n" +
                        "  - Implementation of environmental regulations for large gatherings.\n" +
                        "  - Enforcement of waste disposal laws during festivals.\n" +
                        "- **Public Awareness Campaigns:**\n" +
                        "  - Educating attendees about sustainable practices.\n" +
                        "  - Promoting recycling and conservation efforts among participants.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 7: Technological Innovations in Sustainability\n" +
                        "- **Sustainable Transportation Options:**\n" +
                        "  - Encouraging public transport use through shuttle services.\n" +
                        "  - Offering incentives for carpooling or electric vehicle usage.\n" +
                        "- **Digital Solutions:**\n" +
                        "  - Virtual ticketing systems to reduce paper waste.\n" +
                        "  - Online platforms for event registration and communication.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 8: Global Examples of Environmentally Friendly Festivals\n" +
                        "- **Ibiza’s Summer Festival:**\n" +
                        "  - Adoption of renewable energy sources on the island.\n" +
                        "  - Implementation of recycling programs throughout venues.\n" +
                        "- **Glastonbury Festival:**\n" +
                        "  - Commitment to carbon neutrality through offsetting and sustainable practices.\n" +
                        "  - Use of biodiesel generators for energy needs.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 9: Future Prospects\n" +
                        "- **Sustainability Goals:** \n" +
                        "  - Setting targets for reducing waste and increasing renewable energy use.\n" +
                        "  - Encouraging participation in environmental initiatives by festival-goers.\n" +
                        "- **Community Engagement:**\n" +
                        "  - Collaborating with local communities to foster sustainable practices.\n" +
                        "  - Organizing workshops on eco-friendly living and conservation.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 10: Conclusion\n" +
                        "- **Summary of Key Points:** \n" +
                        "  - Festivals and holidays significantly impact the environment through resource consumption, waste generation, and pollution.\n" +
                        "  - Implementing sustainable strategies can mitigate negative effects while enhancing the positive aspects of these celebrations.\n" +
                        "  - Collaboration between organizers, attendees, and policymakers is essential for promoting a greener festival culture.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "```",
                "```markdown\n" +
                        "--------------------------\n" +
                        "Slide 1: Introduction\n" +
                        "International Traditions: Highlighting Unique Festivals Around the Globe\n" +
                        "\n" +
                        "- **Global Diversity:** Celebrate the rich tapestry of international festivals.\n" +
                        "- **Cultural Exchange:** Festivals bring people together, fostering global understanding.\n" +
                        "- **Economic and Social Impact:** How these traditions contribute to local economies and communities.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 2: Holi - India\n" +
                        "The Festival of Colors\n" +
                        "\n" +
                        "- **Origins:** Celebrates the victory of good over evil.\n" +
                        "- **Significance:** Marks the arrival of spring with vibrant colors and joyous music.\n" +
                        "- **Activities:** Throwing colored powders, singing, dancing.\n" +
                        "- **Global Recognition:** Celebrated internationally by diaspora communities.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 3: Carnaval - Brazil\n" +
                        "The World's Largest Street Festival\n" +
                        "\n" +
                        "- **Historical Roots:** Originates from Roman Catholic traditions.\n" +
                        "- **Celebration Scale:** Millions of participants, vibrant parades, and performances.\n" +
                        "- **Cultural Expression:** A melting pot of Afro-Brazilian influences with samba dancing.\n" +
                        "- **International Appeal:** Draws tourists worldwide, boosting local tourism.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 4: Oktoberfest - Germany\n" +
                        "The World's Largest Voluntary Gathering\n" +
                        "\n" +
                        "- **Historical Background:** Began in Munich as a royal wedding celebration.\n" +
                        "- **Main Event:** Traditional Bavarian beer and food, live music, and cultural performances.\n" +
                        "- **Global Presence:** Celebrations outside of Germany, promoting German culture globally.\n" +
                        "- **Economic Impact:** Boosts local businesses, hotels, and tourism industry.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 5: Diwali - India\n" +
                        "The Festival of Lights\n" +
                        "\n" +
                        "- **Religious Significance:** Marks the victory of light over darkness, good over evil.\n" +
                        "- **Cultural Practices:** Lighting diyas (oil lamps), exchanging gifts, wearing new clothes.\n" +
                        "- **International Celebrations:** Marked in countries with significant Indian populations.\n" +
                        "- **Symbolism:** Represents hope and prosperity, celebrated worldwide.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 6: New Year's Eve - Worldwide\n" +
                        "\n" +
                        "- **Celebration Variations:** Different customs around the globe.\n" +
                        "- **Activities:** Watching fireworks, making resolutions, sharing meals.\n" +
                        "- **Global Significance:** A day for reflection and renewal across cultures.\n" +
                        "- **Environmental Concerns:** Reducing waste during celebrations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 7: Day of the Dead - Mexico\n" +
                        "\n" +
                        "- **Cultural Roots:** Honors deceased loved ones with vibrant rituals.\n" +
                        "- **Traditional Practices:** Building altars, creating sugar skulls, decorating cemeteries.\n" +
                        "- **Global Recognition:** Increasing popularity worldwide as a unique cultural event.\n" +
                        "- **Economic Impact:** Boosts tourism to Mexico during the celebrations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 8: Hanukkah - Israel and Global Communities\n" +
                        "\n" +
                        "- **Historical Context:** Celebrates the rededication of the Second Temple in Jerusalem.\n" +
                        "- **Cultural Practices:** Lighting candles on a menorah, eating fried foods like latkes and sufganiyot.\n" +
                        "- **International Observance:** Observed by Jewish communities worldwide with varying traditions.\n" +
                        "- **Global Significance:** Promotes cultural diversity and understanding.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 9: Christmas - Worldwide\n" +
                        "\n" +
                        "- **Religious Origin:** Celebrates the birth of Jesus Christ.\n" +
                        "- **Cultural Practices:** Decorating trees, exchanging gifts, attending church services.\n" +
                        "- **Commercial Aspect:** A major shopping season with significant economic impact.\n" +
                        "- **Cultural Exchange:** Different traditions in various countries influencing each other.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 10: Chinese New Year - Global Celebrations\n" +
                        "\n" +
                        "- **Cultural Significance:** Marks the beginning of a new year on the lunar calendar.\n" +
                        "- **Traditional Practices:** Cleaning houses, receiving red envelopes with money, dragon dances.\n" +
                        "- **International Participation:** Celebrated by Chinese diaspora and those interested in Asian culture.\n" +
                        "- **Economic Impact:** Boosts tourism and local businesses during the festivities.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 11: Eid al-Fitr - Muslim Communities\n" +
                        "\n" +
                        "- **Religious Significance:** Marks the end of Ramadan, celebrating with prayers and feasts.\n" +
                        "- **Cultural Practices:** Wearing new clothes, exchanging gifts, sharing meals with family.\n" +
                        "- **Global Reach:** Celebrated by Muslims worldwide, promoting interfaith understanding.\n" +
                        "- **Economic Impact:** Boosts local businesses and tourism in regions hosting major celebrations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 12: Bastille Day - France\n" +
                        "\n" +
                        "- **Historical Significance:** Commemorates the storming of the Bastille prison during the French Revolution.\n" +
                        "- **Cultural Practices:** Fireworks displays, military parades, patriotic speeches.\n" +
                        "- **International Recognition:** A symbol of freedom and democracy celebrated globally.\n" +
                        "- **Economic Impact:** Enhances tourism and local businesses in France.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 13: Honeymoon - Japan\n" +
                        "\n" +
                        "- **Unique Festival:** Celebrates the joy of newlyweds with traditional music and dance.\n" +
                        "- **Cultural Practices:** Wearing traditional attire, participating in rituals.\n" +
                        "- **Global Significance:** Reflects Japanese cultural values and hospitality.\n" +
                        "- **Economic Contribution:** Supports local tourism and hospitality industries.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 14: Oktoberfest - United States\n" +
                        "\n" +
                        "- **Adaptation of Traditional Festival:** Celebrated in various U.S. cities with beer, food, and music.\n" +
                        "- **Cultural Fusion:** Blending German traditions with American culture.\n" +
                        "- **Global Impact:** Promotes German-American cultural exchange.\n" +
                        "- **Economic Benefits:** Boosts local businesses and tourism.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 15: Chinese Lantern Festival - Global Celebrations\n" +
                        "\n" +
                        "- **Historical Background:** Marks the first full moon of spring, symbolizing unity.\n" +
                        "- **Traditional Practices:** Floating lanterns, performing lion dances, enjoying traditional snacks.\n" +
                        "- **International Participation:** Celebrated by Chinese communities worldwide with unique twists.\n" +
                        "- **Cultural Significance:** Promotes understanding and appreciation of Chinese culture.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 16: Diwali - Global Diversity\n" +
                        "\n" +
                        "- **Cultural Practices Across Continents:** Celebrations vary based on local traditions.\n" +
                        "- **Significance to Various Religions:** Hindu, Jain, Sikh, and others observe the festival differently.\n" +
                        "- **International Recognition:** Marked by diverse customs highlighting cultural diversity.\n" +
                        "- **Economic Impact:** Boosts tourism and business activities in regions with large Indian populations.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 17: Christmas - Global Variations\n" +
                        "\n" +
                        "- **Cultural Traditions Around the World:** From Santa Claus in the US to Pesebre in Spain.\n" +
                        "- **Commercial Celebrations:** Gift-giving traditions differ across cultures.\n" +
                        "- **Community Spirit:** Bringing people together through shared celebrations.\n" +
                        "- **Global Significance:** A unifying cultural event celebrated worldwide.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 18: Eid al-Adha - Muslim Festivals\n" +
                        "\n" +
                        "- **Historical and Religious Significance:** Commemorates the willingness of Ibrahim to sacrifice his son.\n" +
                        "- **Cultural Practices:** Sharing meat with community, wearing new clothes, participating in prayers.\n" +
                        "- **Global Impact:** Promotes unity among Muslims worldwide.\n" +
                        "- **Economic Contribution:** Supports local businesses during the festivities.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 19: Hanukkah - Global Observance\n" +
                        "\n" +
                        "- **Adaptation of Traditions:** Celebrated with menorah lighting, traditional foods, and community gatherings.\n" +
                        "- **Cultural Fusion:** Reflects the integration of Jewish communities into diverse cultures.\n" +
                        "- **International Significance:** Promotes cultural diversity and understanding across borders.\n" +
                        "- **Economic Benefits:** Boosts local businesses during the holiday season.\n" +
                        "\n" +
                        "--------------------------\n" +
                        "Slide 20: Conclusion\n" +
                        "\n" +
                        "- **Importance of Cultural Diversity in Celebrations:** Each festival reflects unique traditions, beliefs, and values.\n" +
                        "- **Promoting Interconnectedness:** Global celebrations foster unity and mutual respect among cultures.\n" +
                        "- **Sustainability Efforts:** Addressing environmental concerns during festive activities.\n" +
                        "- **Future Prospects:** Continued evolution and adaptation of traditional festivals worldwide.\n" +
                        "\n" +
                        "</details>\n" +
                        "```"
        };
        Utils.generateOutput(topic, subTopics, subTopicContent);
    }

    private static void updateModelList() {
        try {
            List<String> models = new OllamaClient().listModelNames();
            if (models == null || models.isEmpty()) modelNames = new String[0];
            else modelNames = models.toArray(new String[0]);
        } catch (Exception ex) {
            System.err.println("Failed to get model list from ollama");
            modelNames = new String[0];
        }
    }

    private static void disposeSplash() {
        if (splash != null) {
            splash.setVisible(false);
            try {
                splash.dispose();
            } catch (Exception ignored) {
            }
        }
    }


}
