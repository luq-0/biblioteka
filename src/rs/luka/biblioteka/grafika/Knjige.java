package rs.luka.biblioteka.grafika;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import rs.luka.biblioteka.exceptions.PreviseKnjiga;
import rs.luka.biblioteka.exceptions.VrednostNePostoji;
import rs.luka.biblioteka.funkcije.Init;
import rs.luka.biblioteka.funkcije.Pretraga;

/**
 * @author Luka
 */
public class Knjige implements FocusListener {

    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(Knjige.class.getName());
    private static final JFrame win = new JFrame();
    private final int maxKnjiga;

    private final Insets INSET = new Insets(Init.dData.CHECKBOX_TOP_INSET, Init.dData.CHECKBOX_LEFT_INSET, 
                                            Init.dData.CHECKBOX_BOTTOM_INSET, Init.dData.CHECKBOX_RIGHT_INSET);
    
    /**
     * searchBox za pretrazivanje knjiga.
     */
    private final JTextField searchBox = new JTextField(Init.dData.KNJIGE_SEARCH_STRING);
    
    private final LinkedList<SmallButton> buttons;
    private final JLabel[] pisac;
    private final JLabel[] kolicina;
    private final List<IndexedCheckbox> knjige;
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
        this.maxKnjiga = Podaci.getBrojKnjiga();
        win.setTitle(Init.dData.KNJIGE_TITLE_STRING);
        buttons = new LinkedList<>();
        pisac = new JLabel[maxKnjiga];
        kolicina = new JLabel[maxKnjiga];
        knjige = new ArrayList<>(maxKnjiga);
        kolicinaTitle = new JLabel(Init.dData.KNJIGE_KOLICINA_STRING);
        pisacTitle = new JLabel(Init.dData.KNJIGE_PISAC_STRING);
        selectAll = new JCheckBox(Init.dData.KNJIGE_NASLOVI_STRING);
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
        showWindow();
    }
    
    /**
     * Inicalizuje prozor i panele. Postavlja lokacije, velicine, layout-e, scrollDistance i sl.
     */
    private void initPanels() {
        win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //win.setSize(KNJIGE_SIRINA, KNJIGE_VISINA);
        LOGGER.log(Level.CONFIG, "knjigeS: {0}, knjigeV: {1}", new Object[]{Init.dData.KNJIGE_SIRINA, Init.dData.KNJIGE_VISINA});
        //win.setLocationRelativeTo(null);
        mainPan.setLayout(new BoxLayout(mainPan, BoxLayout.X_AXIS));
        mainPan.setBackground(Grafika.getBgColor());
        mainPan.setAutoscrolls(true);
        knjPan.setLayout(new BoxLayout(knjPan, BoxLayout.Y_AXIS));
        knjPan.setBackground(Grafika.getBgColor());
        knjPan.setAlignmentY(Init.dData.KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(knjPan);
        pisacPan.setLayout(new BoxLayout(pisacPan, BoxLayout.Y_AXIS));
        pisacPan.setBackground(Grafika.getBgColor());
        pisacPan.setAlignmentY(Init.dData.KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(pisacPan);
        kolPan.setLayout(new BoxLayout(kolPan, BoxLayout.Y_AXIS));
        kolPan.setBackground(Grafika.getBgColor());
        kolPan.setAlignmentY(0);
        mainPan.add(kolPan);
        sidePan.setBackground(Grafika.getBgColor());
        sidePan.setPreferredSize(new Dimension(Init.dData.KNJIGE_SIDEPAN_WIDTH, 
                (maxKnjiga + 1) * Init.dData.KNJIGE_SIDEPAN_UCENIK_HEIGHT));
        sidePan.setAlignmentY(Init.dData.KNJIGE_PANELS_ALIGNMENT_Y);
        mainPan.add(sidePan);
        scroll.add(scroll.createVerticalScrollBar());
        scroll.getVerticalScrollBar().setUnitIncrement(Init.dData.KNJIGE_SCROLL_INCREMENT);
        butPan.setBackground(Grafika.getBgColor());
        butPan.setLayout(new FlowLayout(FlowLayout.CENTER));
        split.setOneTouchExpandable(false);
        split.setResizeWeight(0.99);
        //split.setDividerLocation(KNJIGE_VISINA - KNJIGE_DIVIDER_LOCATION);
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
        IndexedCheckbox box;
        Knjiga knj;
        for (int i = 0; i < kolicina.length; i++) {
            knj = it.next();
            box = new IndexedCheckbox(knj.getNaslov(), i, Init.dData.INVALID);
            //box.setMinimumSize(new Dimension(300, 30));
            box.setFont(Grafika.getLabelFont());
            box.setForeground(Grafika.getFgColor());
            box.setBackground(Grafika.getBgColor());
            box.setBorder(new EmptyBorder(INSET));
            knjPan.add(box);
            knjige.add(box);

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
        JButton novi = new JButton(Init.dData.KNJIGE_NOVI_STRING);
        novi.setFont(Grafika.getButtonFont());
        novi.setPreferredSize(new Dimension(Init.dData.KNJIGE_NOVI_WIDTH, Init.dData.KNJIGE_BUTTON_HEIGHT));
        novi.addActionListener((ActionEvent e) -> {
            new KnjigeUtils().novi();
        });
        butPan.add(novi);
        JButton obrisi = new JButton(Init.dData.KNJIGE_OBRISI_STRING);
        obrisi.setFont(Grafika.getButtonFont());
        obrisi.setPreferredSize(new Dimension(Init.dData.KNJIGE_OBRISI_WIDTH, Init.dData.KNJIGE_BUTTON_HEIGHT));
        obrisi.addActionListener((ActionEvent e) -> {
            obrisiNaslov();
        });
        butPan.add(obrisi);
        JButton ucSearch = new JButton(Init.dData.KNJIGE_UCSEARCH_STRING);
        ucSearch.setFont(Grafika.getButtonFont());
        ucSearch.setPreferredSize(new Dimension(Init.dData.KNJIGE_UCSEARCH_WIDTH, Init.dData.KNJIGE_BUTTON_HEIGHT));
        ucSearch.addActionListener((ActionEvent e) -> {
            new KnjigeUtils().ucSearch(getFirstSelected(), Init.dData.KNJIGE_VISINA);
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
        ItemListener listener = (ItemEvent e) -> {
            setKolicina((IndexedCheckbox)e.getItem());
        };
        knjige.forEach((IndexedCheckbox box) -> {
            box.addItemListener(listener);
        });
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
        searchBox.setBounds(Init.dData.KNJIGE_SEARCHBOX_X, Init.dData.KNJIGE_SEARCHBOX_Y, 
                Init.dData.KNJIGE_SEARCHBOX_WIDTH, Init.dData.KNJIGE_SEARCHBOX_HEIGHT);
        sidePan.add(searchBox);
    }
    
    /**
     * Postavlja velicinu i poziciju i prikazuje prozor
     */
    private void showWindow() {
        if(Config.getAsBool("customSize"))
            win.setSize(Init.dData.KNJIGE_SIRINA, Init.dData.KNJIGE_VISINA);
        else {
            win.pack();
            win.setSize(win.getWidth(), GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize().height);
        }
        win.setLocationRelativeTo(null);
        win.setVisible(true);
    }

    /**
     * Metoda za listener.
     * Brise odabran naslov. Ako nijedan nije selektovan, prikazuje Dijalog za unos naslova koji treba obrisati.
     */
    private void obrisiNaslov() {
        boolean selected = false;
        for (int i = 0, realI = 0; i < knjige.size(); i++, realI++) {
            if (knjige.get(i).isSelected()) {
                selected = true;
                try {
                    Podaci.obrisiKnjigu(realI);
                    realI--;
                } catch (PreviseKnjiga ex) {
                    LOGGER.log(Level.INFO, "Knjiga zauzeta. Brisanja naslova nije obavljeno");
                    JOptionPane.showMessageDialog(null, Init.dData.KNJIGE_PKEX_MSG1_STRING
                            + knjige.get(i).getText()+ Init.dData.KNJIGE_PKEX_MSG2_STRING,
                            Init.dData.KNJIGE_PKEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (!selected) {
            String naslov = Dijalozi.showTextFieldDialog(Init.dData.KNJIGE_BRISANJE_DIJALOG_TITLE_STRING,
                    Init.dData.KNJIGE_BRISANJE_DIJALOG_MSG_STRING, "");
            try {
                Podaci.obrisiKnjigu(Podaci.getKnjiga(naslov));
            } catch (VrednostNePostoji ex) {
                LOGGER.log(Level.INFO, "Unet naslov {0} ne postoji", naslov);
                JOptionPane.showMessageDialog(null, Init.dData.KNJIGE_BRISANJE_VNPEX_MSG_STRING, 
                        Init.dData.KNJIGE_BRISANJE_VNPEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
            } catch (PreviseKnjiga ex) {
                LOGGER.log(Level.INFO, "Knjiga zauzeta. Brisanja naslova nije obavljeno");
                JOptionPane.showMessageDialog(null, Init.dData.KNJIGE_BRISANJE_PKEX_TITLE_STRING, 
                        Init.dData.KNJIGE_BRISANJE_PKEX_MSG_STRING, JOptionPane.ERROR_MESSAGE);
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
    
    private void setKolicina(IndexedCheckbox box) {
        if(box.isSelected()) {
            SmallButton but = new SmallButton(box.getIndex(), Init.dData.INVALID, 
                    box.getLocationOnScreen().y - sidePan.getLocationOnScreen().y + Init.dData.CHECKBOX_TOP_INSET);
            but.setKol();
            buttons.add(but);
            sidePan.add(but);
        }
        else {
            int index = buttons.indexOf(new SmallButton(box.getIndex(), Init.dData.INVALID, Init.dData.INVALID));
            if(index<0)
                throw new IndexOutOfBoundsException("indexOf vratio nepostojeci index");
            buttons.remove(index);
            sidePan.remove(index + 1); //+1 zato sto je na nultom mestu searchBox
        }
        sidePan.repaint();
    }

    /**
     * Pretrazuje knjige i prikazuje nadjene.
     */
    private void search() {
        ArrayList<Integer> nasIndexes = Pretraga.pretraziKnjige(searchBox.getText());
        for (int i = 0; i < maxKnjiga; i++) {
            if (nasIndexes.contains(i)) {
                Knjiga knjiga = Podaci.getKnjiga(i);
                knjige.get(i).setText(knjiga.getNaslov());
                knjige.get(i).setVisible(true);
                kolicina[i].setText(String.valueOf(knjiga.getKolicina()));
                kolicina[i].setVisible(true);
                pisac[i].setText(knjiga.getPisac());
                pisac[i].setVisible(true);
            } else {
                knjige.get(i).setSelected(false);
                knjige.get(i).setVisible(false);
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
        if(win.isVisible()) {
            Runnable pregled = () -> {new Knjige().pregledKnjiga();};
            if(EventQueue.isDispatchThread()) 
                pregled.run();
            else EventQueue.invokeLater(pregled);
        }
    }
    
    @Override
    public void focusGained(FocusEvent e) {
        if (searchBox.getText().equals(Init.dData.KNJIGE_SEARCH_STRING)) {
            searchBox.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(searchBox.getText().isEmpty()) {
            searchBox.setText(Init.dData.KNJIGE_SEARCH_STRING);
        }
    }
}
