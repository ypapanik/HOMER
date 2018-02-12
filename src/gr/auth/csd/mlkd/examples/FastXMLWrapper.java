/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.auth.csd.mlkd.examples;

import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.preprocessing.VectorizeMultiLabelLibSVM;
import gr.auth.csd.mlkd.utils.EvaluateFastXML;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 */
public class FastXMLWrapper {

    public static void main(String args[]) throws InterruptedException, IOException {
        String dataset = args[0];
        String inTrain = "data/"+args[0]+"/"+args[0]+"train";
        String inTest = "data/"+args[0]+"/"+args[0]+"test";
        String outTrain = "data/"+args[0]+"/train.txt";
        String outTest = "data/"+args[0]+"/test.txt";
               
        CorpusJSON corpus = new CorpusJSON(inTrain);
        CorpusJSON corpus2 = new CorpusJSON(inTest);
        Dictionary dic = new Dictionary(corpus, 5, 10000, 10, 5000);
        Labels labels = new Labels(corpus);
      
        VectorizeMultiLabelLibSVM v = new VectorizeMultiLabelLibSVM(dic, false, labels);
        v.transfrom(inTrain, inTest, outTrain, outTest);
        //Process process = new ProcessBuilder("./fastXML.sh", dataset).redirectError(new File("err.txt")).redirectOutput(new File("out.txt")).start();
        Process process = new ProcessBuilder("./pfastreXML.sh", dataset).redirectError(new File("err.txt")).redirectOutput(new File("out.txt")).start();
        process.waitFor();
        EvaluateFastXML ev = new EvaluateFastXML(outTest, "results/"+dataset+"/score_mat.txt", labels.getSize(), 5);
    }
}
