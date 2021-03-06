package rs.luka.biblioteka.grafika;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import rs.luka.biblioteka.data.Podaci;
import static rs.luka.biblioteka.data.Podaci.getUcenik;
import rs.luka.biblioteka.exceptions.Duplikat;
import rs.luka.biblioteka.exceptions.LosFormat;
import rs.luka.biblioteka.exceptions.NemaViseKnjiga;
import rs.luka.biblioteka.exceptions.Prazno;
import rs.luka.biblioteka.exceptions.VrednostNePostoji;
import rs.luka.biblioteka.funkcije.Init;
import rs.luka.biblioteka.funkcije.Pretraga;

/**
 * Pomocne metode za klasu Knjige
 * 
 * @author luka
 */
public class KnjigeUtils {
    private static final java.util.logging.Logger LOGGER
            = java.util.logging.Logger.getLogger(KnjigeUtils.class.getName());
    
    private final Dimension NOVI_SIZE = new Dimension(Init.dData.NOVINASLOV_WIDTH, Init.dData.NOVINASLOV_HEIGHT);
    
    private final Rectangle NOVI_NASLOV_BOUNDS = new Rectangle
        (Init.dData.NOVINASLOV_LABELS_X, Init.dData.NOVINASLOV_NASLOV_Y, Init.dData.NOVINASLOV_LABELS_WIDTH, Init.dData.NOVINASLOV_LABELS_HEIGHT);
    private final Rectangle NOVI_NASLOVTF_BOUNDS = new Rectangle(Init.dData.NOVINASLOV_TEXTFIELDS_X, Init.dData.NOVINASLOV_NASLOVTF_Y, 
            Init.dData.NOVINASLOV_TEXTFIELDS_WIDTH, Init.dData.NOVINASLOV_TEXTFIELDS_HEIGHT);
    private final Rectangle NOVI_PISAC_BOUNDS = new Rectangle
        (Init.dData.NOVINASLOV_LABELS_X, Init.dData.NOVINASLOV_PISAC_Y, Init.dData.NOVINASLOV_LABELS_WIDTH, Init.dData.NOVINASLOV_LABELS_HEIGHT);
    private final Rectangle NOVI_PISACTF_BOUNDS = new Rectangle(Init.dData.NOVINASLOV_TEXTFIELDS_X, Init.dData.NOVINASLOV_PISACTF_Y, 
            Init.dData.NOVINASLOV_TEXTFIELDS_WIDTH, Init.dData.NOVINASLOV_TEXTFIELDS_HEIGHT);
    private final Rectangle NOVI_KOLICINA_BOUNDS = new Rectangle
        (Init.dData.NOVINASLOV_LABELS_X, Init.dData.NOVINASLOV_KOLICINA_Y, Init.dData.NOVINASLOV_LABELS_WIDTH, Init.dData.NOVINASLOV_LABELS_HEIGHT);
    private final Rectangle NOVI_KOLICINATF_BOUNDS = new Rectangle(Init.dData.NOVINASLOV_TEXTFIELDS_X, Init.dData.NOVINASLOV_KOLICINATF_Y, 
            Init.dData.NOVINASLOV_TEXTFIELDS_WIDTH, Init.dData.NOVINASLOV_TEXTFIELDS_HEIGHT);
    private final Rectangle NOVI_UNOS_BOUNDS = new Rectangle
        (Init.dData.NOVINASLOV_UNOS_X, Init.dData.NOVINASLOV_UNOS_Y, Init.dData.NOVINASLOV_UNOS_WIDTH, Init.dData.NOVINASLOV_UNOS_HEIGHT);
    
    private final Dimension UCSEARCH_SIZE = new Dimension(Init.dData.UCSEARCH_WIDTH, Init.dData.UCSEARCH_HEIGHT);
    private final Rectangle UCSEARCH_NASLOV_BOUNDS = new Rectangle
        (Init.dData.UCSEARCH_NASLOV_X, Init.dData.UCSEARCH_NASLOV_Y, Init.dData.UCSEARCH_NASLOV_WIDTH, Init.dData.UCSEARCH_NASLOV_HEIGHT);
    private final Rectangle UCSEARCH_NASLOVTF_BOUNDS = new Rectangle
        (Init.dData.UCSEARCH_NASLOVTF_X, Init.dData.UCSEARCH_NASLOVTF_Y, Init.dData.UCSEARCH_NASLOVTF_WIDTH, Init.dData.UCSEARCH_NASLOVTF_HEIGHT);
    
    /**
     * Prozor za upisivanje novog naslova.
     *
     * @since jako davno (pocetak)
     */
    protected void novi() {
        //----------JFrame&JPanel-----------------------------------------------
        final JFrame nnF = new JFrame(Init.dData.NOVINASLOV_TITLE_STRING);
        nnF.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        nnF.setSize(NOVI_SIZE);
        nnF.setLocationRelativeTo(null);
        nnF.setResizable(false);
        JPanel nnPan = new JPanel(null);
        nnPan.setBackground(Grafika.getBgColor());
        nnF.setContentPane(nnPan);
        //----------JLabels&JTextFields-----------------------------------------
        JLabel naslov = new JLabel(Init.dData.NOVINASLOV_NASLOV_STRING);
        naslov.setBounds(NOVI_NASLOV_BOUNDS);
        naslov.setFont(Grafika.getLabelFont());
        naslov.setForeground(Grafika.getFgColor());
        nnPan.add(naslov);
        final JTextField naslovTF = new JTextField();
        naslovTF.setBounds(NOVI_NASLOVTF_BOUNDS);
        naslovTF.setFont(Grafika.getLabelFont());
        naslovTF.setBackground(Grafika.getTFColor());
        naslovTF.setForeground(Grafika.getFgColor());
        naslovTF.setCaretColor(Grafika.getFgColor());
        nnPan.add(naslovTF);
        JLabel pisac = new JLabel(Init.dData.NOVINASLOV_PISAC_STRING);
        pisac.setBounds(NOVI_PISAC_BOUNDS);
        pisac.setFont(Grafika.getLabelFont());
        pisac.setForeground(Grafika.getFgColor());
        JTextField pisacTF = new JTextField();
        nnPan.add(pisac);
        pisacTF.setBounds(NOVI_PISACTF_BOUNDS);
        pisacTF.setFont(Grafika.getLabelFont());
        pisacTF.setForeground(Grafika.getFgColor());
        pisacTF.setCaretColor(Grafika.getFgColor());
        pisacTF.setBackground(Grafika.getTFColor());
        nnPan.add(pisacTF);
        JLabel kolicina = new JLabel(Init.dData.NOVINASLOV_KOLICINA_STRING);
        kolicina.setBounds(NOVI_KOLICINA_BOUNDS);
        kolicina.setFont(Grafika.getLabelFont());
        kolicina.setForeground(Grafika.getFgColor());
        nnPan.add(kolicina);
        final JTextField kolicinaTF = new JTextField();
        kolicinaTF.setBounds(NOVI_KOLICINATF_BOUNDS);
        kolicinaTF.setFont(Grafika.getLabelFont());
        kolicinaTF.setForeground(Grafika.getFgColor());
        kolicinaTF.setCaretColor(Grafika.getFgColor());
        kolicinaTF.setBackground(Grafika.getTFColor());

        nnPan.add(kolicinaTF);
        //----------JButton-----------------------------------------------------
        JButton unos = new JButton(Init.dData.NOVINASLOV_UNOS_STRING);
        unos.setFont(Grafika.getButtonFont());
        unos.setBounds(NOVI_UNOS_BOUNDS);
        ActionListener listener = (ActionEvent e) -> {
                try {
                    Podaci.dodajKnjigu(naslovTF.getText(), parseInt(kolicinaTF.getText()), pisacTF.getText());
                    showMessageDialog(null, Init.dData.NOVINASLOV_SUCC_MSG_STRING, Init.dData.NOVINASLOV_SUCC_TITLE_STRING, 
                            JOptionPane.INFORMATION_MESSAGE);
                    nnF.dispose();
                } catch (VrednostNePostoji ex) {
                    LOGGER.log(Level.INFO, "Polje za unos naslova je prazno pri unosu novog naslova");
                    showMessageDialog(null, Init.dData.NOVINASLOV_VNPEX_MSG_STRING,
                            Init.dData.NOVINASLOV_VNPEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
                } catch (Duplikat ex) {
                    LOGGER.log(Level.INFO, "Naslov {0} već postoji", naslovTF.getText());
                    showMessageDialog(null, Init.dData.NOVINASLOV_DEX_MSG_STRING,
                            Init.dData.NOVINASLOV_DEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
                } catch (LosFormat ex) {
                    LOGGER.log(Level.INFO, "{0} ili {1} nije validan string za Knjigu", 
                            new String[]{naslovTF.getText(), pisacTF.getText()});
                    showMessageDialog(null, Init.dData.NOVINASLOV_LFEX_MSG_STRING, 
                            Init.dData.NOVINASLOV_LFEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.INFO, "{0} nije broj", naslovTF.getText());
                    showMessageDialog(null, Init.dData.NOVINASLOV_NFEX_MSG_STRING,
                            Init.dData.NOVINASLOV_NFEX_TITLE_STRING, JOptionPane.ERROR_MESSAGE);
            }
        };
        unos.addActionListener(listener);
        kolicinaTF.addActionListener(listener);
        nnPan.add(unos);
        //----------setVisible--------------------------------------------------
        nnF.setVisible(true);
    }

    /**
     * Prozor za pretragu po ucenicima (kod koga je koja knjiga).
     *
     * @param ime ako nije null, prikazuje prozor koji trazi da se unese ime
     * ucenika, pa zatim pretrazuje i ponovo iscrtava isti prozor sa trazenim
     * podacima. Ako je ime string
     *
     * @since 25.6.'14.
     */
    protected void ucSearch(String ime, int visinaProzora) {
        //-----------JFrame&JPanel------------------------------
        JFrame winS = new JFrame(Init.dData.UCSEARCH_TITLE_STRING);
        winS.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        winS.setSize(UCSEARCH_SIZE);
        winS.setLocationRelativeTo(null);
        JPanel panS = new JPanel(null);
        panS.setBackground(Grafika.getBgColor());
        winS.setContentPane(panS);
        //------------JLabel-----------------------------------
        JLabel naslov = new JLabel(Init.dData.UCSEARCH_NASLOV_STRING);
        naslov.setFont(Grafika.getLabelFont());
        naslov.setForeground(Grafika.getFgColor());
        naslov.setBounds(UCSEARCH_NASLOV_BOUNDS);
        panS.add(naslov);
        //------------JTextField&ActionListener---------------------------------
        JTextField naslovTF = new JTextField();
        ActionListener listener = (ActionEvent e) -> {
            try {
                ArrayList<Point> inx = Pretraga.pretraziUcenikePoNaslovu(naslovTF.getText());
                if (inx.isEmpty()) {
                    throw new Prazno("Učenici nemaju tu knjigu");
                }
                int ucVisina = min(visinaProzora, Init.dData.UCSEARCH_UCVISINA_FIXED_HEIGHT 
                        + inx.size() * Init.dData.UCSEARCH_UCENIK_HEIGHT);
                panS.setLocation(0, 0);
                panS.setPreferredSize(new Dimension(Init.dData.UCSEARCH_PANEL_WIDTH2, 
                        Init.dData.UCSEARCH_PANEL_FIXED_HEIGHT + inx.size() * Init.dData.UCSEARCH_UCENIK_HEIGHT));
                winS.setSize(Init.dData.UCSEARCH_WIDTH2, ucVisina);
                winS.setLocationRelativeTo(null);
                StringBuilder ucBuild = new StringBuilder();
                StringBuilder dateBuild = new StringBuilder();
                inx.stream().map((inx1) -> {
                    ucBuild.append(getUcenik(inx1.y).getIme()).append("<br>");
                    return inx1;
                }).forEach((inx1) -> {
                    dateBuild.append(dateFormat(getUcenik(inx1.y).
                            getDatumKnjige(inx1.x))).append("<br>");
                });
                JLabel ucenici = new JLabel();
                ucenici.setSize(new Dimension(Init.dData.UCSEARCH_UCENICI_WIDTH, (inx.size() + 1) * Init.dData.UCSEARCH_UCENIK_HEIGHT));
                ucenici.setLocation(Init.dData.UCSEARCH_UCENICI_X, Init.dData.UCSEARCH_LABELS_Y2);
                ucenici.setFont(Grafika.getLabelFont());
                ucenici.setForeground(Grafika.getFgColor());
                ucenici.setText(Init.dData.UCSEARCH_UCENICI_STRING
                        + ucBuild.toString());
                JLabel datumi = new JLabel();
                datumi.setLocation(Init.dData.UCSEARCH_DATUMI_X, Init.dData.UCSEARCH_LABELS_Y2);
                datumi.setSize(new Dimension(Init.dData.UCSEARCH_DATUMI_WIDTH, (inx.size() + 1) * Init.dData.UCSEARCH_UCENIK_HEIGHT));
                datumi.setFont(Grafika.getLabelFont());
                datumi.setForeground(Grafika.getFgColor());
                datumi.setText(Init.dData.UCSEARCH_DATUMI_STRING
                        + dateBuild.toString());
                
                JButton ok = new JButton(Init.dData.UCSEARCH_OK_STRING);
                ok.setFont(Grafika.getButtonFont());
                ok.setLocation(Init.dData.UCSEARCH_OK_X, Init.dData.UCSEARCH_OK_FIXED_Y + inx.size() * Init.dData.UCSEARCH_UCENIK_HEIGHT);
                ok.setSize(Init.dData.UCSEARCH_OK_WIDTH, Init.dData.UCSEARCH_OK_HEIGHT);
                ok.addActionListener((ActionEvent ae) -> {
                    winS.dispose();
                });
                panS.removeAll();
                panS.revalidate();
                panS.add(ucenici);
                panS.add(datumi);
                panS.add(ok);
                panS.revalidate();
                panS.repaint();

                JScrollPane scrollPan = new JScrollPane(panS);
                scrollPan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scrollPan.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                winS.setContentPane(scrollPan);
                winS.revalidate();
                scrollPan.setBackground(Grafika.getBgColor());
                winS.repaint();
            } catch (VrednostNePostoji ex) {
                LOGGER.log(Level.INFO, "Naslov {0} nije pronađen", naslovTF.getText());
                showMessageDialog(null, Init.dData.UCSEARCH_VNPEX_MSG_STRING, Init.dData.UCSEARCH_VNPEX_TITLE_STRING,
                        JOptionPane.ERROR_MESSAGE);
            } catch (Prazno ex) {
                showMessageDialog(null, Init.dData.UCSEARCH_PEX_MSG_STRING, Init.dData.UCSEARCH_PEX_TITLE_STRING, 
                        JOptionPane.INFORMATION_MESSAGE);
            }

        };
        naslovTF.addActionListener(listener);
        naslovTF.setBounds(UCSEARCH_NASLOVTF_BOUNDS);
        naslovTF.setFont(Grafika.getLabelFont());
        naslovTF.setBackground(Grafika.getTFColor());
        naslovTF.setForeground(Grafika.getFgColor());
        naslovTF.setCaretColor(Grafika.getFgColor());
        panS.add(naslovTF);

        //naknadno dodato, najbezbolniji nacin, da ne radim refactoring
        if (ime != null) { //ako je poslat argument koristeci checkbox
            LOGGER.log(Level.FINE, "Iniciram pretragu učenika po knjizi preko checkboxa");
            naslovTF.setText(ime);
            listener.actionPerformed(null);
        }
        //-----------setVisible----------------------------------
        winS.setVisible(true);
    }
    
    /**
     * Pretvara datum iz formata u kojem se cuva u format za prikazivanje. Koristi StringBuilder-e
     * i {@link StringBuilder#insert(int, java.lang.String) insert} da ubaci tacke i pocetak godine.
     * @param date datum u formatu ddMMyy
     * @return datum u formatu dd. MM. yyyy.
     */
    private String dateFormat(String date) {
        StringBuilder sb = new StringBuilder(date);
        sb.insert(2, ". ");
        sb.insert(6, ". 20");
        sb.insert(12, '.');
        return sb.toString();
    }
    
    protected void promeniKolicinu(int knjIndex) {
        String kol = Dijalozi.showTextFieldDialog(Init.dData.PROMENIKOL_TITLE_STRING, Init.dData.PROMENIKOL_MSG_STRING, "");
        try {
            int kolInt = Integer.parseInt(kol);
            if(kol.charAt(0)=='+' || kol.charAt(0)=='-') {
                Podaci.setKolicina(knjIndex, Podaci.getKnjiga(knjIndex).getKolicina()+kolInt);
            }
            else {
                Podaci.setKolicina(knjIndex, kolInt);
            }
        }
        catch(NumberFormatException ex) {
            if(kol == null  || kol.isEmpty())
                return;
            LOGGER.log(Level.INFO, "Uneta količina {0} nije broj", kol);
            JOptionPane.showMessageDialog(null, Init.dData.PROMENIKOL_NFEX_MSG_STRING, Init.dData.PROMENIKOL_NFEX_TITLE_STRING, 
                    JOptionPane.ERROR_MESSAGE);
            promeniKolicinu(knjIndex);
        }
        catch(NemaViseKnjiga ex) {
            LOGGER.log(Level.INFO, "Nema dovoljno knjiga {0}", ex.getMessage());
            JOptionPane.showMessageDialog(null, Init.dData.PROMENIKOL_NVKEX_MSG_STRING, Init.dData.PROMENIKOL_NVKEX_TITLE_STRING, 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
