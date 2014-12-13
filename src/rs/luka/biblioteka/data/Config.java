package rs.luka.biblioteka.data;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.String.valueOf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import static rs.luka.biblioteka.data.Podaci.getBrojKnjiga;
import static rs.luka.biblioteka.data.Podaci.getBrojUcenika;
import rs.luka.biblioteka.exceptions.ConfigException;
import rs.luka.biblioteka.funkcije.Utils;
import rs.luka.biblioteka.grafika.Konstante;

/**
 * Klasa sa konfiguracijskim fajlom.
 *
 * @author luka
 * @since 22.8.'14.
 */
public class Config {
    
    private static class Limit {
        final int MIN;
        final int MAX;
        private Limit(int MIN, int MAX) {
            this.MAX = MAX;
            this.MIN = MIN;
        }
        private Limit() {
            this.MAX = Integer.MAX_VALUE;
            this.MIN = Integer.MIN_VALUE;
        }
    }

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Config.class.getName());

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
            + "uceniciS i uceniciV - sirina i visina prozora za pregled ucenika\n"
            + "knjigeS i knjigeV - sirina i visina prozora za pregled knjiga\n"
            + "brKnjiga - maksimalan broj knjiga koje ucenik moze da ima kod sebe\n"
            + "bgBoja - RGB vrednost, pozadinska boja svih prozora\n"
            + "fgBoja - RGB vrednost, boja fonta\n"
            + "TFColor - RGB vrednost, boja polja za unos teksta\n"
            + "logLevel - minimalan nivo poruka koje se loguju. Default je INFO\n"
            + "savePeriod - broj minuta na koji se vrsi automatsko cuvanje podataka\n"
            + "maxUndo - maksimalan broj akcija koje se nalaze u undo stack-u\n"
            + "razredi - String validnih razreda, razdvojenih zapetom\n"
            + "workingDir - radni direktorijum aplikacije\n"
            + "logSizeLimit i logCount - broj i velicina log fajla (fajlova)";

    private static final StringMultiMap vrednosti = new StringMultiMap();
    private static final StringMultiMap limiti =  new StringMultiMap();

    //MINIMALNE I MAKSIMALNE VREDNOSTI ZA CONFIG
    private static final Limit SIRINA = new Limit(100, 3_000);
    private static final Limit VISINA = new Limit(50, 2_000);
    private static final Limit BR_KNJIGA = new Limit(1, 15);
    private static final Limit UC_KNJ_SIZE = new Limit(50, Integer.MAX_VALUE);
    private static final Limit DATE_LIMIT = new Limit(1, 365);
    private static final Limit SAVE_PERIOD = new Limit(0, Integer.MAX_VALUE);
    private static final Limit UNDO = new Limit(0, Integer.MAX_VALUE);
    private static final Limit LOG_SIZE = new Limit(0, 100_000_000);
    private static final Limit LOG_COUNT = new Limit(0, 1_000);
    

    /**
     * Ucitava config iz fajla u Properties.
     */
    public static void loadConfig() {
        setDefaults();
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
            resolveKeys();
        } catch (FileNotFoundException FNFex) {
            showMessageDialog(null, "Konfiguracijski fajl nije pronadjen. Lokacija: " + path);
        } catch (IOException ex) {
            showMessageDialog(null, "Došlo je do greške pri čitanju konfiguracijskog fajla"
                    + "ili postavljanju trenutnog direktorijuma", "I/O Greška", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Podesava hard-coded default vrednosti.
     */
    private static void setDefaults() {
        defaults.setProperty("knjSize", valueOf(getBrojKnjiga()));
        defaults.setProperty("ucSize", valueOf(getBrojUcenika()));
        defaults.setProperty("firstRun", "true");
        defaults.setProperty("dateLimit", "14");
        defaults.setProperty("lookAndFeel", "ocean");
        defaults.setProperty("brKnjiga", "3");
        defaults.setProperty("TFBoja", "false");
        defaults.setProperty("logLevel", "INFO");
        defaults.setProperty("savePeriod", "5");
        defaults.setProperty("maxUndo", "50");
        defaults.setProperty("logSizeLimit", "1000000");
        defaults.setProperty("logFileCount", "10");
    }

    /**
     * Podesava sinonime za kljuceve u configu.
     * @since 25.10.'14.
     */
    private static void defineSynonyms() {
        vrednosti.put("ucSize", "ucSize", "brojUcenika");
        vrednosti.put("knjSize", "knjSize", "brojKnjiga");
        vrednosti.put("firstRun", "firstRun", "prvoPokretanje");
        vrednosti.put("dateLimit", "dateLimit", "maxDana", "zadrzavanje", "zadrzavanjeKnjige",
                "Broj dana koji učenik sme da zadrži knjigu kod sebe");
        vrednosti.put("lookAndFeel", "lookAndFeel", "LaF", "LnF", "izgled", 
                "Izgled aplikacije (system, ocean, nimbus ili motif)");
        vrednosti.put("knjigeS", "knjigeS", "knjigeW", "knjigeSirina", "knjSirina", "sirinaKnjProzora");
        vrednosti.put("knjigeV", "knjigeV", "knjigeH", "knjigeVisina", "knjVisina", "visinaKnjProzora");
        vrednosti.put("uceniciS", "uceniciS", "uceniciW", "uceniciSirina", "ucSirina", "sirinaUcProzora");
        vrednosti.put("uceniciV", "uceniciV", "uceniciH", "uceniciVisina", "ucVisina", "visinaUcProzora");
        vrednosti.put("brKnjiga", "brKnjiga", "maxBrojKnjigaPoUceniku", "maxKnjiga", "maxUcenikKnjiga",
                "Najveći broj knjiga koji učenik može da ima kod sebe");
        vrednosti.put("bgBoja", "bgBoja", "bojaPozadine", "pozadinskaBoja", "bgColor");
        vrednosti.put("fgBoja", "fgBoja", "bojaTeksta", "fgColor");
        vrednosti.put("TFBoja", "TFBoja", "bojitiPoljaZaUnosTeksta");
        vrednosti.put("TFColor", "TFColor", "bojaPolja", "bojaPoljaZaUnosTeksta");
        vrednosti.put("logLevel", "logLevel", "nivoLogovanja", "Minimalni nivo logovanja akcija u aplikaciji");
        vrednosti.put("savePeriod", "savePeriod", "autosavePeriod", "saveInterval", "autosaveInterval", "intervalCuvanja",
                "Interval automatskog čuvanja podataka u minutima");
        vrednosti.put("maxUndo", "maxUndo", "undoStackDepth", "velicinaUndoStacka",
                "Broj akcija koje se čuvaju za undo");
        vrednosti.put("razredi", "razredi", "razrediUcenika", "validniRazredi",
                "Mogući razredi učenika (razdvojeni zapetom)");
        vrednosti.put("workingDir", "workingDir", "workingDirectory", "Radni direktorijum", "dataDir",
                "Folder u kojem se čuvaju podaci");
        vrednosti.put("logSizeLimit", "logSizeLimit", "logSize", "logLimit", "logFileSizeLimit", "velicinaLogFajla", 
                "Maksimalna veličina log fajla u bajtovima");
        vrednosti.put("logFileCount", "logFileCount", "logCount", "logFileNumber", "brojLogFajlova", 
                "Maksimalan broj log fajlova");
    }
    
    /**
     * Postavlja limite u mapi koristeći vrednosti iz fieldova.
     * @since 11.'14
     */
    private static void setLimits() {
        limiti.put("ucSize", UC_KNJ_SIZE.MIN, UC_KNJ_SIZE.MAX);
        limiti.put("knjSize", UC_KNJ_SIZE.MIN, UC_KNJ_SIZE.MAX);
        limiti.put("dateLimit", DATE_LIMIT.MIN, DATE_LIMIT.MAX);
        limiti.put("knjigeS", SIRINA.MIN, SIRINA.MAX);
        limiti.put("knjigeV", VISINA.MIN, VISINA.MAX);
        limiti.put("uceniciS", SIRINA.MIN, SIRINA.MAX);
        limiti.put("uceniciV", VISINA.MIN, VISINA.MAX);
        limiti.put("brKnjiga", BR_KNJIGA.MIN, BR_KNJIGA.MAX);
        limiti.put("savePeriod", SAVE_PERIOD.MIN, SAVE_PERIOD.MAX);
        limiti.put("maxUndo", UNDO.MIN, UNDO.MAX);
        limiti.put("logSizeLimit", LOG_SIZE.MIN, LOG_SIZE.MAX);
        limiti.put("logFileCount", LOG_COUNT.MIN, LOG_COUNT.MAX);
    }
    
    /**
     * Radi iteraciju preko configa i zamenjuje ključeve ako su sinonimi sa glavnim (iz mape vrednosti).
     * Postavlja grafičke konstante (o istom trošku).
     * @since 7.11.'14.
     */
    private static void resolveKeys() {
        Entry e;
        for (Iterator<Entry<Object, Object>> it = config.entrySet().iterator(); it.hasNext();) {
            e = it.next();
            if(e.getKey().toString().startsWith("k_")) 
                set(e.getKey().toString(), e.getValue().toString());
            else if(!vrednosti.containsKey(e.getKey())) {
                config.put(vrednosti.getKey((String) e.getKey()), e.getValue());
                it.remove();
            }
        }
    }

    /**
     * Cuva config u fajl.
     */
    private static void storeConfig() {
        try {
            config.store(new FileWriter(configFile), configMsg);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "I/O greška pri čuvanju konfiguracijskog fajla", ex);
            showMessageDialog(null, "Greška pri čuvanju konfiguracijskog fajla",
                    "I/O greška", JOptionPane.ERROR_MESSAGE);
            JOptionPane.showMessageDialog(null, "Greška pri čuvanji konfiguracijskog fajla.\n"
                    + "Najnovije promene podešavanja nisu sačuvane.", "I/O greška", JOptionPane.ERROR_MESSAGE);
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
        return config.containsKey(vrednosti.getKey(key));
    }

    /**
     * Vraca vrednost koja je povezana sa ovim kljucem ili sinonimom. Ako ne
     * postoji, vraca default vrednost. Ako ni default ne postoji, vraca null.
     *
     * @param key kljuc ili sinonim
     * @return String vrednost
     */
    public static String get(String key) {
        if(key==null) return null;
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
        if(val==null) return false;
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
        if(key.startsWith("k_")) {
            Konstante.set(key.substring(2), val);
            return;
        }
        
        String realKey = vrednosti.getKey(key);
        check(realKey, val);
        
        if(limiti.containsKey(key)) {
            ArrayList<String> lims = limiti.get(key);
            config.setProperty(realKey, 
                    Utils.limitedInteger(val, Integer.parseInt(lims.get(0)), Integer.parseInt(lims.get(1))));
        }
        else {
            config.setProperty(realKey, val);
        }
        LOGGER.log(Level.CONFIG, "{0} podešen na {1}", new String[]{key, val});
        storeConfig();
    }

    /**
     * Proverava da li je data vrednost dozvoljena za dati kljuc. Razredi moraju da
     * budu validni integeri razdvojeni zapetama, logLevel integer ili validan
     * string, lookAndFeel jedan on system, crossOcean ili crossMetal, firstRun
     * i TFBoja 0 ili 1 ili true ili false, sve ostale vrednosti integeri.
     *
     * @param key kljuc u configu
     * @param val vrednost u configu.
     * @return true ako sme da postoji, false u suprotnom.
     * @since 24.10.'14.
     */
    private static boolean isNameValid(String key, String val) {
        if(key.startsWith("k_"))
            return true;
        if (!vrednosti.contains(key)) {
            System.out.println(key + " ne postoji");
            return false;
        }
        val = val.toLowerCase();
        if ("razredi".equalsIgnoreCase(vrednosti.getKey(key))) {
            String[] razredi = val.split(",");
            for (String razred : razredi) {
                razred = razred.trim();
                if (!Utils.isInteger(razred) && !Ucenik.isRazredValid(Integer.parseInt(razred))) {
                    return false;
                }
            }
            return true;
        }
        if ("logLevel".equalsIgnoreCase(vrednosti.getKey(key))) {
            try {
                Level.parse(val.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return false;
            }
            return true;
        }
        if ("lookAndFeel".equalsIgnoreCase(vrednosti.getKey(key))) {
            return val.equals("system") || val.equals("ocean") || val.equals("metal") || 
                    val.equals("Nimbus") || val.equals("motif");
        }
        if ("firstRun".equalsIgnoreCase(vrednosti.getKey(key)) || "TFBoja".equals(vrednosti.getKey(key))) {
            return val.equals("0") || val.equals("1") || val.equals("true") || val.equals("false");
        }
        if ("bgBoja".equalsIgnoreCase(vrednosti.getKey(key)) || "fgBoja".equals(vrednosti.getKey(key))
                || "TFColor".equalsIgnoreCase(vrednosti.getKey(key))) {
            try {
                Color.decode(val);
            } catch (NumberFormatException ex) {
                return false;
            }
            return true;
        } else {
            return Utils.isInteger(val);
        }
    }
    
    private static void check(String key, String val) {
        Iterator<Ucenik> iterator;
        switch(key) {
            case "brKnjiga": int valInt = Integer.parseInt(val);
                iterator = Podaci.iteratorUcenika();
                iterator.forEachRemaining((Ucenik uc) -> {
                    if(uc.getBrojKnjiga() > valInt)
                        throw new ConfigException("brKnjiga");
                });
            break;
            case "razredi": String[] valsStr = val.split(","); int[] vals = new int[valsStr.length]; 
                for(int i=0; i<vals.length; i++) {
                    vals[i] = Integer.parseInt(valsStr[i]);
                }
                iterator = Podaci.iteratorUcenika();
                iterator.forEachRemaining((Ucenik uc) -> {
                    if(!Utils.arrayContains(vals, uc.getRazred()))
                        throw new ConfigException("razredi");
                });
            break;
            case "workingDir": File folder = new File(val);
                if(!folder.isDirectory() && !folder.mkdir())
                    throw new ConfigException("workingDir");
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
     * Vraca nazive svih kljuceva u listi, ako imaju puno ime (vise reci sa razmakom).
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
}
