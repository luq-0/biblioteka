package rs.luka.biblioteka.data;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import rs.luka.biblioteka.exceptions.ConfigException;
import rs.luka.biblioteka.funkcije.Utils;
import rs.luka.biblioteka.grafika.Konstante;
import static rs.luka.biblioteka.data.Strings.loadStrings;
import static rs.luka.biblioteka.grafika.Konstante.*;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;


public class Config {
    private static final java.util.logging.Logger LOGGER = 
            java.util.logging.Logger.getLogger(Config.class.getName());



    /**
     * Properties.
     */
    private static Properties config;
    /**
     * Default vrednosti za config.
     */
    private static final Properties defaults = new Properties();
    /**
     * Fajl odakle se ucitava config.
     */
    private static File configFile;
    /**
     * Komentar za config fajl, koji se upisuje na pocetku fajla.
     */
    private static final String configMsg = "ucSize i knjSize - koliko ima ucenika i knjiga, "
            + "predvidjena velicina lista. Opciono.\n"
            + "firstRun - true ili false, oznacava da li se program pokrece po prvi put. Ako da, "
            + "otvara prozor za unos podataka.\n"
            + "dateLimit - broj dana koliko ucenik moze da zadrzi knjigu kod sebe. Default je 14\n"
            + "lookAndFeel - generalni izgled prozora i grafickih komponenti. Vrednosti:"
            + "system, ocean, metal, nimbus, motif. Izbegavati nimbus i motif\n"
            + "brKnjiga - maksimalan broj knjiga koje ucenik moze da ima kod sebe\n"
            + "bgBoja - RGB vrednost, pozadinska boja svih prozora\n"
            + "fgBoja - RGB vrednost, boja fonta\n"
            + "TFColor - RGB vrednost, boja polja za unos teksta\n"
            + "logLevel - minimalan nivo poruka koje se loguju. Default je INFO\n"
            + "savePeriod - broj minuta na koji se vrsi automatsko cuvanje podataka\n"
            + "maxUndo - maksimalan broj akcija koje se nalaze u undo stack-u\n"
            + "razredi - String validnih razreda, razdvojenih zapetom\n"
            + "workingDir - radni direktorijum aplikacije\n"
            + "logSizeLimit i logCount - broj i velicina log fajla (fajlova)\n"
            + "label,but i smallBut Font Name/Size/Weight - osobine fontova koriscenih za labele i dugmad\n"
            + "kPrefix - prefix za kljuceve koji oznacavaju da se vrednost odnosi na konstantu,"
            + "default je k_\n"
            + "ucSort - metoda sortiranja ucenika. Default je po razredu (razred||raz), moze i po imenu (ime)\n"
            + "shiftKnjige - odredjuje da li se pri vracanju sve knjige pomeraju ulevo, tako da knjige"
            + "koje se trenutno nalaze kod ucenika popunjavaju prva prazna mesta (poravnate ulevo)\n"
            + "customSize - oznacava da li se velicina prozora odredjuje automatski ili putem konstanti";
    
    private static String KONSTANTE_PREFIX = "k_";
    /**
     * Sve vrednosti koje kljuc moze da ima, sem onih koji pocinju sa k_ (koje se nalaze u Konstante.java).
     */
    private static final StringMultiMap vrednosti = new StringMultiMap();
    /**
     * Limiti za sve vrednosti.
     */
    private static final Map<String, Limit> limiti = new HashMap<>();

    //LIMITI ZA CONFIG
    private static final Limit SIRINA = new Limit(100, 6_000);
    private static final Limit VISINA = new Limit(70, 5_000);
    private static final Limit BR_KNJIGA = new Limit(1, 25);
    private static final Limit UC_KNJ_SIZE = new Limit(30);
    private static final Limit DATE_LIMIT = new Limit(1, 365);
    private static final Limit SAVE_PERIOD = new Limit(0, 1440); //1 dan
    private static final Limit UNDO = new Limit();
    private static final Limit LOG_SIZE = new Limit(0, 100_000_000);
    private static final Limit LOG_COUNT = new Limit(0, 10_000);
    private static final Limit LABEL_FONT = new Limit(1, 50);
    private static final Limit BUTTON_FONT = new Limit(1, 30);
    private static final Limit DATE_CHECK_LIMIT = new Limit(0, 30); //1 mesec

    /**
     * Ucitava config iz fajla u Properties.
     */
    public static void loadConfig() {
        setDefaults();
        loadStrings();
        defineSynonyms();
        setLimits();
        configFile = new File(Utils.getWorkingDir() + "config.properties");
        config = new Properties(defaults);
        String path = null;
        try {
            configFile.createNewFile();
            path = configFile.getCanonicalPath();
            FileReader configFR = new FileReader(configFile);
            Config.config.load(configFR);
            setKPrefix();
            resolveKeys();
        } catch (FileNotFoundException ex) {
            showMessageDialog(null, LOADCONFIG_FNFEX_MSG_STRING + path, LOADCONFIG_FNFEX_TITLE_STRING, 
                    ERROR_MESSAGE);
        } catch (IOException ex) {
            showMessageDialog(null, LOADCONFIG_IOEX_MSG_STRING, LOADCONFIG_IOEX_TITLE_STRING, ERROR_MESSAGE);
        }
    }

    /**
     * Podesava hard-coded default vrednosti.
     */
    private static void setDefaults() {
        defaults.setProperty("knjSize", "300");
        defaults.setProperty("ucSize", "550");
        defaults.setProperty("firstRun", "true");
        defaults.setProperty("dateLimit", "14");
        defaults.setProperty("lookAndFeel", "ocean");
        defaults.setProperty("brKnjiga", "3");
        defaults.setProperty("TFBoja", "false");
        defaults.setProperty("logLevel", "INFO");
        defaults.setProperty("savePeriod", "5");
        defaults.setProperty("maxUndo", "50");
        defaults.setProperty("logSizeLimit", "10000000");
        defaults.setProperty("logFileCount", "15");
        defaults.setProperty("customSize", "false");
    }

    /**
     * Podesava sinonime za kljuceve u configu.
     *
     * @since 25.10.'14.
     */
    private static void defineSynonyms() {
        vrednosti.put("ucSize", "ucSize", "brojUcenika");
        vrednosti.put("knjSize", "knjSize", "brojKnjiga");
        vrednosti.put("firstRun", "firstRun", "prvoPokretanje");
        vrednosti.put("dateLimit", "dateLimit", "maxDana", "zadrzavanje", "zadrzavanjeKnjige", 
                CONFIG_DATELIMIT_DESC);
        vrednosti.put("lookAndFeel", "lookAndFeel", "LaF", "LnF", "izgled", CONFIG_LOOKANDFEEL_DESC);
        vrednosti.put("brKnjiga", "brKnjiga", "maxBrojKnjigaPoUceniku", "maxKnjiga", "maxUcenikKnjiga",
                CONFIG_BRKNJIGA_DESC);
        vrednosti.put("bgBoja", "bgBoja", "bojaPozadine", "pozadinskaBoja", "bgColor");
        vrednosti.put("fgBoja", "fgBoja", "bojaTeksta", "fgColor");
        vrednosti.put("TFBoja", "TFBoja", "bojitiPoljaZaUnosTeksta");
        vrednosti.put("TFColor", "TFColor", "bojaPolja", "bojaPoljaZaUnosTeksta");
        vrednosti.put("logLevel", "logLevel", "nivoLogovanja", CONFIG_LOGLEVEL_DESC);
        vrednosti.put("savePeriod", "savePeriod", "autosavePeriod", "saveInterval", "autosaveInterval", "intervalCuvanja",
                CONFIG_SAVEPERIOD_DESC);
        vrednosti.put("maxUndo", "maxUndo", "undoStackDepth", "velicinaUndoStacka", CONFIG_MAXUNDO_DESC);
        vrednosti.put("razredi", "razredi", "razrediUcenika", "validniRazredi", CONFIG_RAZREDI_DESC);
        vrednosti.put("workingDir", "workingDir", "workingDirectory", "Radni direktorijum", "dataDir",
                CONFIG_WORKINGDIR_DESC);
        vrednosti.put("logSizeLimit", "logSizeLimit", "logSize", "logLimit", "logFileSizeLimit", "velicinaLogFajla",
                CONFIG_LOGSIZELIMIT_DESC);
        vrednosti.put("logFileCount", "logFileCount", "logCount", "logFileNumber", "brojLogFajlova",
                CONFIG_LOGFILECOUNT_DESC);
        vrednosti.put("labelFontName", "labelFontName", "labelName", "fontName", "font", 
                CONFIG_LABELFONTNAME_DESC);
        vrednosti.put("labelFontSize", "labelFontSize", "labelSize", "fontSize");
        vrednosti.put("labelFontWeight", "labelFontWeight", "labelWeight", "fontWeight");
        vrednosti.put("butFontName", "butFontName", "buttonFontName", "butName", "buttonName",
                CONFIG_BUTFONTNAME_DESC);
        vrednosti.put("butFontSize", "butFontSize", "buttonFontSize", "butSize", "buttonSize");
        vrednosti.put("butFontWeight", "butFontWeight", "buttonFontWeight", "butWeight", "buttonWeight");
        vrednosti.put("smallButFontName", "smallButFontName", "sButFontName", "sButName", "smallButtonName",
                "UzmiVratiFontName", "UzmiVratiName", "UVFont", CONFIG_SMALLBUTFONTNAME_DESC);
        vrednosti.put("smallButFontSize", "smallButFontSize", "sButFontSize", "sButSize", "smallButtonSize",
                "UzmiVratiFontSize", "UzmiVratiSize", "UVSize");
        vrednosti.put("smallButWeight", "smallButWeight", "smallButtonWeight", "sButW", "sButWeight",
                "UzmiVratiFontWeight", "UzmiVratiWeight", "UVWeight");
        vrednosti.put("kPrefix", "kPrefix", "konstantePrefix", "PrefixZaKonstante");
        vrednosti.put("ucSort", "ucSort", "uceniciSort", "sortiratiUcenikePo", "uceniciSortKriterijum");
        vrednosti.put("datePeriod", "datePeriod", "dateCheckPeriod", CONFIG_DATEPERIOD_DESC);
        vrednosti.put("shiftKnjige", "shiftKnjige", "pomerajKnjige", "pomerajUcenikKnjige", "alignKnjige");
        vrednosti.put("customSize", "customSize", "useCustomSize", "customVelicina");
    }

    /**
     * Postavlja limite u mapi koristeći vrednosti iz fieldova.
     *
     * @since 11.'14
     */
    private static void setLimits() {
        limiti.put("ucSize", UC_KNJ_SIZE);
        limiti.put("knjSize", UC_KNJ_SIZE);
        limiti.put("dateLimit", DATE_LIMIT);
        limiti.put("knjigeS", SIRINA);
        limiti.put("knjigeV", VISINA);
        limiti.put("uceniciS", SIRINA);
        limiti.put("uceniciV", VISINA);
        limiti.put("brKnjiga", BR_KNJIGA);
        limiti.put("savePeriod", SAVE_PERIOD);
        limiti.put("maxUndo", UNDO);
        limiti.put("logSizeLimit", LOG_SIZE);
        limiti.put("logFileCount", LOG_COUNT);
        limiti.put("labelFontSize", LABEL_FONT);
        limiti.put("butFontSize", BUTTON_FONT);
        limiti.put("smallButFontSize", BUTTON_FONT);
        limiti.put("datePeriod", DATE_CHECK_LIMIT);
    }

    /**
     * Radi iteraciju preko configa i zamenjuje ključeve ako su sinonimi sa
     * glavnim (iz mape vrednosti). Postavlja grafičke konstante (o istom
     * trošku).
     *
     * @since 7.11.'14.
     */
    private static void resolveKeys() {
        Entry e;
        for (Iterator<Entry<Object, Object>> it = config.entrySet().iterator(); it.hasNext();) {
            e = it.next();
            if (e.getKey().toString().startsWith("k_")) {
                set(e.getKey().toString(), e.getValue().toString());
            } else if (!vrednosti.containsKey(e.getKey())) {
                config.put(vrednosti.getKey((String) e.getKey()), e.getValue());
                it.remove();
            }
        }
    }
    
    /**
     * Postavlja prefix za konstante, ako postoji u configu. Raditi pre resolveKeys.
     */
    private static void setKPrefix() {
        if(config.containsKey("kPrefix"))
            KONSTANTE_PREFIX = config.getProperty("kPrefix");
    }

    /**
     * Cuva config u fajl.
     */
    private static void storeConfig() {
        try {
            config.store(new FileWriter(configFile), configMsg);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "I/O greška pri čuvanju konfiguracijskog fajla", ex);
            showMessageDialog(null, STORECONFIG_IOEX_MSG_STRING, STORECONFIG_IOEX_TITLE_STRING, 
                    ERROR_MESSAGE);
        }
    }

    /**
     * Proverava da li config sadrzi dati kljuc ili sinonim.
     *
     * @param key kljuc koji se trazi
     * @return true ako sadrzi, false u suprotnom
     * @since 25.10'.14.
     */
    public static boolean hasKey(String key) {
        String realKey = vrednosti.getKey(key);
        if(realKey==null)
            return false;
        else return config.containsKey(realKey);
    }

    /**
     * Vraca vrednost koja je povezana sa ovim kljucem ili sinonimom. Ako ne
     * postoji, vraca default vrednost. Ako ni default ne postoji, vraca null.
     *
     * @param key kljuc ili sinonim
     * @return String vrednost
     */
    public static String get(String key) {
        if (key == null) {
            return null;
        }
        return config.getProperty(vrednosti.getKey(key));
    }

    /**
     * Vraca vrednost koja je povezana sa ovim kljucem ili sinonimom. Ako ne
     * postoji, vraca def.
     *
     * @param key kljuc ili sinonim
     * @param def default vrednost
     * @return vrednost koja je povezana sa datim kljucem ili sinonimom ili def.
     */
    public static String get(String key, String def) {
        return config.getProperty(vrednosti.getKey(key), def);
    }

    /**
     * Vraca integer reprezentaciju trazenog kljuca ili njegovog sinonima.
     *
     * @param key kljuc koji se trazi
     * @return vrednost kljuca kao int.
     * @throws NumberFormatException ako vrednost nije int
     * @since 25.10'.14.
     */
    public static int getAsInt(String key) {
        return Integer.parseInt(get(key));
    }

    /**
     * Vraca integer reprezentaciju trazenog kljuce ili njgeovog sinonima. Ako ne postoji, vraca default
     * u int obliku.
     * @param key kljuc cija se vrednost trazi
     * @param def default vrednost
     * @return odgovarajuca vrednost za kljuc
     */
    public static int getAsInt(String key, String def) {
        return Integer.parseInt(get(key, def));
    }

    /**
     * Vraca boolean reprezentaciju trazenog kljuca ili njegovog sinonima. Ako
     * je vrednost kljuca int, koristi {@link Utils#parseBoolean(int)} da dobije
     * boolean, u suprotnom uporedjuje String sa "true".
     *
     * @param key kljuc koji se trazi ili sinonim.
     * @return boolean koji se dobija na opisani nacin
     * @since 25.10'.14.
     */
    public static boolean getAsBool(String key) {
        String val = get(key);
        if (val == null) {
            return false;
        }
        if (Utils.isInteger(val)) {
            return Utils.parseBoolean(Integer.parseInt(val));
        }
        return val.equalsIgnoreCase("true");
    }

    /**
     * Postavlja dati kljuc na datu vrednost, ako su oba validna. Ignorise
     * veliko/malo slovo.
     *
     * @param key kljuc
     * @param val vrednost
     * @see #isNameValid(java.lang.String, java.lang.String)
     * @since 25.10.'14.
     */
    public static void set(String key, String val) {
        if (!isNameValid(key, val)) {
            throw new IllegalArgumentException("Vrednost " + val + " nije validna za kljuc " + key);
        }
        if (key.startsWith(KONSTANTE_PREFIX)) {
            Konstante.set(key.substring(2), val);
            return;
        }

        String realKey = vrednosti.getKey(key);
        checkValue(realKey, val); //throwuje ConfigException-e (koji su Runtime)

        if (limiti.containsKey(key)) {
            config.setProperty(realKey,
                limiti.get(key).limit(val));
        } else {
            config.setProperty(realKey, val);
        }
        LOGGER.log(Level.CONFIG, "{0} podešen na {1}", new String[]{key, val});
        storeConfig();
    }

    /**
     * Proverava da li je data vrednost dozvoljena za dati kljuc. Razredi moraju
     * da budu validni integeri razdvojeni zapetama, logLevel integer ili
     * validan string, lookAndFeel jedan on system, crossOcean ili crossMetal,
     * firstRun i TFBoja 0 ili 1 ili true ili false, sve ostale vrednosti
     * integeri.
     *
     * @param key kljuc u configu
     * @param val vrednost u configu.
     * @return true ako sme da postoji, false u suprotnom.
     * @since 24.10.'14.
     */
    private static boolean isNameValid(String key, String val) {
        if (key.startsWith(KONSTANTE_PREFIX)) {
            return true;
        }
        if (!vrednosti.contains(key)) {
            System.out.println(key + " ne postoji");
            return false;
        }
        val = val.toLowerCase();
        String realKey = vrednosti.getKey(key).toLowerCase();
        if ("razredi".equals(realKey)) {
            String[] razredi = val.split(",");
            for (String razred : razredi) {
                razred = razred.trim();
                if (!Utils.isInteger(razred) && !Ucenik.isRazredValid(Integer.parseInt(razred))) {
                    return false;
                }
            }
            return true;
        }
        if ("loglevel".equals(realKey)) {
            try {
                Level.parse(val.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return false;
            }
            return true;
        }
        if ("lookandfeel".equals(realKey)) {
            return val.equals("system") || val.equals("ocean") || val.equals("metal")
                    || val.equals("nimbus") || val.equals("motif") || val.equals("win classic");
        }
        if ("firstrun".equals(realKey) || "tfboja".equals(realKey)) {
            return val.equals("0") || val.equals("1") || val.equals("true") || val.equals("false");
        }
        if ("bgboja".equalsIgnoreCase(realKey) || "fgboja".equals(realKey)
                || "tfcolor".equalsIgnoreCase(realKey)) {
            try {
                Color.decode(val);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        } else if(realKey.endsWith("weight")) {
            return val.equals("bold") || val.equals("italic") || val.equals("plain") ||
                    (val.startsWith("bold") && val.endsWith("italic"));
        } else if(realKey.endsWith("name") || realKey.equals("kprefix")) {
            return true;
        } else if(realKey.equals("ucSort")) {
            return val.equals("ime") || val.equals("razred") || val.equals("raz");
        } else if(realKey.equals("dateperiod")) {
            return Utils.isDouble(val);
        } else {
            return Utils.isInteger(val);
        }
    }

    /**
     * Proverava da li su vrednosti validne za brKnjiga, razrede i workingDir
     * @param key ako je jedan od 3 koji se proverava, radi proveru, u suprotnom ignorise
     * @param val vrednost kljuca
     */
    private static void checkValue(String key, String val) {
        Iterator<Ucenik> iterator;
        switch (key) {
            case "brKnjiga":
                int valInt = Integer.parseInt(val);
                iterator = Podaci.iteratorUcenika();
                iterator.forEachRemaining((Ucenik uc) -> {
                    if (uc.getBrojKnjiga() > valInt) {
                        throw new ConfigException("brKnjiga");
                    }
                });
                break;
            case "razredi":
                String[] valsStr = val.split(",");
                int[] vals = new int[valsStr.length];
                for (int i = 0; i < vals.length; i++) {
                    vals[i] = Integer.parseInt(valsStr[i]);
                }
                iterator = Podaci.iteratorUcenika();
                iterator.forEachRemaining((Ucenik uc) -> {
                    if (!Utils.arrayContains(vals, uc.getRazred())) {
                        throw new ConfigException("razredi");
                    }
                });
                break;
            case "workingDir":
                File folder = new File(val);
                if (!folder.isDirectory() && !folder.mkdir()) {
                    throw new ConfigException("workingDir");
                }
        }
    }

    /**
     * Vraca dati kljuc na default vrednost ili null ako default ne postoji.
     *
     * @param key kljuc koji treba resetovati
     * @since 25.10'.14.
     */
    public static void reset(String key) {
        config.setProperty(key, defaults.getProperty(key));
    }

    /**
     * Vraca nazive svih kljuceva u listi, ako imaju puno ime (vise reci sa
     * razmakom).
     *
     * @return ime kljuca koja je user-friendly
     * @since 26.10.'14.
     */
    public static ArrayList<String> getUserFriendlyNames() {
        ArrayList<String> vals = new ArrayList<>();
        for (int i = 0; i < vrednosti.size(); i++) {
            if (vrednosti.getLastValue(i).contains(" ")) {
                vals.add(vrednosti.getLastValue(i));
            }
        }
        return vals;
    }
    
    //==========LIMITS==========================================================

    /**
     * Sastoji se od minimalne i maksimalne vrednosti i funkcije koja uzima int i vraca validnu vrednost.
     */
    private static class Limit {

        final int MIN;
        final int MAX;

        /**
         * Kreira Limit sa datim MIN- i MAX-om
         * @param MIN minimalna vrednost za int
         * @param MAX maksimala vrednost za int
         */
        private Limit(int MIN, int MAX) {
            this.MAX = MAX;
            this.MIN = MIN;
        }

        /**
         * Kreira Limit kojem su MIN i MAX {@link Integer#MIN_VALUE} i {@link Integer#MAX_VALUE}
         */
        private Limit() {
            this.MAX = Integer.MAX_VALUE;
            this.MIN = 0;
        }

        /**
         * Kreira limit kojem je MIN data vrednost, a max {@link Integer#MAX_VALUE}.
         * @param MIN
         */
        private Limit(int MIN) {
            this.MIN = MIN;
            this.MAX = Integer.MAX_VALUE;
        }

        /**
         * Vraca dati int ako se nalazi izmedju min i max-a. U suprotom, vraca MIN ili MAX, u zavisnosti
         * sta je blize
         * @param val vrednost
         * @return validna vrednost (val, MIN ili MAX)
         */
        public int limit(int val) {
            return Integer.max(Integer.min(val, MAX), MIN);
        }

        /**
         * String wrapper za {@link #limit(int)}. Prihvata float-ove
         * @param val vrednost kao String
         * @return validna vrednost kao String
         */
        public String limit(String val) {
            return String.valueOf(limit((int)Math.ceil(Float.valueOf(val))));
        }
    }
}
