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
package gr.auth.csd.mlkd.examples;

import gr.auth.csd.mlkd.utils.MicroAndMacroFLabelPivoted;
import gr.auth.csd.mlkd.mlclassification.homer.ClusteringDataset;
import gr.auth.csd.mlkd.mlclassification.homer.Homer;
import gr.auth.csd.mlkd.mlclassification.homer.HomerCmdOption;
import gr.auth.csd.mlkd.mlclassification.homer.Tree;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.Dbscan;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.Hierarchical;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.Optics;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.RecursiveBalancedKMeans;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.RecursiveKMeans;
import gr.auth.csd.mlkd.mlclassification.homer.clusterer.RecursiveLabelClustering;
import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.utils.Timer;
import java.io.FileNotFoundException;

/**
 *
 * @author Yannis Papanikolaou
 */
public class HomerExample {

    public static void main(String args[]) throws FileNotFoundException {
        Timer timer = new Timer();
        HomerCmdOption option = new HomerCmdOption(args);

        String cm = option.vectorMethod;
        String clusterer = option.clusteringMethod;
        String hmethod = option.hierarchicalMethod;
        Homer.baseClassifier = option.classifier;
        double epsilon = option.epsilon;

        String df = option.distanceFunction;
        CorpusJSON trainingCorpus = new CorpusJSON(option.trainingFile);
        CorpusJSON testCorpus = null;
        if (option.testFile != null) {
            testCorpus = new CorpusJSON(option.testFile);
        }

        Labels labels = new Labels(trainingCorpus);
        Dictionary dictionary = new Dictionary(trainingCorpus, option.lowUnigrams, option.highUnigrams,
                option.lowBigrams, option.highBigrams);
        System.out.println(timer.duration());
        dictionary.writeDictionary(option.dictionary);
        labels.writeLabels(option.labels);

        ClusteringDataset cd = createHierarchy(cm, option, args, labels, clusterer, hmethod, df, epsilon);
        Homer homer = new Homer((HomerCmdOption) option);
        //Homer homer = new Homer((HomerCmdOption) option, dictionary, labels, trainingCorpus, testCorpus);


        homer.train();
        homer.predict(null);
        homer.bipartitionsWrite(option.bipartitionsFile);
        homer.finalCleanup(option);

        MicroAndMacroFLabelPivoted ev = new MicroAndMacroFLabelPivoted(labels, new CorpusJSON(option.testFile), option.bipartitionsFile);
        ev.evaluate();
    }

    private static ClusteringDataset createHierarchy(String cm, HomerCmdOption option, String[] args,
            Labels labels, String clusterer, String hmethod, String df, double epsilon) throws FileNotFoundException {
        ClusteringDataset cd;
        cd = new ClusteringDataset(labels, option.trainingFile, option.labels);

        String libSVMFile = cd.writeToFile(option.trainingFile, null);

        System.out.println(clusterer);
        RecursiveLabelClustering cc;

        switch (clusterer) {
            case "kmeans":
                cc = new RecursiveKMeans(labels, option.maxClusterSize, option.numOfClusters, df);
                break;
            case "optics":
                cc = new Optics(labels, option.maxClusterSize, epsilon, df);
                break;
            case "hierarchical":
                cc = new Hierarchical(labels, option.maxClusterSize, option.numOfClusters, hmethod, df);
                break;
            default:
                cc = new Dbscan(labels, 3, epsilon, df);
                break;
        }
//NOT WORKING        cc = new Clique(cd.getLabels(), option.maxClusterSize, option.numOfClusters);
        cc.recursion(true, cc.hierarchy.getRoot(), libSVMFile, cd, new CorpusJSON(option.trainingFile));
        cc.hierarchy.writeTree(option.treeFile);
        cc.hierarchy.getRoot().print(cc.labels);
        System.out.println("The hierarchy has " + Tree.getNumberOfNodes() + " nodes.");
        return cd;
    }
}
