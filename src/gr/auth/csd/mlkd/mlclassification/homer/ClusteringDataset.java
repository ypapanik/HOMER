/*
 * Copyright (C) 2015 Yannis Papanikolaou <ypapanik@csd.auth.gr>
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
import gr.auth.csd.mlkd.utils.CmdOption;
import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Document;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 * 
 * This class creates the clustering dataset in the form of a sparse 2-dimensional 
 * binary array with each row representing a label and each column a document. 
 */
public class ClusteringDataset {

    protected Labels labels = null;
    protected ArrayList<LinkedHashSet<Integer>> data;

    public ClusteringDataset() {
    }

    public ClusteringDataset(Labels labels, String trainingFile, String labelsFile) {
        CorpusJSON corpus = new CorpusJSON(trainingFile);
        if (labels != null) {
            this.labels = labels;
        } else {
            this.labels = new Labels(corpus);
        }
        this.labels.writeLabels(labelsFile);
        data = new ArrayList<>();
        for (int i = 0; i < this.labels.getSize(); i++) {
            data.add(i, new LinkedHashSet<Integer>());
        }

        corpus.reset();
        Document doc;
        int docId = 1;
        while ((doc = corpus.nextDocument()) != null) {
            THashSet<String> documentLabels = doc.getLabels();
            for (String label : documentLabels) {
                data.get(this.labels.getIndex(label) - 1).add(docId);
            }
            docId++;
        }
    }

    public Labels getLabels() {
        return labels;
    }

    public String writeToFile(String inputFile, List<? extends Object> labs) {
        //write to file
        try (BufferedWriter output = Files.newBufferedWriter(Paths.get(inputFile + ".libSVM"), Charset.forName("UTF-8"))) {
            if (labs == null) {
                int label = 0;
                for (LinkedHashSet<Integer> d : data) {
                    output.write(label + " ");
                    Iterator<Integer> it2 = d.iterator();
                    while (it2.hasNext()) {
                        int docid = it2.next();
                        output.write(docid + ":1 ");
                    }
                    label++;
                    output.newLine();
                }
            } else {
                for (Object label : labs) {
                    Integer key = Integer.parseInt(label.toString());
                    output.write(key + " ");
                    Iterator<Integer> it2 = data.get(key).iterator();
                    while (it2.hasNext()) {
                        int docid = it2.next();
                        output.write(docid + ":1 ");
                    }
                    output.newLine();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClusteringDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
        return inputFile;
    }

    public static void main(String args[]) {
        
        CmdOption option = new CmdOption(args);
        ClusteringDataset cd = new ClusteringDataset(null, option.trainingFile, option.labels);
        cd.writeToFile(option.trainingFile, null);
    }

}
