/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package formPerpus;

import java.awt.Desktop;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.lucene.analysis.id.IndonesianStemmer;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.xml.sax.SAXException;
import perpustakaan.freqDokumen;

/**
 *
 * @author ASUS
 */
public class menuUtama extends javax.swing.JFrame {

    /**
     * Creates new form menuUtama
     */
    public String files;
    public int byk;
    private File tfolder, tmp[];
    public Vector nama = new Vector();
    public Vector stopword = new Vector();
    public Vector psqrtDOC = new Vector();
    public Vector isiDokumen = new Vector();
    public Vector tes = new Vector();
    Map<String, Set<freqDokumen>> pemetaanSumber = new HashMap<>();
    public HashMap<String, Integer> TF = new HashMap<String, Integer>();
    public HashMap<String, Float> kkid = new HashMap<String, Float>();

    public void kosongkanTable() {
        try {
            String kosong = "TRUNCATE TABLE tb_kopus";
            java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(kosong);
            pst.execute();
        } catch (Exception e) {
        }
    }

    public void tambahSumber() {
        try {
            Vector tmp = new Vector();
            cekSumber(tmp);
            for (int i = 0; i < jListData.getModel().getSize(); i++) {
                int b = 0;
                String sql = "insert into tb_sumber values('" + b + "','" + nama.get(i).toString() + "','" + jListData.getModel().getElementAt(i) + "')";
                java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
                java.sql.PreparedStatement pst = conn.prepareStatement(sql);
                pst.execute();
            }
        } catch (Exception e) {
        }
    }

    public void cekSumber(Vector d) {
        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String NamaSumber = "select url from tb_sumber";
            ResultSet res = st.executeQuery(NamaSumber);
            while (res.next()) {
                //System.out.println(res.getString("url"));
                d.add(res.getString("url"));
            }
            st.close();
            res.close();
        } catch (Exception e) {
        }
    }

    public void PlainText() {
        try {
            Statement state = koneksi_database.koneksi.koneksiDB().createStatement();
            String banyakKolom = "select distinct url from tb_sumber";
            ResultSet res = state.executeQuery(banyakKolom);
            InputStream is;
            while (res.next()) {
                //System.out.print(res.getString("url"));
                try {
                    is = new FileInputStream(res.getString("url"));
                    BodyContentHandler ch = new BodyContentHandler();
                    Metadata metadata = new Metadata();
                    Parser parser = new AutoDetectParser();
                    parser.parse(is, ch, metadata, new ParseContext());
                    //System.out.println(ch.toString());
                    isiDokumen.add(ch.toString());
                } catch (SQLException | IOException | SAXException | TikaException e) {
                    JOptionPane.showMessageDialog(rootPane, e);
                }
            }
            state.close();
            res.close();
        } catch (SQLException | HeadlessException e) {
            JOptionPane.showMessageDialog(rootPane, e);
        }
    }

    public void stoplist(Vector tokenKata, Vector tdok, Vector jml) {
        try {
            FileReader fr = new FileReader("src/stopwords.txt");
            BufferedReader br = new BufferedReader(fr);
            StringTokenizer st = new StringTokenizer(br.readLine());
            while (st.hasMoreTokens()) {
                stopword.add(st.nextToken());
            }
            for (int a = 0; a < tokenKata.size(); a++) {
                for (int b = 0; b < stopword.size(); b++) {
                    if (a % 2 == 0) {
                        if (tokenKata.get(a).equals(stopword.get(b))) {
                            tokenKata.remove(a);
                            tokenKata.remove(a + 1);
                            tdok.remove(a / 2);
                            jml.remove(a / 2);
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

    }

    public void tokenKata() {
        PlainText();
        Vector<Integer> jml = new Vector<Integer>();
        for (int a = 0; a < isiDokumen.size(); a++) {
            StringTokenizer st = new StringTokenizer(isiDokumen.get(a).toString().toLowerCase(), "\t\n:;,(). ");
            freqDokumen dokumen = new freqDokumen("D" + (a + 1));
            while (st.hasMoreTokens()) {
                
                int len = 0;
                IndonesianStemmer stema = new IndonesianStemmer();
                EnglishStemmer es = new EnglishStemmer();
                char[] tes = st.nextToken().toCharArray();
                len = stema.stem(tes, tes.length, true);
                String k = new String(tes, 0, len);
                es.setCurrent(k.trim());
                es.stem();                
                String kata = es.getCurrent();
                dokumen.put(kata);
                Set<freqDokumen> dok = pemetaanSumber.get(kata);
                if (dok == null) {
                    dok = new HashSet<>();
                    pemetaanSumber.put(kata, dok);
                }
                dok.add(dokumen);
            }
        }
        StringBuilder build = new StringBuilder();
        StringBuilder dapatkanDokumen = new StringBuilder();
        for (String kata : pemetaanSumber.keySet()) {
            Set<freqDokumen> dok = pemetaanSumber.get(kata);
            build.append(kata + "\t");
            int jk = 0;
            for (freqDokumen dokumen : dok) {
                dapatkanDokumen.append(dokumen.getNamaDokumen());
                dapatkanDokumen.append(", ");
                //build.append(dokumen.getNamaDokumen() + ":" + dokumen.getBanyak(kata));
                build.append(dokumen.getBanyak(kata));
                build.append(", ");
                jk++;
            }
            jml.add(jk);
            //System.out.println(jk);  
            dapatkanDokumen.delete(dapatkanDokumen.length() - 2, dapatkanDokumen.length() - 1);
            dapatkanDokumen.append("\n");

            build.delete(build.length() - 2, build.length() - 1);
            build.append("\n");
        }
        //System.out.println(build);
        Vector tampung = new Vector();
        Vector tdok = new Vector();
        tampung.removeAllElements();
        tdok.removeAllElements();;
        String[] tmp = build.toString().split("\n");
        for (String a : tmp) {
            String[] t = a.split("\t");
            for (String b : t) {
                tampung.add(b);
            }
        }

        String[] tu = dapatkanDokumen.toString().split("\n");
        for (String as : tu) {
            String[] td = as.split("\t");
            for (String bt : td) {
                tdok.add(bt);
            }
        }

        stoplist(tampung, tdok, jml);

        Vector tttt = new Vector();
        System.out.println(tampung.size());
        System.out.println(tdok.size());
        int a = 0;
        float kali = 0;
        String gabung = "";
        for (int i = 0; i < tampung.size(); i++) {
            StringBuilder tfidf = new StringBuilder();
            StringBuilder pVector = new StringBuilder();
            String mm = tampung.get(i + 1).toString();
            if (i % 2 == 0) {
                try {
                    //       System.out.println(a + "\t" + tampung.get(i) + "\t" + tampung.get(i + 1));
                    float idf = (float) Math.log10((float) isiDokumen.size() / (float) jml.get(a));
                    String[] ttf = mm.split(", ");
                    tfXidf(ttf, idf, kali, tfidf, pVector);
                    splitDok(tdok, tttt, a);
                    tampilkanTFIDF(pVector, gabung, isiDokumen, tttt);
                    sqrtDoc(tampung, psqrtDOC);
                    String sql = "insert into tb_kopus values('" + 0 + "','" + tampung.get(i) + "','" + tdok.get(a) + "','" + tampung.get(i + 1) + "','" + jml.get(a) + "','" + idf + "','" + tfidf + "','" + pVector + "')";
                    java.sql.Connection con = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
                    java.sql.PreparedStatement pst = con.prepareStatement(sql);
                    pst.execute();
                } catch (NumberFormatException | SQLException e) {
                    //JOptionPane.showMessageDialog(rootPane, e);
                }
                a++;
            }
        }
    }

    public void tfXidf(String[] ttf, float idf, float kali, StringBuilder tfidf, StringBuilder pVector) {
        for (String m : ttf) {
            kali = idf * Float.parseFloat(m);
            tfidf.append(kali);
            pVector.append(Math.pow(kali, 2));
            tfidf.append(", ");
            pVector.append(", ");
        }
        tfidf.delete(tfidf.length() - 2, tfidf.length() - 1);
        pVector.delete(pVector.length() - 2, pVector.length() - 1);
    }

    public void tampilkanTFIDF(StringBuilder tfidf, String gabung, Vector isiDokumen, Vector tttt) {
        String mm = tfidf.toString();
        String ttf[] = mm.split(", ");
        gabung = "";
        for (int a = 1; a <= isiDokumen.size(); a++) {
            float jml = 0.0f;
            int b = 0;
            for (String tmp : ttf) {
                String uhu = "D" + a;
                float aa = 0;
                if (uhu.equals(tttt.get(b).toString().trim())) {
                    aa = Float.parseFloat(tmp);
                }
                jml += aa;
                b++;
            }
            gabung = gabung.concat(String.valueOf(jml) + " ");
        }
        //System.out.println(gabung);
        sqrtDoc(gabung);
    }

    public void sqrtDoc(String a) {
        int b = 0;
        String[] tmp = a.split(" ");
        for (String t : tmp) {
            tes.add(t);
        }
    }

    public void sqrtDoc(Vector a, Vector b) {
        //String gabung = "";
        b.removeAllElements();
        //System.out.println(a.size() / 2 + "\t" + tes.size());
        if (isiDokumen.size() * (a.size() / 2) == tes.size()) {
            for (int i = 0; i < isiDokumen.size(); i++) {
                float jml = 0;
                int m = 0;
                for (int j = 0; j < tes.size() / isiDokumen.size(); j++) {
                    jml += Float.parseFloat(tes.get(i + m).toString());
                    m += isiDokumen.size();
                }
                DecimalFormat df = new DecimalFormat("#.###");
                insertTBSQRT(df.format(Math.sqrt(jml)).replace(",", "."));
//                gabung = gabung.concat(df.format(Math.sqrt(jml)) + " ");
            }
//            System.out.println(gabung);
//            b.add(gabung);
        }
    }

    public void insertTBSQRT(String b) {
        try {
            int a = 0;
            String insert = "insert into tb_sqrtDoc values('" + a + "','" + b + "')";
            java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(insert);
            pst.execute();
        } catch (Exception e) {
        }
    }

    public void kosongkan() {
        try {
            int a = 0;
            String delete = "TRUNCATE TABLE tb_sqrtDoc";
            java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(delete);
            pst.execute();
        } catch (Exception e) {
        }
    }

    public void splitDok(Vector tdok, Vector banyak, int value) {
        banyak.removeAllElements();
        String mm = "";
        mm = tdok.get(value).toString();
        String ttf[] = mm.split(", ");
        for (String tmp : ttf) {
            banyak.add(tmp);
        }
        //System.out.println(banyak.size());
    }

    //proses cari
    public void tokenKeyword(Vector kk, Vector jk) {
        kk.removeAllElements();
        jk.removeAllElements();
        TF.clear();
        try {
            StringTokenizer st = new StringTokenizer(jTcari.getText().toLowerCase(), "\t\n:;,(). ");
            while (st.hasMoreTokens()) {
                int freq;
                int len = 0;
                IndonesianStemmer stema = new IndonesianStemmer();
                EnglishStemmer es = new EnglishStemmer();
                char[] tes = st.nextToken().toCharArray();
                len = stema.stem(tes, tes.length, true);
                String stt = new String(tes, 0, len);
                es.setCurrent(stt.trim());
                es.stem();
                String term = es.getCurrent();
                if (!(TF.containsKey(term))) {
                    TF.put(term, 0);
                }
                freq = TF.get(term);
                TF.put(term, freq + 1);
            }
            for (String a : TF.keySet()) {
                kk.add(a);
                jk.add(TF.get(a));
            }

        } catch (Exception e) {
        }
    }

    public void StoplistKey(Vector kk, Vector sk, Vector jk) {
        sk.removeAllElements();
        tokenKeyword(kk, jk);
        try {
            FileReader fr = new FileReader("src/stopwords.txt");
            BufferedReader br = new BufferedReader(fr);
            StringTokenizer st = new StringTokenizer(br.readLine());
            while (st.hasMoreTokens()) {
                stopword.add(st.nextToken());
            }

            for (int a = 0; a < kk.size(); a++) {
                for (int b = 0; b < stopword.size(); b++) {
                    if (kk.get(a).equals(stopword.get(b))) {
                        kk.remove(a);
                        jk.remove(a);
                    }
                }
            }

            for (int c = 0; c < kk.size(); c++) {
                Vector idf = new Vector();
                Vector pVecSet = new Vector();
//                System.out.println(kk.get(c)+"\t"+jk.get(c));                
                DecimalFormat dc = new DecimalFormat("#.###");
                idf(kk.get(c).toString(), idf);
                float tfidf = Float.parseFloat(idf.get(0).toString()) * Float.parseFloat(jk.get(c).toString());
                float pVector = (float) Math.pow(tfidf, 2);
                ambilPvector(kk.get(c).toString(), pVecSet);
                String kkdi = "";
                for (int i = 0; i < pVecSet.size(); i++) {
                    float kkxdi = pVector * Float.parseFloat(pVecSet.get(i).toString());
                    kkdi = kkdi.concat(String.valueOf(dc.format(kkxdi)) + " ");
                }
                isiTableKeyword(kk.get(c).toString(), jk.get(c).toString(), dc.format(tfidf).toString(), dc.format(pVector).toString(), kkdi);
                //System.out.println(tfidf+"\t"+pVector);
//                System.out.println(idf +"\t"+es.getCurrent());
                //System.out.println(idf.get(0));
            }
        } catch (Exception e) {
        }
    }

    public void idf(String a, Vector b) {
        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String sql = "select distinct idf from tb_kopus where kata ='" + a + "'";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                b.add(rs.getString("idf"));
                //System.out.println(b);
            }
            st.close();
            rs.close();
        } catch (Exception e) {
        }
    }

    public void ambilPvector(String a, Vector c) {
        try {
            Vector b = new Vector();
            b.removeAllElements();
            c.removeAllElements();
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String sql = "select pVector from tb_kopus where kata ='" + a + "'";
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                b.add(rs.getString("pVector"));
                //System.out.println(b);
            }
            st.close();
            rs.close();

            for (int j = 0; j < b.size(); j++) {
                String[] e = b.get(0).toString().split(", ");
                //System.out.println(a);
                for (String f : e) {
                    c.add(f);
                    //System.out.println(c.size());
                }
            }

        } catch (Exception e) {
        }

    }

    public void isiTableKeyword(String kata, String df, String tfidf, String pVector, String kkdi) {
        try {
            int a = 0;
            String insert = "insert into tb_keyword values('" + a + "','" + kata + "','" + df + "','" + tfidf + "','" + pVector.replace(",", ".") + "','" + kkdi + "')";
            java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(insert);
            pst.execute();
        } catch (Exception e) {
        }
    }

    public void hitungKKDI(String a, Vector b, Vector c) {
        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String banyak = "select dokumen from tb_kopus where kata ='" + a + "'";
            ResultSet res = st.executeQuery(banyak);
            while (res.next()) {
                b.add(res.getString("dokumen"));
            }
        } catch (Exception e) {
        }

        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String banyak = "select kkdi as aaa from tb_keyword where kata ='" + a + "'";
            ResultSet res = st.executeQuery(banyak);
            while (res.next()) {
                c.add(res.getString("aaa"));
            }
        } catch (Exception e) {
        }

    }

    public void hitungAkhir(Vector kkdi, Vector hkkdi) {
        Vector hasil = new Vector();
        Vector hasilDoc = new Vector();
        hasil.removeAllElements();
        hasilDoc.removeAllElements();
        kkid.clear();
        System.out.println(hkkdi.size());
        for (int i = 0; i < hkkdi.size(); i++) {
            String tampungan[] = hkkdi.get(i).toString().split(" ");
            String tamp[] = kkdi.get(i).toString().split(", ");
            for (String term : tamp) {
                hasilDoc.add(term);
            }

            for (String st : tampungan) {
                hasil.add(st);
            }
        }
        //System.out.println(hasil.size() + "\t" + hasilDoc.size());

        for (int j = 0; j < hasilDoc.size(); j++) {
            try {
                float freq, fre;
                //System.out.println(hasilDoc.get(j));
                String term = hasilDoc.get(j).toString();
                String nilai = hasil.get(j).toString().replace(",", ".");
                fre = Float.parseFloat(nilai);
                //System.out.println(j);
                if (!(kkid.containsKey(term))) {
                    kkid.put(term, fre);
                } else {
                    freq = kkid.get(term);
                    kkid.put(term, freq + fre);
                }

            } catch (Exception e) {
            }
        }
    }

    public void sumKK(Vector b) {
        try {
            Statement state = koneksi_database.koneksi.koneksiDB().createStatement();
            String sum = "select sum(pVector) from tb_keyword";
            ResultSet res = state.executeQuery(sum);
            while (res.next()) {
                b.add(res.getString("sum(pVector)"));
            }
        } catch (Exception e) {
        }
    }

    public void cosine(Vector doc, Vector nilai, Vector sum) {
        Vector jum = new Vector();
        jum.removeAllElements();
        DecimalFormat df = new DecimalFormat("#.#####");
        try {
            int a = 0;
            for (int i = 0; i < doc.size(); i++) {
                tampilkanSqrtDOK(doc.get(i).toString().replace("D", "").trim(), jum);
                float total = Float.parseFloat(nilai.get(i).toString()) / (Float.parseFloat(sum.get(0).toString()) * Float.parseFloat(jum.get(0).toString()));
                String insert = "insert into tb_hasilCari values('" + a + "','" + df.format(Math.cos(Math.toRadians(total))) + "','" + doc.get(i).toString().replace("D", "").trim() + "')";
                java.sql.Connection con = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
                java.sql.PreparedStatement pst = con.prepareStatement(insert);
                pst.execute();
            }
        } catch (Exception e) {
        }
    }

    public void tampilkanSqrtDOK(String id, Vector jml) {
        try {
            Statement sta = koneksi_database.koneksi.koneksiDB().createStatement();
            String tampil = "Select jumlah from tb_sqrtDoc where id_sqrtDoc=" + id;
            ResultSet res = sta.executeQuery(tampil);
            while (res.next()) {
                jml.add(res.getString("jumlah"));
            }
            res.close();
            sta.close();
        } catch (Exception e) {
        }
    }

    public void rangking(Vector a, Vector b) {
        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String urutkan = "Select nilai,dok from tb_hasilCari order by nilai desc";
            ResultSet res = st.executeQuery(urutkan);
            while (res.next()) {
                a.add(res.getString("nilai"));
                b.add(res.getString("dok"));
            }
            res.close();
            st.close();
        } catch (Exception e) {
        }
    }

    public void tampilkanHasil(Vector a) {
        try {
            DefaultListModel dlm = new DefaultListModel();
            Vector b = new Vector();
            b.removeAllElements();
            for (int i = 0; i < a.size(); i++) {
                alamatFile(a.get(i).toString().trim(), b);
            }

            for (int j = 0; j < b.size(); j++) {
                dlm.addElement(b.get(j).toString().trim());
                jListHasil.setModel(dlm);
            }
        } catch (Exception e) {
        }
    }

    public void alamatFile(String a, Vector b) {
        try {
            Statement st = koneksi_database.koneksi.koneksiDB().createStatement();
            String sql = "select url from tb_sumber where id_sumber =" + a;
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                b.add(res.getString("url"));
            }
            res.close();
            st.close();
        } catch (Exception e) {
        }
    }

    public void kosong() {
        try {
            String delete = "TRUNCATE TABLE tb_hasilCari";
            java.sql.Connection con = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = con.prepareStatement(delete);
            pst.execute();
        } catch (Exception e) {
        }
    }

    public menuUtama() {
        initComponents();
        this.setLocationRelativeTo(this);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTcari = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListData = new javax.swing.JList();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListHasil = new javax.swing.JList();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Search");

        jScrollPane2.setViewportView(jListData);

        jButton1.setText("Load");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Cari");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jListHasil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListHasilMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jListHasil);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jTcari, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton2)
                .addGap(44, 44, 44))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTcari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addGap(42, 42, 42)
                .addComponent(jButton1)
                .addContainerGap())
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Tambah 1 File");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Tambah File 1 Folder");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("About");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        JFileChooser load_file = new JFileChooser();
        int jes = load_file.showOpenDialog(this);
        if (jes == JFileChooser.APPROVE_OPTION) {
            File f = load_file.getSelectedFile();
            nama.add(load_file.getName(f));
            String sumber = f.getPath().replace("\\", "/");
            DefaultListModel dlm = new DefaultListModel();
            dlm.addElement(sumber);
            jListData.setModel(dlm);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Pilih Directori");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            tfolder = chooser.getSelectedFile();
            String sumber = tfolder.toString().replace("\\", "/");
            tmp = tfolder.listFiles();
            DefaultListModel dlm = new DefaultListModel();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isFile()) {
                    dlm.addElement(sumber + "/" + tmp[i].getName());
                    jListData.setModel(dlm);
                    nama.add(tmp[i].getName());
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Tidak Ada Folder Yang Dipilih");
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        try {
            tambahSumber();
            kosongkan();
            kosongkanTable();
            tokenKata();
            //System.out.print(jListData.getModel().getSize());
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        DefaultListModel dlm = new DefaultListModel();
        dlm.addElement(null);
        jListHasil.setModel(dlm);
        try {
            String kosong = "TRUNCATE TABLE tb_keyword";
            java.sql.Connection conn = (java.sql.Connection) koneksi_database.koneksi.koneksiDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(kosong);
            pst.execute();
        } catch (Exception e) {
        }
        kosong();
        Vector tmkkNilai = new Vector();
        Vector tmkkDoc = new Vector();
        Vector sumKK = new Vector();
        Vector kataKunci = new Vector();
        Vector jmlKK = new Vector();
        Vector stoplisKK = new Vector();
        Vector tmpKKID = new Vector();
        Vector tmpHKKID = new Vector();
        Vector tA = new Vector();
        Vector tampAkhir = new Vector();
        tampAkhir.removeAllElements();
        tA.removeAllElements();
        sumKK.removeAllElements();
        tmkkNilai.removeAllElements();
        tmkkDoc.removeAllElements();
        kataKunci.removeAllElements();
        jmlKK.removeAllElements();
        stoplisKK.removeAllElements();
        StoplistKey(kataKunci, stoplisKK, jmlKK);
        for (int i = 0; i < kataKunci.size(); i++) {
            hitungKKDI(kataKunci.get(i).toString(), tmpKKID, tmpHKKID);
        }
        hitungAkhir(tmpKKID, tmpHKKID);

        for (String a : kkid.keySet()) {
            tmkkDoc.add(a);
            tmkkNilai.add(kkid.get(a));
            //System.out.println(a + "\t" + kkid.get(a));
            //System.out.println(kkid.get(a));
        }
        sumKK(sumKK);
        cosine(tmkkDoc, tmkkNilai, sumKK);
        rangking(tampAkhir, tA);
        tampilkanHasil(tA);
        //System.out.println(tmpKKID + "\t" + tmpHKKID + "\t" + tA);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jListHasilMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListHasilMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            try {
                File myFile = new File(jListHasil.getSelectedValue().toString());
                Desktop.getDesktop().open(myFile);
            } catch (IOException e) {
            }

        }
    }//GEN-LAST:event_jListHasilMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(menuUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(menuUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(menuUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(menuUtama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new menuUtama().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jListData;
    private javax.swing.JList jListHasil;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTcari;
    // End of variables declaration//GEN-END:variables
}
