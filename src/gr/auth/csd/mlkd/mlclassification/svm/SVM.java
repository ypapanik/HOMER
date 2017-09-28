package gr.auth.csd.mlkd.mlclassification.svm;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.ModelGr;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.ProblemGr;
import de.bwaldvogel.liblinear.SolverType;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.mlclassification.BinaryClassifier;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVM extends BinaryClassifier {

    protected ProblemGr train;
    protected static ProblemGr test = null;
    protected Parameter param;
    protected Model model;
    double C = 1.0;
    double E = 0.01;
    protected int BIAS = 1;
    private boolean tuned = false;

    public SVM(String trainLibSvm, String testLibSvm, int threads, int mod, int start,
            int stop, String modelFolder, TIntObjectHashMap<TreeSet<Integer>> lv,
            byte learnorpredict, TIntHashSet mc, int nrFeatures, boolean score,
            int nrDocuments, int nrLabels, boolean tuned) {
        super(trainLibSvm, testLibSvm, threads, mod, start, stop, modelFolder, lv,
                learnorpredict, mc, nrFeatures, nrDocuments, score, nrLabels);
        Linear.disableDebugOutput();
        this.param = (!tuned) ? new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, C, E)
                : new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 0.225, E);
        //param = new Parameter(SolverType.L1R_L2LOSS_SVC, C, E);

        if (trainLibSvm != null) {
            this.train = loadProblem(trainLibSvm);
        }
        if (test == null && testLibSvm != null) {
            this.test = loadProblem(testLibSvm);
        }
        this.tuned = tuned;
    }

    public SVM(String trainLibSvm, String testLibSvm, int threads, int mod, int start,
            int stop, String modelFolder, TIntObjectHashMap<TreeSet<Integer>> lv,
            byte learnorpredict, TIntHashSet mc, int nr_features, int nr_Documents,
            boolean score, int nrLabels) {
        super(trainLibSvm, testLibSvm, threads, mod, start, stop, modelFolder, lv,
                learnorpredict, mc, nr_features, nr_Documents, score, nrLabels);
        Linear.disableDebugOutput();
        this.param = new Parameter(SolverType.L2R_L2LOSS_SVC_DUAL, 1.0, E);
    }

    @Override
    public void learn(int label) {
        if ((modelChoice != null) && !modelChoice.contains(label)) {
            System.out.println(label + " not in modelchoice");
            return;
        }
        changeShell(label);
        if (tuned) {
            int pos = 0;
            for (double l : train.y) {
                if (l == 1) {
                    pos++;
                }
            }
            //System.out.println("pos:"+pos);
            if (pos < 100) {
                int[] weightLabels = {0, 1};
                double[] weights = {1, (100.0 / pos) + 1};
                param.setWeights(weights, weightLabels);
            }
        }

        //System.out.println("Training label "+label);
        model = Linear.train(train, param);
        //System.out.println("Finished training label "+label);
        // save models if needed
        //saveModel(label);
        saveCompact(label);
    }

    @Override
    public void predict(int label) {
        if ((modelChoice != null) && !modelChoice.contains(label)) {
            //System.out.println(label + " not in modelchoice");
            return;
        }
        Model m;
        double d;
        File f = new File(modelFolder + "/model" + label + ".dat");
        if (!f.exists()) { //System.out.println("label no"+label+" model doesn't exist!");

            return;
        }
        //System.out.println("label:" + label);
        if (train == null) {
            //m = readModel(label);
            m = readCompact(label);

        } else {
            m = model;
        }
        //if(label ==1) System.out.println("Test: "+Arrays.deepToString(test.x));
        for (int i = 0; i < test.l; i++) {
            try {
                d = de.bwaldvogel.liblinear.Linear.predict(m, test.x[i]);
                predictions[i][label - 1] = d;
            } catch (Exception ex) {
                Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.out.println(Arrays.toString(predictions[0]));
    }

    private ProblemGr loadProblem(String filenameTrainShell) {
        ProblemGr problem;
        File trainFile = new File(filenameTrainShell);
        try {
            problem = ProblemGr.readFromFile(trainFile, BIAS, nr_features);
            return problem;

        } catch (IOException | InvalidInputDataException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    protected void changeShell(int label) {
        //System.out.println("Changing shell");
        TreeSet<Integer> sortedSet = labelValues.get(label);
        int j = 0;
        for (Integer x : sortedSet) {
            int jj = x;
            while (j < jj) {
                if (train.y[j] == 1) {
                    train.y[j] = 0;
                }
                j++;
            }
            if (train.y[j] == 0) {
                train.y[j] = 1;
            }
            j++;
        }

        int numInstances = train.y.length;
        while (j < numInstances) {
            if (train.y[j] == 1) {
                train.y[j] = 0;
            }
            j++;
        }
    }

    @Override
    public Model readModel(int label) {
        Model m = null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFolder + "/model" + label + ".dat")));) {
            m = (Model) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            System.out.println(e);
        }
        return m;
    }

    @Override
    public void saveModel(int label) {
        if (modelFolder != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(modelFolder + "/model" + label + ".dat")));) {
                oos.writeObject(model);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public Model readCompact(int label) {
        ModelGr mg = null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFolder + "/model" + label + ".dat")));) {
            mg = (ModelGr) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SVM.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            System.out.println(e);
        }
        return ModelGr.modelGrToModel(mg);
    }

    @Override
    public Model readCompact(int label, String modelFolder) {
        ModelGr mg = null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(modelFolder + label + ".dat")));) {
            mg = (ModelGr) ois.readObject();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ModelGr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException e) {
            System.out.println(e);
        }
        return ModelGr.modelGrToModel(mg);
    }

    @Override
    public void saveCompact(int label) {
        ModelGr mg = new ModelGr(model);
        if (modelFolder != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(modelFolder + "/model" + label + ".dat")));) {
                oos.writeObject(mg);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void setTest(ProblemGr test) {
        SVM.test = test;
    }

}
