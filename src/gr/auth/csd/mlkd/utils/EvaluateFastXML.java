package gr.auth.csd.mlkd.utils;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yannis Papanikolaou
 */
public class EvaluateFastXML {

    int numLabels;
    private final int rcut;
    protected String filenamePredictions;
    protected HashMap<Integer, TreeSet<Integer>> bipartitions = new HashMap<>();
    TIntObjectHashMap<TreeSet<Integer>> truth;

    public EvaluateFastXML(String folderTest, String filenamePredictions, int numTags, int rcut) {
        numLabels = numTags;
        this.filenamePredictions = filenamePredictions;
        truth = readTruth(folderTest);
        this.rcut = rcut;
        this.evaluate();

    }

    public void evaluate() {
        readBipartitions(rcut);
        //load predicted labels
        double[] tp, fp, tn, fn;
        tp = new double[numLabels];
        fp = new double[numLabels];
        tn = new double[numLabels];
        fn = new double[numLabels];

        double df = 0;

        for (int doc = 0; doc < bipartitions.size(); doc++) {
            TreeSet<Integer> t = truth.get(doc);
            TreeSet<Integer> pred = bipartitions.get(doc);
            //System.out.println(doc+" "+t+" "+pred);
            ConfMatrix cm = new ConfMatrix(pred, t, tp, fn, fp, tn, numLabels);
        }

        double macroF = 0;
        double tpa = 0;
        double fpa = 0;
        double tna = 0;
        double fna = 0;
        for (int i = 0; i < numLabels; i++) {
            //System.out.print("Label " + labels.getLabel(i + 1) + " " + (i + 1) + " ");
            //System.out.printf("tp %.0f ", tp[i]);
            tpa += tp[i];
            //System.out.printf("fp %.0f ", fp[i]);
            fpa += fp[i];
            //System.out.printf("tn %.0f ", tn[i]);
            tna += tn[i];
            //System.out.printf("fn %.0f ", fn[i]);
            fna += fn[i];
            double f = 2.0 * tp[i] / (2.0 * tp[i] + fp[i] + fn[i]);
            if (new Double(f).isNaN()) {
                f = 1;
            }
            macroF += f;
            //System.out.printf("f %.2f", f);
            //System.out.println("");
        }

        /*System.out.println(
         "F: " + df / corpus.getCorpusSize());
         */ System.out.println(
                "MacroF: " + macroF / numLabels);
        double microF = 2.0 * tpa / (2.0 * tpa + fpa + fna);

        System.out.println(tpa + ", " + fpa + ", " + fna);
        System.out.println(
                "MicroF: " + microF);

    }
    
    protected void readBipartitions(int rcut) {
        try (BufferedReader br = new BufferedReader(new FileReader(filenamePredictions))) {
            String line;
            int d = 0;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                TIntDoubleHashMap predictionsPerDoc = new TIntDoubleHashMap();
                //read
                String[] labelscores = line.split(" ");
                //if(d==0) System.out.println(d+" "+Arrays.toString(labelscores));
                for (String ls : labelscores) {
                    String[] split = ls.split(":");
                    int l = Integer.parseInt(split[0]);
                    double score = Double.parseDouble(split[1]);
                    predictionsPerDoc.put(l, score);
                }
                //write
                TreeSet<Integer> preds = new TreeSet<>();
                for (int i = 0; i < rcut; i++) {
                    int maxIndex = Utils.maxIndex(predictionsPerDoc);
                    //if(d==0) System.out.println(maxIndex+" "+predictionsPerDoc.get(maxIndex));
                    predictionsPerDoc.put(maxIndex, Double.MIN_VALUE);
                    preds.add(maxIndex);
                }
                //if(d==0) System.out.println(d+" "+preds);
                bipartitions.put(d, preds);
                d++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected TIntObjectHashMap<TreeSet<Integer>> readTruth(String folderTest) {
        TIntObjectHashMap<TreeSet<Integer>> tru = new TIntObjectHashMap<>();
        try (final BufferedReader reader = new BufferedReader(new FileReader(folderTest))) {
            String line;
            int doc = 0;
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                TreeSet<Integer> truth = new TreeSet<>();
                String[] split = line.split(" ");
                String[] tags = split[0].split(",");
                if (tags.length != 0&&!tags[0].isEmpty()) {
                    for (String label : tags) {
                        truth.add(Integer.parseInt(label));
                    }
                }
                tru.put(doc, truth);
                doc++;
            }
        } catch (IOException ex) {
            Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tru;
    }

    public static void main(String[] args) {
        String folderTest = args[0];
        String filenamePredictions = args[1];
        int numTags = Integer.parseInt(args[2]);
        int rcut = Integer.parseInt(args[3]);
        EvaluateFastXML ev = new EvaluateFastXML(folderTest, filenamePredictions, numTags, rcut);
    }

}
