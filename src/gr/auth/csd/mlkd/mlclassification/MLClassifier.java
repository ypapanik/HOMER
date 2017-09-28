/*
 * Copyright (C) 2015 Yannis Papanikolaou
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.auth.csd.mlkd.mlclassification;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.preprocessing.Corpus;
import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Document;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.utils.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yannis Papanikolaou
 */
public abstract class MLClassifier {

    protected int threads;
    protected double[][] predictions;
    protected int numLabels = 0;
    protected Dictionary dictionary = null;
    protected Labels globalLabels = null;
    protected Corpus corpus2;
    protected Corpus corpus;
    protected String[] docMap = null;
    public String bipartitionsFile = "bipartitions";
    public String testFile;
    protected TreeMap<String, THashSet<String>> bipartitions = new TreeMap<>();
    protected String trainingFile;
    protected int offset = 0;
    protected String testFilelibSVM = "testFile.libSVM";
    protected int numFeatures;
    protected String predictionsFilename = "predictions";

    public MLClassifier(String trainingFile, String testFile, String dic, String labels, int threads) {
        this((dic == null) ? null : Dictionary.readDictionary(dic), (labels == null) ? null : Labels.readLabels(labels),
                ((trainingFile != null) ? new CorpusJSON(trainingFile) : null),
                ((testFile != null) ? new CorpusJSON(testFile) : null), threads);
        this.trainingFile = trainingFile;
        this.testFile = testFile;
    }

    public MLClassifier(String trainingFile, String testFile, Dictionary dictionary, Labels labels, int threads) {
        this(dictionary, labels, ((trainingFile != null) ? new CorpusJSON(trainingFile) : null),
                ((testFile != null) ? new CorpusJSON(testFile) : null), threads);
        this.trainingFile = trainingFile;
        this.testFile = testFile;
    }

    public MLClassifier(Dictionary dictionary, Labels labels, Corpus trainingCorpus,
            Corpus testCorpus, int threads) {
        corpus = trainingCorpus;
        corpus2 = testCorpus;
        this.dictionary = dictionary;
        this.globalLabels = labels;
        if (labels != null) {
            numLabels = labels.getSize();
        }
        if (dictionary != null) {
            numFeatures = dictionary.getId().size();
        }
        if (testCorpus != null) {
            bipartitionsFile = "bipartitions";
            createDocMap();
        }
        this.threads = threads;
    }

    protected void createDocMap() {
        Document doc;
        int id = 0;
        int size = CorpusJSON.size(corpus2);
        docMap = new String[size];
        corpus2.reset();
        while ((doc = corpus2.nextDocument()) != null) {
            docMap[id] = doc.getId();
            id++;
        }
    }

    public void bipartitionsWrite(String bipartitionsFile) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(bipartitionsFile)))) {
            Iterator<Map.Entry<String, THashSet<String>>> it = bipartitions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, THashSet<String>> next = it.next();
                Set<String> pred = new TreeSet<>();
                pred.addAll(next.getValue());
                writeToFile(writer, pred, next.getKey());
            }
        } catch (Exception ex) {
            Logger.getLogger(MLClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void bipartitionsWrite(String bipartitionsFile, ArrayList<TObjectDoubleHashMap<String>> mostRelevantLabels) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(bipartitionsFile)))) {
            for (int i = 0; i < mostRelevantLabels.size(); i++) {
                String pmid = this.docMap[i];
//                if(i==0) System.out.println(bipartitions.get(pmid));              
                bipartitions.get(pmid).retainAll(mostRelevantLabels.get(i).keySet());
//                if(i==0) System.out.println(bipartitions.get(pmid)); 
            }
            Iterator<Map.Entry<String, THashSet<String>>> it = bipartitions.entrySet().iterator();
            int i = 0;
            while (it.hasNext()) {
                Map.Entry<String, THashSet<String>> next = it.next();
                Set<String> pred = new TreeSet<>();
                pred.addAll(next.getValue());
                writeToFile(writer, pred, next.getKey());
                i++;
            }
        } catch (Exception ex) {
            Logger.getLogger(MLClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void writeToFile(PrintWriter writer, Set<String> pred, String doc) {
        writer.write(doc + ": ");
        String[] pred2 = pred.toArray(new String[0]);
        for (int i = 0; i < pred2.length - 1; i++) {
            writer.write(pred2[i] + "; ");
        }
        if (pred2.length != 0) {
            writer.write(pred2[pred2.length - 1] + "\n");
        } else {
            writer.write("\n");
        }
    }

    public TreeMap<String, THashSet<String>> predict(TIntHashSet mc) {
        predictInternal(mc);
        savePredictions();
        //create bipartitions
        createBipartitions();
        return bipartitions;

    }
    public void createBipartitions() {
        //System.out.println( docMap.length+" "+predictions.length);//predictions[0].length);
        for (int doc = 0; doc < predictions.length; doc++) {
            String pmid = docMap[doc];
            if (!bipartitions.containsKey(pmid)) {
                bipartitions.put(pmid, new THashSet<>());
            }
            for (int label = 0; label < predictions[doc].length; label++) {
                if (predictions[doc][label] > 0) {
                    bipartitions.get(pmid).add(globalLabels.getLabel(label + 1));
                }
            }
        }
        //System.out.println(bipartitions.size());
    }

    public void setBipartitions(TreeMap<String, THashSet<String>> bipartitions) {
        this.bipartitions = bipartitions;
    }

    public abstract void train();

    public abstract double[][] predictInternal(TIntHashSet mc);

    protected void writeProbs(TreeMap<String, TObjectDoubleHashMap<String>> probMap, String scorestxt) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(scorestxt)))) {
            Iterator<Map.Entry<String, TObjectDoubleHashMap<String>>> it = probMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, TObjectDoubleHashMap<String>> next = it.next();
                //System.out.println(next.getValue());
                TObjectDoubleIterator<String> it2 = next.getValue().iterator();
                TreeMap<Integer, Double> ordered = new TreeMap<>();
                while (it2.hasNext()) {
                    it2.advance();
                    ordered.put(Integer.parseInt(it2.key()), it2.value());
                }
                StringBuilder sb = new StringBuilder();
                int i = 0;
                Iterator<Map.Entry<Integer, Double>> it3 = ordered.entrySet().iterator();
                while (it3.hasNext()) {
                    Map.Entry<Integer, Double> n = it3.next();
                    sb.append(n.getKey() - 1).append(":").append(Math.round(n.getValue() * 1000000.0) / 1000000.0);
                    if (i < ordered.size() - 1) {
                        sb.append(" ");
                    }
                    i++;
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(MLClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void savePredictions() {
        double p2[][] = new double[predictions.length][];
        for (int doc = 0; doc < predictions.length; doc++) {
            p2[doc] = Arrays.copyOf(predictions[doc], predictions[0].length);
        }
        ArrayList<TIntDoubleHashMap> p = new ArrayList<>();
        for (int doc = 0; doc < p2.length; doc++) {
            TIntDoubleHashMap preds = new TIntDoubleHashMap();
            if (100 > predictions[0].length) {
                for (int k = 0; k < predictions[0].length; k++) {
                    if(p2[doc][k]!=0) preds.put(k, p2[doc][k]);
                }
            } else {
                for (int k = 0; k < 100; k++) {
                    int label = Utils.maxIndex(p2[doc]);
                    if(p2[doc][label]!=0) preds.put(label, p2[doc][label]);
                    p2[doc][label] = -1;
                }
            }
            p.add(doc, preds);
        }
        Utils.writeObject(p, predictionsFilename);
    }
}
