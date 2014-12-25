package rs.luka.biblioteka.grafika;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import static java.lang.Integer.parseInt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import rs.luka.biblioteka.data.Config;
import rs.luka.biblioteka.data.Knjiga;
import rs.luka.biblioteka.data.Podaci;
import rs.luka.biblioteka.exceptions.Duplikat;
import rs.luka.biblioteka.exceptions.NemaViseKnjiga;
import rs.luka.biblioteka.exceptions.PreviseKnjiga;
import rs.luka.biblioteka.exceptions.VrednostNePostoji;
import static rs.luka.biblioteka.grafika.Konstante.*;

/**
 * @author Luka
 */
public class Knjige implements FocusListener {

    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(Knjige.class.getName());
    private static final JFrame win = new JFrame("Pregled knjiga");

    private final Insets INSET = new Insets
        (KNJIGE_TOP_INSET, KNJIGE_LEFT_INSET, KNJIGE_BOTTOM_INSET, KNJIGE_RIGHT_INSET);
    
    /**
     * searchBox za pretrazivanje knjiga.
     */
    private final JTextField searchBox = new JTextField(KNJIGE_SEARCH_TEXT);

    private int sirina, visina;
    
    private final LinkedList<UzmiVratiButton> buttons;
    private final JLabel[] pisac;
    private final JLabel[] kolicina;
    private final JCheckBox[] knjige;
    private final JLabel kolicinaTitle;
    private final JLabel pisacTitle;
    private final JCheckBox selectAll;
    private final JSplitPane split;
    private final JPanel butPan;
    private final JScrollPane scroll;
    private final JPanel sidePan;
    private final JPanel kolPan;
    private final JPanel pisacPan;
    private final JPanel knjPan;
    private final JPanel mainPan;

    /**
     * Konstruktuje komponente prozora i zove {@link #pregledKnjiga()}
     */
    public Knjige() {
        buttons = new LinkedList<>();
        pisac = new JLabel[Podaci.getBrojKnjiga()];
        kolicina = new JLabel[Podaci.getBrojKnjiga()];
        knjige = new JCheckBox[Podaci.getBrojKnjiga()];
        kolicinaTitle = new JLabel("Količina:");
        pisacTitle = new JLabel("Pisac:");
        selectAll = new JCheckBox("Naslovi:");
        butPan = new JPanel();
        sidePan = new JPanel(null);
        kolPan = new JPanel(null);
        pisacPan = new JPanel(null);
        knjPan = new JPanel(null);
        mainPan = new JPanel(null);
        scroll = new JScrollPane(mainPan);
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, butPan);
    }

    /**
     * Pregled knjiga koje su trenutno u biblioteci. Zove init* metoda i postavlja {@link #win} na visible.
     */
    public void pregledKnjiga() {
        initPanels();
        initText();
        initButtons();
        initOtherListeners();
        initSearchBox();
        
        win.setVisible(true);
    }
    
    /**
     * Inicalizuje prozor i panele. Postavlja lokacije, velicine, layout-e, scrollDistance i sl.
     */
    private void initPanels() {
        win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        sirina = parseInt(Config.get("knjigeS", String.valueOf(KNJIGE_SIRINA)));
        visina = parseInt(Config.get("knjigeV", String.valueOf(KNJIGE_VISINA)));
        win.setSize(sirina, visina);
        LOGGER.log(Level.CONFIG, "knjigeS: {0}, knjigeV: {1}", new Object[]{sirina, visina});
        win.setLocationRelativeTo(null);
        mainPan.setLayout(new BoxLayout(mainPan, BoxLayout.X_AXIS));
        mainPan.setBackground(Grafika.getBgColor());
        mainPan.setAutoscrolls(true);
        knjPan.setLayout(new BoxLayout(knjPan, BoxLayout.Y_AXIS));
        knjPan.setBackground(Grafika.getBgColor());
        knjPan.setAlignmentY(KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(knjPan);
        pisacPan.setLayout(new BoxLayout(pisacPan, BoxLayout.Y_AXIS));
        pisacPan.setBackground(Grafika.getBgColor());
        pisacPan.setAlignmentY(KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(pisacPan);
        kolPan.setLayout(new BoxLayout(kolPan, BoxLayout.Y_AXIS));
        kolPan.setBackground(Grafika.getBgColor());
        kolPan.setAlignmentY(0);
        mainPan.add(kolPan);
        sidePan.setBackground(Grafika.getBgColor());
        sidePan.setPreferredSize(new Dimension(KNJIGE_SIDEPAN_WIDTH, 
                (Podaci.getBrojKnjiga() + 1) * KNJIGE_SIDEPAN_UCENIK_HEIGHT));
        sidePan.setAlignmentY(KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(sidePan);
        scroll.add(scroll.createVerticalScrollBar());
        scroll.getVerticalScrollBar().setUnitIncrement(KNJIGE_SCROLL_INCREMENT);
        butPan.setBackground(Grafika.getBgColor());
        butPan.setLayout(new FlowLayout(FlowLayout.CENTER));
        split.setOneTouchExpandable(false);
        split.setDividerLocation(visina - KNJIGE_DIVIDER_LOCATION);
        win.setContentPane(split);
    }

    /**
     * Postavlja JLabel-e i JCheckBox-ove. Postavlja odgovarajuci tekst i boju i stavlja ih na panele.
     */
    private void initText() {
        selectAll.setFont(Grafika.getLabelFont());
        selectAll.setForeground(Grafika.getFgColor());
        selectAll.setBackground(Grafika.getBgColor());
        selectAll.setBorder(new EmptyBorder(INSET));
        knjPan.add(selectAll);
        pisacTitle.setFont(Grafika.getLabelFont());
        pisacTitle.setForeground(Grafika.getFgColor()); //bgColor nije neophodan za labele
        pisacTitle.setBorder(new EmptyBorder(INSET));
        pisacPan.add(pisacTitle);
        kolicinaTitle.setFont(Grafika.getLabelFont());
        kolicinaTitle.setForeground(Grafika.getFgColor());
        kolicinaTitle.setBorder(new EmptyBorder(INSET));
        kolPan.add(kolicinaTitle);
        //----------------------------------------------------------------------
        Iterator<Knjiga> it = Podaci.iteratorKnjiga();
        Knjiga knj;
        for (int i = 0; i < knjige.length; i++) {
            knj = it.next();
            knjige[i] = new JCheckBox();
            knjige[i].setText(knj.getNaslov());
            knjige[i].setBorder(new EmptyBorder(INSET));
            knjige[i].setMinimumSize(new Dimension(300, 30));
            knjige[i].setFont(Grafika.getLabelFont());
            knjige[i].setForeground(Grafika.getFgColor());
            knjige[i].setBackground(Grafika.getBgColor());
            knjPan.add(knjige[i]);

            pisac[i] = new JLabel(knj.getPisac());
            pisac[i].setBorder(new EmptyBorder(INSET));
            pisac[i].setFont(Grafika.getLabelFont());
            pisac[i].setForeground(Grafika.getFgColor());
            pisac[i].setBackground(Grafika.getBgColor());
            pisacPan.add(pisac[i]);

            kolicina[i] = new JLabel(String.valueOf(knj.getKolicina()));
            kolicina[i].setBorder(new EmptyBorder(INSET));
            kolicina[i].setFont(Grafika.getLabelFont());
            kolicina[i].setForeground(Grafika.getFgColor());
            kolPan.add(kolicina[i]);
        }
    }

    /**
     * Inicijalizuje dugmad koja se nalazi pri dnu i dodaje ih u {@link #butPan}.
     */
    private void initButtons() {
        JButton novi = new JButton("Ubaci novi naslov");
        novi.setFont(Grafika.getButtonFont());
        novi.setPreferredSize(new Dimension(KNJIGE_NOVI_WIDTH, KNJIGE_BUTTON_HEIGHT));
        novi.addActionListener((ActionEvent e) -> {
            new KnjigeUtils().novi();
        });
        butPan.add(novi);
        JButton obrisi = new JButton("Obriši naslov");
        obrisi.setFont(Grafika.getButtonFont());
        obrisi.setPreferredSize(new Dimension(KNJIGE_OBRISI_WIDTH, KNJIGE_BUTTON_HEIGHT));
        obrisi.addActionListener((ActionEvent e) -> {
            obrisiNaslov();
        });
        butPan.add(obrisi);
        JButton ucSearch = new JButton("Kod koga je naslov...");
        ucSearch.setFont(Grafika.getButtonFont());
        ucSearch.setPreferredSize(new Dimension(KNJIGE_UCSEARCH_WIDTH, KNJIGE_BUTTON_HEIGHT));
        ucSearch.addActionListener((ActionEvent e) -> {
            new KnjigeUtils().ucSearch(getFirstSelected(), visina);
        });
        butPan.add(ucSearch);
    }

    /**
     * Inicalizuje listenere za selectAll i uzmiKnjigu.
     */
    private void initOtherListeners() {
        selectAll.addItemListener((ItemEvent e) -> {
            selectAll();
        });
        for (int i = 0; i < Podaci.getBrojKnjiga(); i++) {
            final int red = i;
            knjige[i].addItemListener((ItemEvent ie) -> {
                uzmiKnjigu(red);
            });
        }
    }

    /**
     * Inicijalizuje searchBox, postavlja tekst, boje i stavlja na vrh u sidePan-u.
     */
    private void initSearchBox() {
        searchBox.addFocusListener(this);
        searchBox.addActionListener((ActionEvent e) -> {
            search();
        });
        searchBox.setFont(Grafika.getLabelFont());
        searchBox.setBackground(Grafika.getTFColor());
        searchBox.setForeground(Grafika.getFgColor());
        searchBox.setCaretColor(Grafika.getFgColor());
        searchBox.setBounds(KNJIGE_SEARCHBOX_X, KNJIGE_SEARCHBOX_Y, KNJIGE_SEARCHBOX_WIDTH, KNJIGE_SEARCHBOX_HEIGHT);
        sidePan.add(searchBox);
    }

    /**
     * Metoda za listener.
     * Brise odabran naslov. Ako nijedan nije selektovan, prikazuje Dijalog za unos naslova koji treba obrisati.
     */
    private void obrisiNaslov() {
        boolean selected = false;
        for (int i = 0, realI = 0; i < knjige.length; i++, realI++) {
            if (knjige[i].isSelected()) {
                selected = true;
                try {
                    Podaci.obrisiKnjigu(realI);
                    realI--;
                } catch (PreviseKnjiga ex) {
                    LOGGER.log(Level.INFO, "Knjiga zauzeta. Brisanja naslova nije obavljeno");
                    JOptionPane.showMessageDialog(null, "Kod nekog učenika " + " se nalazi knjiga " 
                            + knjige[i].getText()+ ".\nKada vrati knjigu, pokušajte ponovo.",
                            "Zauzeta knjiga", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (!selected) {
            String naslov = Dijalozi.showTextFieldDialog("Brisanje naslova",
                    "Unesite naslov koji želite da obrišete i pritisnite enter:", "");
            try {
                Podaci.obrisiKnjigu(Podaci.indexOfNaslov(naslov));
            } catch (VrednostNePostoji ex) {
                LOGGER.log(Level.INFO, "Unet naslov {0} ne postoji", naslov);
                JOptionPane.showMessageDialog(null, "Naslov koji ste uneli ne postoji.\n"
                        + "Proverite unos i pokušajte ponovo", "Greška pri brisanju naslova",
                        JOptionPane.ERROR_MESSAGE);
            } catch (PreviseKnjiga ex) {
                LOGGER.log(Level.INFO, "Knjiga zauzeta. Brisanja naslova nije obavljeno");
                JOptionPane.showMessageDialog(null, "Zauzeta knjiga", "Kod nekog ucenika "
                        + " se nalazi ova knjiga\n"
                        + "Kada vrati knjigu, pokusajte ponovo.", JOptionPane.ERROR_MESSAGE);
            }
        }
        win.dispose();
        new Knjige().pregledKnjiga();
    }

    /**
     * Postavlja sve knjige koje su enabled i visible na checked (odabrane).
     */
    private void selectAll() {
        for (JCheckBox knjiga : knjige) {
            knjiga.setSelected(knjiga.isVisible() && selectAll.isSelected() && knjiga.isEnabled());
        }
    }

    /**
     * Uzima datu knjigu od ucenika i vraca biblioteci. Ucenik se unosi preko dijaloga.
     * @param red red u kome se nalazi knjiga za iznajmljivanje
     */
    private void uzmiKnjigu(int red) {
        if (knjige[red].isSelected()) {
            LOGGER.log(Level.FINER, "Prikazujem dugme za uzimanje br {0}", red);
            UzmiVratiButton button = new UzmiVratiButton(red, INVALID, 
                    knjige[red].getLocationOnScreen().y - sidePan.getLocationOnScreen().y);
            button.addActionListener((ActionEvent ae) -> {
                String ucenik = Dijalozi.showTextFieldDialog("Iznajmljivanje knjige",
                        "Unesite ime učenika koji iznajmljuje knjigu i pritisnite enter:", "");
                try {
                    Podaci.uzmiKnjigu(red, ucenik);
                    JOptionPane.showMessageDialog(null, "Učenik je uspešno iznajmio knjigu", 
                            "Uspeh!", JOptionPane.INFORMATION_MESSAGE);
                    new Ucenici().pregledUcenika();
                    new Knjige().pregledKnjiga();
                } catch (PreviseKnjiga ex) {
                    LOGGER.log(Level.INFO, "Kod učenika {0} se "
                            + "trenutno nalazi previše knjiga", ucenik);
                    JOptionPane.showMessageDialog(null, "Kod učenika se "
                            + "trenutno nalazi previše knjiga",
                            "Greška pri iznajmljivanju", JOptionPane.ERROR_MESSAGE);
                } catch (Duplikat ex) {
                    LOGGER.log(Level.INFO, "Kod učenika {0} se već nalazi "
                            + "knjiga naslova {1}", new Object[]{ucenik, Podaci.getKnjiga(red).getNaslov()});
                    JOptionPane.showMessageDialog(null, "Kod učenika se "
                            + "već nalazi knjiga tog naslova",
                            "Greška pri iznajmljivanju", JOptionPane.ERROR_MESSAGE);
                } catch (NemaViseKnjiga ex) {
                    LOGGER.log(Level.INFO, "Nema više knjiga naslova {0} "
                            + "u biblioteci", Podaci.getKnjiga(red).getNaslov());
                    JOptionPane.showMessageDialog(null, "Nema više knjiga"
                            + " tog naslova", "Greška pri iznajmljivanju",
                            JOptionPane.ERROR_MESSAGE);
                } catch (VrednostNePostoji ex) {
                    LOGGER.log(Level.INFO, "Učenik {0} nije pronađen", ucenik);
                    JOptionPane.showMessageDialog(null, "Učenik nije pronađen.\n"
                            + "Proverite unos i pokušajte ponovo",
                            "Greška pri iznajmljivanju", JOptionPane.ERROR_MESSAGE);
                }
            });
            button.uzmi();
            sidePan.add(button);
            buttons.add(button);
            sidePan.repaint();
            LOGGER.log(Level.FINE, "Dugme za uzimanje br. {0} prikazano.", red);
        } else {
            int delIndex = buttons.indexOf(new UzmiVratiButton(red, INVALID, INVALID));
            sidePan.remove(delIndex + 1);
            buttons.remove(delIndex);
            sidePan.repaint();
            LOGGER.log(Level.FINE, "Dugme za uzimanje br. {0} obrisano", red);
        }
    }

    /**
     * Pretrazuje knjige i prikazuje nadjene.
     */
    private void search() {
        rs.luka.legacy.biblioteka.Knjige funkcije = new rs.luka.legacy.biblioteka.Knjige();
        ArrayList<Integer> nasIndexes = funkcije.pretraziKnjige(searchBox.getText());
        for (int i = 0; i < Podaci.getBrojKnjiga(); i++) {
            if (nasIndexes.contains(i)) {
                Knjiga knjiga = Podaci.getKnjiga(i);
                knjige[i].setText(knjiga.getNaslov());
                knjige[i].setVisible(true);
                kolicina[i].setText(String.valueOf(knjiga.getKolicina()));
                kolicina[i].setVisible(true);
                pisac[i].setText(knjiga.getPisac());
                pisac[i].setVisible(true);
            } else {
                knjige[i].setSelected(false);
                knjige[i].setVisible(false);
                kolicina[i].setVisible(false);
                pisac[i].setVisible(false);
            }
        }
        
        sidePan.setMaximumSize(new Dimension(140, (nasIndexes.size() +1) * selectAll.getHeight()));
        LOGGER.log(Level.FINE, "Pretraga obavljena (grafički)");
    }
    
    /**
     * Vraca prvi checkbox koji je selektovan.
     * @return tekst prvog checkbox-a
     */
    private String getFirstSelected() {
        for(JCheckBox knjiga : knjige) {
            if(knjiga.isSelected() && knjiga.isVisible()) {
                return knjiga.getText();
            }
        }
        return null;
    }
    
    /**
     * Radi refresh prozora (zove {@link #pregledKnjiga()}) ako je on prikazan.
     * U suprotnom, ignorise poziv.
     */
    public static void refresh() {
        if(win.isVisible())
            new Knjige().pregledKnjiga();
    }

    
    @Override
    public void focusGained(FocusEvent e) {
        if (searchBox.getText().equals(KNJIGE_SEARCH_TEXT)) {
            searchBox.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(searchBox.getText().isEmpty()) {
            searchBox.setText(KNJIGE_SEARCH_TEXT);
        }
    }
}
