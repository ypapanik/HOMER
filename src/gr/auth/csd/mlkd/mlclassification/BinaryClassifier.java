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
package gr.auth.csd.mlkd.mlclassification;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.TreeSet;

public abstract class BinaryClassifier implements Runnable {

    protected int threads;
    protected int mod;
    protected int start;
    protected int stop;
    protected static double[][] predictions = null;
    protected String modelFolder;
    protected static TIntObjectHashMap<TreeSet<Integer>> labelValues;
    //flag to check wether we re gonna learn/predict (0) or just predict(1)
    byte learnOrPredict;
    protected static TIntHashSet modelChoice = null;
    protected final String trainLibSvm;
    protected final String testLibSvm;
    protected final int nr_features;

    public static void setPredictions(double[][] predictions) {
        BinaryClassifier.predictions = predictions;
    }
    protected static boolean score;

    public BinaryClassifier(String trainLibSvm, String testLibSvm, int threads, int mod, int start, int stop,
            String modelFolder, TIntObjectHashMap<TreeSet<Integer>> lv, byte learnorpredict, TIntHashSet mc,
            int nr_features, int nr_Documents, boolean score, int nrLabels) {
        //System.out.println("Creating "+mod+" "+threads);
        this.trainLibSvm = trainLibSvm;
        this.testLibSvm = testLibSvm;
        this.modelFolder = modelFolder;
        this.threads = threads;
        this.mod = mod;
        this.start = start;
        this.stop = stop;
        labelValues = lv;
        this.learnOrPredict = learnorpredict;
        modelChoice = mc;
        this.nr_features = nr_features;
        if (testLibSvm != null) {
            if(predictions==null) {
                predictions = new double[nr_Documents][nrLabels];
            }
        }
        this.score = score;
    }

    public static void savePredictions(String file) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(predictions);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static double[][] loadPredictions(String file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            double[][] p = (double[][]) ois.readObject();
            return p;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
            return null;
        }
    }

    @Override
    public void run() {
        for (int i = start; i <= stop; i++) {
            if (i % threads == mod) {
                //System.out.println("Thread no"+mod+" learning label "+i);
                if (learnOrPredict == 0) {
                    learn(i);
                    if (testLibSvm != null) {
//                        predict(i);
                    }
                } else {
                    predict(i);
                }
            }
        }
    }

    public abstract void learn(int label);

    public abstract void predict(int label);

    protected abstract void changeShell(int label);

    public abstract Object readModel(int label);

    public abstract void saveModel(int label);

    public abstract Object readCompact(int label);

    public abstract Object readCompact(int label, String modelFolder);

    public abstract void saveCompact(int label);

    private static void normalizePredictions() {
        //System.out.println(Arrays.toString(predictions[0]));
        for (int doc = 0; doc < predictions.length; doc++) {
            for (int label = 0; label < predictions[doc].length; label++) {
                predictions[doc][label] = -predictions[doc][label];
            }
            //if (doc == 0) System.out.println(Arrays.toString(predictions[0]));
            for (int label = 0; label < predictions[doc].length; label++) {
                predictions[doc][label] = 1 / (1 + Math.exp(-predictions[doc][label]));
            }
//            //if (doc == 0) System.out.println(Arrays.toString(predictions[0]));
            Utils.normalize(predictions[doc], 1.0);
        }
        //System.out.println(Arrays.toString(predictions[0]));
    }

    public static double[][] getPredictions(boolean normalize) {
        if (score&&normalize) {
            normalizePredictions();
        }
        return predictions;
    }
}
