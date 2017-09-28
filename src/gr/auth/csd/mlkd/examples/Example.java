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

import gr.auth.csd.mlkd.utils.CmdOption;
import gr.auth.csd.mlkd.utils.MicroAndMacroFLabelPivoted;
import gr.auth.csd.mlkd.mlclassification.MLClassifier;
import gr.auth.csd.mlkd.mlclassification.svm.BinaryRelevanceSVM;
import gr.auth.csd.mlkd.preprocessing.CorpusJSON;
import gr.auth.csd.mlkd.preprocessing.Dictionary;
import gr.auth.csd.mlkd.preprocessing.Labels;
import gr.auth.csd.mlkd.utils.Timer;

/**
 *
 * @author Yannis Papanikolaou
 */
public class Example {

    public static void main(String args[]) {
        Timer timer = new Timer();

        CmdOption option = new CmdOption(args);
        Dictionary dic = null;
        CorpusJSON corpus = new CorpusJSON(option.trainingFile);
        dic = new Dictionary(corpus, option.lowUnigrams, option.highUnigrams,
                option.lowBigrams, option.highBigrams);

        Labels labels = new Labels(corpus);
        labels.writeLabels(option.labels);
        dic.writeDictionary(option.dictionary);

        MLClassifier mlc = null;
        mlc = new BinaryRelevanceSVM(option.trainingFile, option.testFile,
                option.dictionary, option.labels, option.modelsDirectory, option.threads, false);

        //mlc.train();
        mlc.predict(null);
        mlc.bipartitionsWrite(option.bipartitionsFile);

        MicroAndMacroFLabelPivoted ev = new MicroAndMacroFLabelPivoted(labels, new CorpusJSON(option.testFile), option.bipartitionsFile);
        ev.evaluate();
    }
}
