package gr.auth.csd.mlkd.preprocessing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VectorizeMultiLabelLibSVM extends VectorizeJSON {

    public VectorizeMultiLabelLibSVM(Dictionary d, boolean zoning, Labels labels) {
        super(d, zoning, labels);
    }

    protected void vectorizeLabeled(Corpus corpus, String libsvmFilename) {
        ArrayList<String> docMap = new ArrayList<>();
        try (BufferedWriter output = Files.newBufferedWriter(Paths.get(libsvmFilename), Charset.forName("UTF-8"))) {
            List<String> lines;
            Document doc;
            int document = 0;
            int corpusSize = CorpusJSON.size(corpus);
            StringBuilder sb = new StringBuilder();
            sb.append(corpusSize).append(" ").append(dictionary.getId().size()).append(" ").append(this.labels.getSize()).append("\n");
            output.write(sb.toString());
            corpus.reset();
            while ((doc = corpus.nextDocument()) != null) {
                document++;
                docMap.add(doc.getId());
                lines = doc.getContentAsSentencesOfTokens(false);
                Map<Integer, Double> vector = vectorize(lines, true, doc);
                if (vector != null) {
                    // output features in shell libsvm file
                    Iterator<Map.Entry<Integer, Double>> values = vector.entrySet().iterator();
                    sb = new StringBuilder();
                    //labels
                    HashSet<Integer> labelIds = new HashSet<>();
                    Set<String> meshTerms = doc.getLabels();
                    for (String term : meshTerms) {
                        int id = labels.getIndex(term);
                        if (id != -1) {
                            labelIds.add(id);
                        }

                    }
                    int l = 0;
                    for (Integer id : labelIds) {
                        sb.append(id - 1);
                        if (l < labelIds.size() - 1) {
                            sb.append(",");
                        }
                        l++;
                    }
                    //features
                    while (values.hasNext()) {
                        Map.Entry<Integer, Double> entry = values.next();
                        if (entry.getValue() != 0) {
                            sb.append(" ");
                            sb.append(entry.getKey()).append(":").append(String.format(Locale.US, "%.6f", entry.getValue()));
                        }
                    }
                    sb.append("\n");
                    output.write(sb.toString());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(VectorizeMultiLabelLibSVM.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (String doc : docMap) {
                sb.append(doc).append("\n");
            }
            Files.write(Paths.get("docMap.txt"), sb.toString().getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Labels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void transfrom(String inTrain, String inTest, String outTrain, String outTest) {
        Corpus corpus = new CorpusJSON(inTrain);
        Corpus corpus2 = new CorpusJSON(inTest);
        vectorizeLabeled(corpus, outTrain);
        vectorizeLabeled(corpus2, outTest);
    }
}
