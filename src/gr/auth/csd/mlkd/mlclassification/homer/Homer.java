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
package gr.auth.csd.mlkd.mlclassification.homer;

import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.utils.CmdOption;
import gr.auth.csd.mlkd.mlclassification.BinaryClassifier;
import gr.auth.csd.mlkd.mlclassification.svm.BinaryRelevanceSVM;
import gr.auth.csd.mlkd.mlclassification.MLClassifier;
import gr.auth.csd.mlkd.mlclassification.svm.SVM;
import gr.auth.csd.mlkd.preprocessing.Parser;
import gr.auth.csd.mlkd.preprocessing.Corpus;
import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Document;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author Yannis Papanikolaou
 */
public class Homer extends MLClassifier {

    Tree hierarchy = null;
    private CorpusJSON entireTrainingCorpus = null;
    CorpusJSON entireTestCorpus = null;
    final String modelsDirectory;
    Labels globalLabels;
    final String metaLabelerFile;
    public static String baseClassifier = "BR";
    CmdOption option;
    private final boolean eval = false;

    public Homer(HomerCmdOption option) {
        super(option.trainingFile, option.testFile, option.dictionary, option.labels, option.threads);
        modelsDirectory = option.modelsDirectory;
        this.globalLabels = Labels.readLabels(option.labels);
        this.metaLabelerFile = option.metalabelerFile;
        this.option = option;
        hierarchy = Tree.readTree(option.treeFile);
        this.entireTrainingCorpus = new CorpusJSON(option.trainingFile);
        this.entireTestCorpus = new CorpusJSON(option.testFile);
        this.testFile = option.testFile;

    }

    public Homer(HomerCmdOption option, Dictionary dic, Labels labels) {
        super(option.trainingFile, option.testFile, dic, labels, option.threads);
        this.globalLabels = labels;
        modelsDirectory = option.modelsDirectory;
        this.metaLabelerFile = option.metalabelerFile;
        this.option = option;
        hierarchy = Tree.readTree(option.treeFile);
        if (option.trainingFile != null) {
            this.entireTrainingCorpus = new CorpusJSON(option.trainingFile);
        }
        if (option.testFile != null) {
            this.entireTestCorpus = new CorpusJSON(option.testFile);
        }
        this.trainingFile = option.trainingFile;
        this.testFile = option.testFile;
    }

    public Homer(HomerCmdOption option, Dictionary dic, Labels labels, Corpus trainingCorpus, Corpus testCorpus) {
        super(dic, labels, trainingCorpus, testCorpus, option.threads);
        this.globalLabels = labels;
        this.entireTrainingCorpus = (CorpusJSON) corpus;
        this.entireTestCorpus = (CorpusJSON) corpus2;
        this.modelsDirectory = option.modelsDirectory;
        this.metaLabelerFile = option.metalabelerFile;
        this.option = option;
        hierarchy = Tree.readTree(option.treeFile);
        this.trainingFile = option.trainingFile;
        this.testFile = option.testFile;
    }
    public CorpusJSON createJSONTrainingDataset(Node n, CorpusJSON entireFile, String outputFile) {

        Document doc;
        TreeSet<Document> docList = new TreeSet<>();
        entireFile.reset();
        TIntHashSet labels = (TIntHashSet) n.getData();
        List<Node> children = n.getChildren();
        while ((doc = entireFile.nextDocument()) != null) {
            THashSet<String> newDocLabels = new THashSet<>();
            boolean add = false;
            for (String label : doc.getLabels()) {
                int index = globalLabels.getIndex(label);
                if (labels.contains(index)) {
                    if (!n.isLeaf()) {
                        for (Node child : children) {
                            if (((TIntHashSet) child.getData()).contains(index)) {
                                newDocLabels.add("L" + child.getId());
                            }
                        }
                    } else {
                        newDocLabels.add(label);
                    }
                    add = true;
                }
            }
            if (add) {
                doc.setLabels(newDocLabels);
                docList.add(doc);
            }
        }
        return writeFile(outputFile, docList);
    }

    public CorpusJSON createJSONTestDatasetForEvaluation(Node n, CorpusJSON testFile, String outputFile) {

        Document doc;
        TreeSet<Document> docList = new TreeSet<>();
        testFile.reset();
        TIntHashSet labels = (TIntHashSet) n.getData();
        List<Node> children = n.getChildren();
        while ((doc = testFile.nextDocument()) != null) {
            THashSet<String> newDocLabels = new THashSet<>();
            for (String label : doc.getLabels()) {
                int index = globalLabels.getIndex(label);
                if (labels.contains(index)) {
                    if (!n.isLeaf()) {
                        for (Node child : children) {
                            if (((TIntHashSet) child.getData()).contains(index)) {
                                newDocLabels.add("L" + child.getId());
                            }
                        }
                    } else {
                        newDocLabels.add(label);
                    }
                }
            }
            doc.setLabels(newDocLabels);
            docList.add(doc);
        }
        return writeFile(outputFile, docList);
    }

    public CorpusJSON createJSONTestDataset(CorpusJSON entireFile, String outputFile, THashSet<String> pmids) {
        Document doc;
        TreeSet<Document> docList = new TreeSet<>();
        entireFile.reset();
        while ((doc = entireFile.nextDocument()) != null) {
            if (pmids.contains(doc.getId())) {
                docList.add(doc);
            }

        }
        return writeFile(outputFile, docList);
    }

    public CorpusJSON writeFile(String outputFile, TreeSet<Document> docList) {
        JsonFactory jfactory = new JsonFactory();
        try (JsonGenerator jGenerator = jfactory.createJsonGenerator(new File(outputFile), JsonEncoding.UTF8)) {
            jGenerator.writeStartObject();
            jGenerator.writeFieldName("documents");
            jGenerator.writeStartArray();
            Iterator<Document> it = docList.iterator();
            while (it.hasNext()) {
                Document doc = it.next();
                THashSet<String> ls = new THashSet<>();
                for (String l : doc.getLabels()) {
                    ls.add(l);
                }
                Parser.write(jGenerator, doc.getId(), doc.getTitle(), doc.getAbs(), Integer.toString(doc.getYear()), doc.getJournal(), ls, doc.getBody());
            }

            jGenerator.writeEndArray();
            jGenerator.writeEndObject();
        } catch (Exception ex) {
            Logger.getLogger(Homer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new CorpusJSON(outputFile);
    }

    public void trainPerNode(Node n) {
        int id = n.getId();

        CorpusJSON training = createJSONTrainingDataset(n, entireTrainingCorpus, trainingFile + id);
        int size = CorpusJSON.size(training);
        System.out.println("Training node #" + id + " corpus size:" + size + ", leaf:" + n.isLeaf());
        if (size == 0) {
            return;
        }
        Labels labelsPerNode = new Labels(training);
        n.setMetaLabels(labelsPerNode);
//        Dictionary dictionaryPerNode = new Dictionary(training, option.lowUnigrams, option.highUnigrams,
//                option.lowBigrams, option.highBigrams);
        n.setDictionary(dictionary/*PerNode*/);
        MLClassifier mlc = new BinaryRelevanceSVM(n.getDictionary(), n.getMetaLabels(), training, null, modelsDirectory + id, threads, false);
        mlc.train();

        cleanup(option);

        List<Node> children = n.getChildren();

        if (n.isLeaf()) {
            return;
        }
        for (Node child : children) {
            trainPerNode(child);
        }
    }

    public void predictPerNode(Node n, CorpusJSON testSet) {
        //System.out.println("Node "+n.getId()+"predicting, labelsSet:"+n.getMetaLabels().getLabels().toString());
        MLClassifier mlc;
        int size = CorpusJSON.size(testSet);
        if (size == 0) {
            return;
        }
        mlc = new BinaryRelevanceSVM(n.getDictionary(), n.getMetaLabels(), null, testSet, modelsDirectory + n.getId(), threads, false);
        BinaryClassifier.setPredictions(null);
        SVM.setTest(null);
        TreeMap<String, THashSet<String>> bipartitionsPerNode = mlc.predict(null);
        if (eval) {
            if (!n.isLeaf()) {
                System.out.println("Node#" + n.getId() + " depth:" + n.getDepth() + " silhouette:" + n.getSilhouette());
            }
            createJSONTestDatasetForEvaluation(n, testSet, "t");
            mlc.bipartitionsWrite(option.bipartitionsFile);
        }
        cleanup(option);

        if (n.isLeaf()) {
            Iterator<Map.Entry<String, THashSet<String>>> it = bipartitionsPerNode.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, THashSet<String>> next = it.next();
                String doc = next.getKey();
                THashSet<String> predictedLabelsPerNode = next.getValue();
                if (bipartitions.containsKey(doc)) {
                    bipartitions.get(doc).addAll(predictedLabelsPerNode);
                } else {
                    bipartitions.put(doc, predictedLabelsPerNode);
                }
            }
        } else {
            for (int label = 0; label < n.getMetaLabels().getSize(); label++) {
                String metaLabel = n.getMetaLabels().getLabel(label + 1);
                THashSet<String> docs = new THashSet<>();
                Iterator<Map.Entry<String, THashSet<String>>> it = bipartitionsPerNode.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, THashSet<String>> next = it.next();
                    String doc = next.getKey();
                    if (next.getValue().contains(metaLabel)) {
                        docs.add(doc + "");
                    }
                }
                Node child = (Node) hierarchy.getNodeMap().get(metaLabel);
                CorpusJSON childTestSet = createJSONTestDataset(entireTestCorpus, testFile + child.getId(), docs);
                predictPerNode(child, childTestSet);
            }
        }
        cleanup(option);
        //System.out.println(bipartitions.toString());
    }

    @Override
    public void train() {
        Node root = hierarchy.getRoot();
        trainPerNode(root);
        System.out.println("Writing updated tree...");
        hierarchy.writeTree(((HomerCmdOption) option).treeFile);
    }

    @Override
    public TreeMap<String, THashSet<String>> predict(TIntHashSet mc) {
        for (int doc = 0; doc < docMap.length; doc++) {
            bipartitions.put(docMap[doc], new THashSet<String>());
        }
        Node root = hierarchy.getRoot();
        predictPerNode(root, entireTestCorpus);
        return bipartitions;
    }

    protected void cleanup(CmdOption option) {
        //cleanup
        File toDelete = new File(option.fileTrainLibsvm);
        boolean a = toDelete.delete();
        toDelete = new File(option.fileTrainLabels);
        boolean b = toDelete.delete();
        toDelete = new File(option.fileMetaTrainLabels);
        boolean c = toDelete.delete();
        toDelete = new File(option.testFilelibSVM);
        boolean d = toDelete.delete();

        if (a && b && c && d) {
            System.out.println("files deleted.");
        }
    }

    public void finalCleanup(CmdOption option) {
        boolean deleted = true;
        Node root = hierarchy.getRoot();
        if (cleanupPerNode(root)) {
            System.out.println("Cleanup completed successfully!");
        }
//        File toDelete = new File(option.dictionary);
//        boolean e = toDelete.delete();
//        toDelete = new File(option.labels);
//        boolean f = toDelete.delete();
//        toDelete = new File(option.predictionsFile);
//        boolean g = toDelete.delete();
    }

    protected boolean cleanupPerNode(Node n) {
        File toDelete = new File(testFile + n.getId());
        File toDelete2 = new File(trainingFile + n.getId());

        boolean cleanupPerNode = false;
        if (!n.isLeaf()) {
            for (Node child : ((List<Node>) n.getChildren())) {
                cleanupPerNode = cleanupPerNode(child);
            }
        } else {
            cleanupPerNode = true;
        }
        return toDelete.delete() & toDelete2.delete() & cleanupPerNode;
    }

    @Override
    public void createBipartitions() {
    }

    @Override
    public double[][] predictInternal(TIntHashSet mc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
