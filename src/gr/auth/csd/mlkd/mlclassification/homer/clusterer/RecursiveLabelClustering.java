package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.LabelList;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.mlclassification.homer.ClusteringDataset;
import gr.auth.csd.mlkd.mlclassification.homer.Node;
import gr.auth.csd.mlkd.preprocessing.Corpus;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public abstract class RecursiveLabelClustering<S extends SparseNumberVector> extends LabelClustering {

    final int maxClusterSize;
    protected int numOfClusters;
    private final boolean eval = true;
    private int depth=0;

    public RecursiveLabelClustering(Labels labels, int max, int num, String distFunction) {
        super(labels, distFunction);
        this.maxClusterSize = max;
        this.numOfClusters = num;
    }

    public void recursion(boolean firstCall, Node<TIntHashSet> parent, String libSVMFile, 
            ClusteringDataset cd, Corpus corpus) throws FileNotFoundException {
        
        MultipleObjectsBundle objs = parse(libSVMFile + ".libSVM");
        StaticArrayDatabase db = createDb(objs);
        Relation<S> rel = db.getRelation(TypeUtil.SPARSE_VECTOR_VARIABLE_LENGTH);
        DBIDRange ids = (DBIDRange) rel.getDBIDs();
        if (ids.size() < maxClusterSize) {
            return;
        }
        Clustering<Model> c = (Clustering<Model>) run(db);
        depth++;
        if(eval) {
            parent.setDepth(depth);
            parent.setSilhouette(this.silhouette(db, c, rel));
        }
        //printResults(c, ids);
        if (c.getAllClusters().size() == 1) { //if 
            delete(libSVMFile);
            return;
        }
        int clusterId = 0;
        ArrayList<Node<TIntHashSet>> children = new ArrayList<>();
        for (Cluster<Model> clu : c.getAllClusters()) {
            clusterId++;
            TIntHashSet clusterSet = new TIntHashSet();
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                final int offset = ids.getOffset(it);

                clusterSet.add((Integer.parseInt(objs.getColumn(1).get(offset).toString())) + 1);
            }
            id++;
            //System.out.println(id);
            Node<TIntHashSet> cluster = new Node<>(clusterSet, parent, null, id, hierarchy);
            children.add(cluster);
            boolean stopCriterion = false;//stopCriterion(clusterSet);

            if (clu.getIDs().size() > maxClusterSize&& !stopCriterion) {
                int i = 0;
                ArrayList<S> os = new ArrayList<>();
                List<LabelList> ls = new ArrayList<>();
                for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
                    final S obj = rel.get(it);
                    os.add(obj);
                    ArrayList<String> lss = new ArrayList<>();
                    lss.add(objs.getColumn(1).get(ids.getOffset(it)) + "");
                    ls.add(LabelList.make(lss));
                    i++;
                }

                MultipleObjectsBundle objs2 = MultipleObjectsBundle.makeSimple(TypeUtil.SPARSE_VECTOR_VARIABLE_LENGTH, os, TypeUtil.LABELLIST, ls);
                String svmFile = libSVMFile + "." + clusterId;
                List<?> labs = objs2.getColumn(1);
                cd.writeToFile(svmFile, labs);
                delete(libSVMFile);
                //this.numOfClusters = 2;
                recursion(false, cluster, svmFile, cd, corpus);
            } else {
                delete(libSVMFile);
            }
        }
        depth--;
        parent.setChildren(children, hierarchy);
    }

    protected void delete(String libSVMFile) {
        File toDelete = new File(libSVMFile + ".libSVM");
//        if (!toDelete.delete()) {
//            System.out.println(libSVMFile + " not deleted.");
//        }
    }

    private boolean stopCriterion(TIntHashSet clusterSet, Corpus c) {
        boolean stop = false;
        //System.out.println(labels.getPositiveInstances());
        TIntIterator it = clusterSet.iterator();
        THashSet<String> clusterLabels = labels.getLabels(clusterSet);
        int corpusSize = Labels.positiveInstancesInCorpus(c, clusterLabels);
        //int positives=0;
        
        int criterion = 0;
        double avgfreq = 0;
        while(it.hasNext()) {
            int labelIndex = it.next();
            String label = labels.getLabel(labelIndex);
            int positivesPerLabel = labels.getPositiveInstances().get(label);
            avgfreq+=positivesPerLabel;
            int negatives = corpusSize - positivesPerLabel;
            //System.out.println(label+" "+positivesPerLabel+" "+negatives);
            criterion+=positivesPerLabel-negatives;
        }
        
        System.out.println("criterion:"+criterion+" average label frequency:"+
                (avgfreq/clusterSet.size())+" Ln = "+clusterSet.size()+" corpusSize = "+corpusSize);
        if(criterion>-200) {
            stop = true;
            System.out.println("Stopping further expansion of the node");
        }
        return stop;
    }
}
