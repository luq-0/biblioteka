package rs.luka.biblioteka.grafika;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import rs.luka.biblioteka.data.Config;
import rs.luka.biblioteka.data.Podaci;
import static rs.luka.biblioteka.data.Podaci.getMaxBrojUcenikKnjiga;
import static rs.luka.biblioteka.data.Podaci.getUcenik;
import rs.luka.biblioteka.data.Ucenik;
import rs.luka.biblioteka.debugging.Console;
import rs.luka.biblioteka.exceptions.PreviseKnjiga;
import rs.luka.biblioteka.funkcije.Init;
import rs.luka.biblioteka.funkcije.Pretraga;
import rs.luka.biblioteka.funkcije.Save;
import rs.luka.biblioteka.funkcije.Undo;
import rs.luka.biblioteka.funkcije.Utils;

/**
 * @since 1.7.'13.
 * @author Luka
 */
public class Ucenici implements FocusListener {

    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(Ucenici.class.getName());
    private static final int maxKnjiga = getMaxBrojUcenikKnjiga();
    private final int maxUcenika;
    private static final JFrame win = new JFrame();
    private static final int MAX_HEIGHT = 
            GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize().height;

    /**
     * Dispose-uje {@link #win}
     */
    protected static void close() {
        if(EventQueue.isDispatchThread())
            win.dispose();
        else EventQueue.invokeLater(() -> (win.dispose()));
    }

    /**
     * Matrix sa knjigama pocinje od [][1], na 0 su labeli "Knjiga".
     */
    private IndexedCheckbox[][] knjige;
    private IndexedCheckbox[] ucenici;
    private final Insets INSET = new Insets(Init.dData.CHECKBOX_TOP_INSET, Init.dData.CHECKBOX_LEFT_INSET,
            Init.dData.CHECKBOX_BOTTOM_INSET, Init.dData.CHECKBOX_RIGHT_INSET); //ne sme static, da se ne bi prerano inicijalizovala
    private final JSplitPane split;
    private final JPanel butPan;
    private final JSeparator[][] knjSeparatori;
    private final JSeparator[] ucSeparatori;
    private final JPanel[] knjigePan;
    private final JPanel uceniciPan;
    private final JScrollPane scroll;
    private final JPanel pan;
    private final JTextField searchBox;
    private final JCheckBox selectAllUc;
    //private final JButton[] vratiBut;
    //private final JButton[] uzmiBut;
    private final LinkedList<SmallButton> buttons;
    private final JPanel sidePan;

    /**
     * Konstruktuje sve komponenta prozora i zove {@link #pregledUcenika()}
     */
    public Ucenici() {
        this.maxUcenika = Podaci.getBrojUcenika();
        win.setTitle(Init.dData.UCENICI_TITLE_STRING);
        butPan = new JPanel();
        sidePan = new JPanel(null);
        if (Ucenik.sortedByRazred()) {
            knjSeparatori = new JSeparator[Podaci.getMaxBrojUcenikKnjiga()][Ucenik.getBrojRazreda() - 1];
            ucSeparatori = new JSeparator[Ucenik.getBrojRazreda() - 1];
        } else {
            knjSeparatori = null;
            ucSeparatori = null;
        }
        knjigePan = new JPanel[getMaxBrojUcenikKnjiga()];
        uceniciPan = new JPanel();
        pan = new JPanel();
        scroll = new JScrollPane(pan);
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, butPan);
        buttons = new LinkedList<>() /*{
                 private static final long serialVersionUID = 1L;
                 @Override
                 public boolean contains(Object obj) {
                 if (this.stream().anyMatch((el) -> (el.equals(obj)))) {
                 return true;
                 }
                 return false;
                 }
                 }*/;
        //uzmiBut = new JButton[Podaci.getBrojUcenika()];
        //vratiBut = new JButton[Podaci.getBrojUcenika()];
        searchBox = new JTextField(Init.dData.UCENICI_SEARCH_STRING);
        selectAllUc = new JCheckBox(Init.dData.UCENICI_UCENICI_STRING);
    }

    /**
     * Pregled ucenika sa knjigama koje su trenutno kod njih.
     *
     * @since 1.7.'13.
     */
    public void pregledUcenika() {
        initPanels();
        initText();
        setTextAndSeparators();
        initButtons();
        initIcons();
        initMainListeners();
        initSearchBox();
        showWindow();
        setInputMaps();
    }

    /**
     * Inicijalizuje prozor i panele. Postavalja closeOperation, boju, font,
     * tekst, scroll.
     */
    private void initPanels() {
        //sirina = UCENICI_KNJPANEL_WIDTH * getMaxBrojUcenikKnjiga() + UCENICI_FIXED_WIDTH;
        //visina = UCENICI_HEIGHT;
        //win.setSize(sirina, visina);
        //LOGGER.log(Level.CONFIG, "Postavljam visinu prozora sa učenicima na {0}, širinu na {1}",
        //        new Object[]{visina, sirina});
        //win.setResizable(false);
        win.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Grafika.exit();
            }
        });
        pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
        pan.setBackground(Grafika.getBgColor());
        pan.setAutoscrolls(true);
        scroll.add(scroll.createVerticalScrollBar());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        butPan.setBackground(Grafika.getBgColor());
        butPan.setLayout(new FlowLayout(FlowLayout.CENTER));
        split.setOneTouchExpandable(false);
        split.setResizeWeight(1);
        //split.setDividerLocation(0.8);
        uceniciPan.setLayout(new BoxLayout(uceniciPan, BoxLayout.Y_AXIS));
        uceniciPan.setBackground(Grafika.getBgColor());
        uceniciPan.setAlignmentY(0);
        for (int i = 0; i < getMaxBrojUcenikKnjiga(); i++) {
            knjigePan[i] = new JPanel();
            knjigePan[i].setLayout(new BoxLayout(knjigePan[i], BoxLayout.Y_AXIS));
            knjigePan[i].setBackground(Grafika.getBgColor());
            knjigePan[i].setAlignmentY(0);
        }
        sidePan.setLayout(null);
        sidePan.setBackground(Grafika.getBgColor());
//        sidePan.setPreferredSize(new Dimension(UCENICI_SIDEPAN_WIDTH,
//                (Podaci.getBrojUcenika() + 1) * UCENICI_HEIGHT_PER_LABEL));
//        sidePan.setBorder(BorderFactory.createLineBorder(Color.RED));
        sidePan.setAlignmentY(0);
        win.setContentPane(split);
    }

    /**
     * Postavlja precice na tastaturi.
     */
    private void setInputMaps() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((KeyEvent e) -> {
            if(e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) { //ctrl + z
                Undo.undo();
                return true;
            } else if(e.getKeyCode() == KeyEvent.VK_Y && e.isControlDown()) { //ctrl  + y
                Undo.redo();
                return true;
            } else if(e.getKeyCode() == KeyEvent.VK_T && e.isControlDown()) {
                if(e.isShiftDown()) { //ctrl + shift + T
                    new Console().console();
                    return true;
                } else if(e.isAltDown()) { //ctrl + alt + T
                    new Console().fullConsoleWindow();
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Postavlja text, boje i font za JCheckBox-ove.
     */
    private void initText() {
        Podaci.sortUcenike();
        Ucenik uc;
        knjige = new IndexedCheckbox[maxKnjiga][maxUcenika + 1];
        selectAllUc.setFont(Grafika.getLabelFont());
        selectAllUc.setForeground(Grafika.getFgColor());
        selectAllUc.setBackground(Grafika.getBgColor());
        selectAllUc.setBorder(new EmptyBorder(INSET));

        ucenici = new IndexedCheckbox[maxUcenika];
        Iterator<Ucenik> it = Podaci.iteratorUcenika();
        for (int i = 0; i < maxUcenika; i++) {
            ucenici[i] = new IndexedCheckbox(it.next().getDisplayName(), i, Init.dData.INVALID);
        }

        for (int i = 0; i < maxKnjiga; i++) {
            knjige[i][0] = new IndexedCheckbox(Init.dData.UCENICI_KNJIGE_STRING, 1, i);
            it = Podaci.iteratorUcenika();
            for (int j = 1; j < maxUcenika + 1; j++) {
                uc = it.next();
                knjige[i][j] = new IndexedCheckbox(uc.getKnjiga(i), j - 1, i);
            }
        }
        LOGGER.log(Level.FINE, "Postavio labele učenika");
        uceniciPan.add(selectAllUc);
        pan.add(uceniciPan);
    }

    /**
     * Dodaje JCheckBoxove na panel i postavlja separatore.
     */
    private void setTextAndSeparators() {
        if (ucSeparatori != null) {
            for (int i = 0; i < ucSeparatori.length; i++) {
                ucSeparatori[i] = new JSeparator(SwingConstants.HORIZONTAL);
            }
        }
        int[] razredi = Podaci.getGraniceRazreda();
        int razredIterator = 0;
        for (int i = 0; i < maxUcenika; i++) {
            uceniciPan.add(ucenici[i]);
            if (ucSeparatori != null && i == razredi[razredIterator] && i != maxUcenika - 1) {
                uceniciPan.add(ucSeparatori[razredIterator]);
                razredIterator++;
            }
        }
        if (knjSeparatori != null) {
            for (JSeparator[] knjSeparatori0 : knjSeparatori) {
                for (int i = 0; i < knjSeparatori0.length; i++) { //ne moze preko for : loopa.
                    knjSeparatori0[i] = new JSeparator(SwingConstants.HORIZONTAL);
                }
            }
        }
        for (int i = 0; i < maxKnjiga; i++) {
            razredIterator = 0;
            for (int j = 0; j < maxUcenika + 1; j++) {
                knjigePan[i].add(knjige[i][j]);
                if (knjSeparatori != null && j - 1 == razredi[razredIterator] && j != maxUcenika) {
                    knjigePan[i].add(knjSeparatori[i][razredIterator]);
                    razredIterator++;
                }
            }
            pan.add(knjigePan[i]);
        }
    }

    /**
     * Inicijalizuje i dodaje glavnu dugmad na butPan i RigidArea (razmak)
     */
    private void initButtons() {
        JButton noviUc = new JButton(Init.dData.UCENICI_NOVIUC_STRING);
        noviUc.setPreferredSize(new Dimension(Init.dData.UCENICI_NOVIUC_WIDTH, Init.dData.UCENICI_BUTPAN_BUTTON_HEIGHT));
        noviUc.setFont(Grafika.getButtonFont());
        noviUc.addActionListener((ActionEvent e) -> {
            new UceniciUtils().dodajNovogUcenika();
        });
        butPan.add(noviUc);
        JButton delUc = new JButton(Init.dData.UCENICI_DELUC_STRING);
        delUc.setFont(Grafika.getButtonFont());
        delUc.setPreferredSize(new Dimension(Init.dData.UCENICI_DELUC_WIDTH, Init.dData.UCENICI_BUTPAN_BUTTON_HEIGHT));
        delUc.addActionListener((ActionEvent e) -> {
            obrisiUcenika();
        });
        butPan.add(delUc);
        JButton novaGen = new JButton(Init.dData.UCENICI_NOVAGEN_STRING);
        novaGen.setFont(Grafika.getButtonFont());
        novaGen.setPreferredSize(new Dimension(Init.dData.UCENICI_NOVAGEN_WIDTH, Init.dData.UCENICI_BUTPAN_BUTTON_HEIGHT));
        novaGen.addActionListener((ActionEvent e) -> {
            new UceniciUtils().dodajNovuGeneraciju();
        });
        butPan.add(novaGen);

        butPan.add(Box.createRigidArea(new Dimension(Init.dData.UCENICI_BUTPAN_RIGIDAREA_WIDTH, 1)));
    }

    /**
     * Postavlja ikonice i actionListener-e za iste.
     */
    private void initIcons() {
        JButton pregledBut;
        try {
            BufferedImage buttonIcon = ImageIO.read(new File(Utils.getWorkingDir() + "knjige.png"));
            pregledBut = new JButton(getIconFromImage(buttonIcon, Init.dData.UCENICI_ICON_WIDTH, Init.dData.UCENICI_ICON_HEIGHT));
            pregledBut.setToolTipText(Init.dData.UCENICI_PREGLEDKNJ_TOOLTIP_STRING);
        } catch (IOException ex) {
            pregledBut = new JButton(Init.dData.UCENICI_PREGLEDKNJ_STRING);
            LOGGER.log(Level.SEVERE, "IO greška pri učitavanju slike za dugme za pregled knjiga", ex);
        }
        pregledBut.addActionListener((ActionEvent e) -> {
            new Knjige().pregledKnjiga();
        });
        pregledBut.setFocusable(false);
        //pregledBut.setBackground(Color.WHITE);
        pregledBut.setContentAreaFilled(false); // ??
        pregledBut.setBorder(null);
        butPan.add(pregledBut);

        JButton saveBut;
        try {
            BufferedImage buttonIcon = ImageIO.read(new File(Utils.getWorkingDir() + "save.png"));
            saveBut = new JButton(getIconFromImage(buttonIcon, Init.dData.UCENICI_ICON_WIDTH, Init.dData.UCENICI_ICON_HEIGHT));
            saveBut.setToolTipText(Init.dData.UCENICI_SAVE_TOOLTIP_STRING);
        } catch (IOException ex) {
            saveBut = new JButton(Init.dData.UCENICI_SAVE_STRING);
            LOGGER.log(Level.SEVERE, "IO greška pri učitavanju slike za dugme za čuvanje", ex);
        }
        saveBut.addActionListener((ActionEvent e) -> {
            try {
                Save.save();
            } catch (IOException ex) {
                showMessageDialog(null, Init.dData.UCENICI_IOEX_MSG_STRING, Init.dData.UCENICI_IOEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
            }
        });
        saveBut.setFocusable(false);
        saveBut.setBorder(null);
        saveBut.setContentAreaFilled(false);
        butPan.add(saveBut);

        JButton podesavanjaBut;
        try {
            BufferedImage buttonIcon = ImageIO.read(new File(Utils.getWorkingDir() + "gear.png"));
            podesavanjaBut = new JButton(getIconFromImage(buttonIcon, Init.dData.UCENICI_ICON_WIDTH, Init.dData.UCENICI_ICON_HEIGHT));
            podesavanjaBut.setToolTipText(Init.dData.UCENICI_PODESAVANJA_STRING);

        } catch (IOException ex) {
            podesavanjaBut = new JButton(Init.dData.UCENICI_PODESAVANJA_STRING);
            LOGGER.log(Level.SEVERE, "IO greška pri učitavanju slike za dugme za podešavanja", ex);
        }
        podesavanjaBut.addActionListener((ActionEvent e) -> {
            new Podesavanja().podesavanja();
        });
        podesavanjaBut.setFocusable(false);
        podesavanjaBut.setBorder(null);
        podesavanjaBut.setContentAreaFilled(false);
        butPan.add(podesavanjaBut);
    }

    /**
     * Postavlja listenere za selectAll, uzmiKnjigu i vratiKnjigu.
     */
    private void initMainListeners() {
        selectAllUc.addItemListener((ItemEvent e) -> {
            selectAllUc();
        });
        for (int i = 0; i < maxKnjiga; i++) {
            final int red = i;
            knjige[i][0].addItemListener((ItemEvent e) -> {
                selectAllKnj(red);
            });
        }
        uzimanjeListener = (ItemEvent e) -> {
            uzimanjeKnjige((IndexedCheckbox) e.getItem());
            sidePan.repaint();
        };
        vracanjeListener = (ItemEvent e) -> {
            vracanjeKnjige((IndexedCheckbox) e.getItem());
            sidePan.repaint();
        };
        for (int i = 0; i < maxUcenika; i++) {
            ucenici[i].addItemListener(uzimanjeListener);
            //knjige
            for (int j = 0; j < maxKnjiga; j++) {
                if (knjige[j][i + 1].getText().equals(" ")) {
                    knjige[j][i + 1].addItemListener(uzimanjeListener);
                } else {
                    knjige[j][i + 1].addItemListener(vracanjeListener);
                }
            }
        }
    }
    private ItemListener vracanjeListener;
    private ItemListener uzimanjeListener;

    /**
     * Postavlja velicinu i poziciju i prikazuje prozor
     */
    private void showWindow() {
        if(Config.getAsBool("customSize")) {
            win.setSize(Init.dData.UCENICI_FIXED_WIDTH + maxKnjiga * Init.dData.UCENICI_KNJPANEL_WIDTH, Init.dData.UCENICI_HEIGHT);
        }
        else {
            win.pack();
            win.setSize(win.getWidth(), MAX_HEIGHT);
        }
        win.setLocationRelativeTo(null);
        win.setVisible(true);
    }
    /**
     * Inicijalizuje searchbox. Postavlje tekst, font, boju i listener-e.
     * Postavlja i velicinu sidePan-a (workaround)
     */
    private void initSearchBox() {
        searchBox.addFocusListener(this);
        searchBox.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                search();
            });
        });
        searchBox.setBounds(Init.dData.UCENICI_SEARCHBOX_X, Init.dData.UCENICI_SEARCHBOX_Y,
                Init.dData.UCENICI_SEARCHBOX_WIDTH, Init.dData.UCENICI_SEARCHBOX_HEIGHT);
        searchBox.setFont(Grafika.getLabelFont());
        searchBox.setBackground(Grafika.getTFColor());
        searchBox.setForeground(Grafika.getFgColor());
        searchBox.setCaretColor(Grafika.getFgColor());
        sidePan.add(searchBox);
        if (maxUcenika > 0) {
            sidePan.setPreferredSize(new Dimension(Init.dData.UCENICI_SIDEPAN_WIDTH,
                    (maxUcenika+1)*Init.dData.UCENICI_HEIGHT_PER_LABEL));
        }
        //NE RADI
        pan.add(sidePan);
    }

    /**
     * Vraca i resize-uje ImageIcon prema datim width i height, prema datoj
     * image
     *
     * @param image slika iz koje treba Icon
     * @param width sirina Icon-a
     * @param height visina Icon-a
     * @return Icon
     */
    private ImageIcon getIconFromImage(BufferedImage image, int width, int height) {
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        double scaleX = (double) width / imageWidth;
        double scaleY = (double) height / imageHeight;
        AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
        AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);

        return new ImageIcon(bilinearScaleOp.filter(
                image,
                new BufferedImage(width, height, image.getType())));
    }

    //----------METODE ZA LISTENERE---------------------------------------------
    /**
     * Radi iteraciju preko svih checkboxova i ako je selectall selektovan i
     * dati checkbox je vidljiv, selektuje ga. Ako to ne vazi, deselektuje
     * checkbox.
     */
    private void selectAllUc() {
        for (JCheckBox ucenik : ucenici) {
            ucenik.setSelected(ucenik.isVisible() && selectAllUc.isSelected() && ucenik.isEnabled());
        }
    }

    /**
     * Postavlja sve vidljive i omogucene JCheckBox-ove na selected ili
     * unselected.
     *
     * @param red
     */
    private void selectAllKnj(int red) {
        for (int j = 1; j < maxUcenika + 1; j++) {
            if (knjige[red][j].isVisible() && knjige[red][j].isEnabled()) {
                knjige[red][j].setSelected(knjige[red][0].isSelected());
            }
        }
    }

    /**
     * Brise selektova ucenike. Ako ne postoje, zove
     * {@link UceniciUtils#obrisiUcenika()}.
     */
    private void obrisiUcenika() {
        List<Integer> imena = new LinkedList<>();
        for (int i = 0; i < maxUcenika; i++) {
            if (ucenici[i].isSelected()) {
                imena.add(i);
            }
        }
        if (imena.isEmpty()) {
            new UceniciUtils().obrisiUcenika();
        } else {
            int delCount = 0; //broj obrisanih ucenika, posto se indexi menjaju prilikom brisanja
            for (Integer ime : imena) {
                try {
                    ime -= delCount;
                    Podaci.obrisiUcenika(ime);
                    delCount++;
                } catch (PreviseKnjiga ex) {
                    LOGGER.log(Level.INFO, "Kod učenika {0} se nalaze neke knjige. "
                            + "Brisanje neuspešno", ime);
                    JOptionPane.showMessageDialog(null, Init.dData.UCENICI_PKEX_MSG_STRING, Init.dData.UCENICI_PKEX_MSG_STRING,
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        win.dispose();
        new Ucenici().pregledUcenika();
    }

    /**
     * Uzima knjigu za selektovanog ucenika.
     *
     * @param red
     */
    private void uzimanjeKnjige(IndexedCheckbox box) {
        boolean selected = false;
        int red = box.getIndex();
        int knjRed = red + 1;
        for (int k = 0; k < maxKnjiga; k++) {
            if (knjige[k][knjRed].isSelected()) {
                selected = true; //makar jedan selektovan checkbox
            }
        }
        if (ucenici[red].isSelected()) {
            selected = true;
        }
        int index = buttons.indexOf(new SmallButton(red, Init.dData.INVALID, Init.dData.INVALID));
        if (index != Init.dData.INVALID) { //contains radi argument.equals(element)
            if (!selected) { //ako nema selektovanih boxova, a dugme postoji
                sidePan.remove(index + 1); //ukloni dugme, +1 zato sto se na prvom mestu nalazi searchBox
                buttons.remove(index); //ukloni dugme iz liste
                for (int k = 0; k < maxKnjiga; k++) {
                    if (!knjige[k][knjRed].getText().equals(" ")) {
                        knjige[k][knjRed].setEnabled(true); //ponovo omogucuje checkboxove za vracanje
                    }
                }
                sidePan.repaint();
                return; //izadji iz listenera
            } else {
                return;
            }
        }
        SmallButton button = new SmallButton(red, Init.dData.INVALID,
                ucenici[red].getLocationOnScreen().y - sidePan.getLocationOnScreen().y);
        setSidePanSize(red);
        button.uzmi(this);
        buttons.add(button);
        sidePan.add(button);
        for (int k = 0; k < maxKnjiga; k++) {
            if (!knjige[k][knjRed].getText().equals(" ")) {
                knjige[k][knjRed].setEnabled(false); //onemogucuje jcheckboxove za vracanje
            }
        }
        sidePan.repaint();
    }

    /**
     * Vraca selektovanu knjigu za selektovanog ucenika
     *
     * @param kol kolona u kojoj je knjige
     * @param red red u kojoj su ucenik i knjiga
     */
    private void vracanjeKnjige(IndexedCheckbox box) {
        boolean selected = false;
        int red = box.getIndex();
        int knjIndex = box.getKnjIndex();
        int knjRed = red + 1;

        for (int k = 0; k < maxKnjiga; k++) {
            if (knjige[k][knjRed].isSelected()) {
                selected = true; //makar jedan selektovan checkbox 
            }
        }
        if (!selected) { //ako nema selektovanih boxova
            int index = buttons.indexOf(new SmallButton(red, Init.dData.INVALID, Init.dData.INVALID));
            //contains radi argument.equals(element), pa argument mora da bude SmallButton
            sidePan.remove(index + 1); //ukloni dugme
            buttons.remove(index);
            ucenici[red].setEnabled(true); //ponovo omogucuje checkboxove za uzimanje
            for (int k = 0; k < maxKnjiga; k++) {
                if (knjige[k][knjRed].getText().equals(" ")) {
                    knjige[k][knjRed].setEnabled(true);
                }
            }
            return; //izadji iz listenera
        }
        for (SmallButton btn : buttons) { //ako je dugme vec tu
            if (btn.equals(red)) {
                btn.addNaslovZaVracanje(knjIndex);
                return; //izadji
            }
        }
        SmallButton button = new SmallButton(red, knjIndex,
                ucenici[red].getLocationOnScreen().y - sidePan.getLocationOnScreen().y);
        button.vrati(this);
        setSidePanSize(red);
        buttons.add(button);
        sidePan.add(button);
        ucenici[red].setEnabled(false); //ne mogu i uzimanje i vracanje biti vidljivi u istom trenutku
        for (int k = 0; k < maxKnjiga; k++) { //onemogućuje checkboxove za uzimanje
            if (knjige[k][knjRed].getText().equals(" ")) {
                knjige[k][knjRed].setEnabled(false);
            }
        }
    }

    /**
     * Postavlja size sidePan-a. WORKAROUND
     *
     * @param red
     */
    private void setSidePanSize(int red) { //WORKAROUND, pri scrollu se vraca na staro
        if (red == Init.dData.INVALID || sidePan.getHeight()
                < ucenici[red].getLocationOnScreen().y - sidePan.getLocationOnScreen().y + Init.dData.UCENICI_HEIGHT_PER_LABEL) {
            sidePan.setSize(Init.dData.UCENICI_SIDEPAN_WIDTH,
                    ucenici[red].getLocationOnScreen().y - sidePan.getLocationOnScreen().y + Init.dData.UCENICI_HEIGHT_PER_LABEL);
        }
    }

    /**
     * Radi pretragu.
     */
    private void search() {
        System.out.println(System.currentTimeMillis() + "\tPocinjem pretragu");
        LOGGER.log(Level.FINE, "Počinjem pretragu (grafički)");
        if (ucSeparatori != null) {
            for (JSeparator sep : ucSeparatori) {
                uceniciPan.remove(sep); //remove ili samo reset ??
            }
            for (int i = 0; i < maxKnjiga; i++) {
                for (JSeparator knjSeparatori1 : knjSeparatori[i]) {
                    knjigePan[i].remove(knjSeparatori1);
                }
            }
        }
        ArrayList<Integer> ucIndexes = Pretraga.pretraziUcenike(searchBox.getText());

        System.out.println(System.currentTimeMillis() + "\tBackend gotov. Krecem iteracije");
        Ucenik uc;
        for (int i = 0; i < maxUcenika; i++) {
            if (ucIndexes.contains(i)) {
                uc = getUcenik(i);
                ucenici[i].setText(uc.getDisplayName());
                ucenici[i].setVisible(true);
                for (int j = 0; j < maxKnjiga; j++) {
                    if (uc.isKnjigaEmpty(j)) {
                        knjige[j][i + 1].setText(" "); //workaround
                        knjige[j][i + 1].setVisible(true);
                    } else {
                        knjige[j][i + 1].setKnjiga(uc.getKnjiga(j));
                        knjige[j][i + 1].setVisible(true);
                    }
                }
            } else {
                ucenici[i].setVisible(false);
                ucenici[i].setSelected(false);
                for (int j = 0; j < maxKnjiga; j++) {
                    knjige[j][i + 1].setVisible(false);
                    knjige[j][i].setSelected(false);
                }
            }
        }
        
        System.out.println(System.currentTimeMillis() + "\tIteracija gotova");
        sidePan.setMaximumSize(new Dimension(Init.dData.UCENICI_SIDEPAN_WIDTH,
                (ucIndexes.size() + 1) * selectAllUc.getHeight()));

        pan.revalidate();
        pan.repaint();
        System.out.println(System.currentTimeMillis() + "\tPretraga gotova");
    }

    /**
     * Refreshuje ucenika sa datim indexom i resetuje checkboxove i dugmad.
     * @param index index ucenika
     * @param but dugme koje treba izbaciti
     */
    void refreshUcenik(int index, SmallButton but) {
        Ucenik uc = Podaci.getUcenik(index);
        ucenici[index].setSelected(false);
        ucenici[index].setEnabled(true);
        for (int j = 0; j < maxKnjiga; j++) {
            knjige[j][index + 1].removeItemListener(vracanjeListener);
            knjige[j][index + 1].removeItemListener(uzimanjeListener);
            knjige[j][index + 1].setSelected(false);
            knjige[j][index + 1].setEnabled(true);
            if (uc.isKnjigaEmpty(j)) {
                knjige[j][index + 1].setText(" "); //workaround
                knjige[j][index + 1].addItemListener(uzimanjeListener);
            } else {
                knjige[j][index + 1].setKnjiga(uc.getKnjiga(j));
                knjige[j][index + 1].addItemListener(vracanjeListener);
            }
        }
        sidePan.remove(but);
        buttons.remove(but);
        sidePan.repaint();
        win.pack();
        win.setSize(win.getWidth(), MAX_HEIGHT);
    }

    //==========FOCUS============================================================
    @Override
    public void focusGained(FocusEvent e) {
        if (searchBox.getText().equals(Init.dData.UCENICI_SEARCH_STRING)) {
            searchBox.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (searchBox.getText().isEmpty()) {
            searchBox.setText(Init.dData.UCENICI_SEARCH_STRING);
        }
    }

}
