package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.DBSCAN;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.FastOPTICS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSTypeAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.optics.OPTICSXi;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.index.preprocessed.fastoptics.RandomProjectedNeighborssAndDensities;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.mlclassification.homer.ClusteringDataset;
import gr.auth.csd.mlkd.mlclassification.homer.Node;
import gr.auth.csd.mlkd.preprocessing.Corpus;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.FileNotFoundException;

public class Dbscan<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    final int minPts;
    double epsilon;

    public Dbscan(Labels labels, int maxClusterSize, double e, String df) {
        super(labels, maxClusterSize, 0, df);
        minPts = maxClusterSize;
        epsilon = e;
    }

    @Override
    public void recursion(boolean firstCall, Node<TIntHashSet> parent, String libSVMFile, ClusteringDataset cd, Corpus corpus) throws FileNotFoundException {
        epsilon = 2*epsilon;
        super.recursion(firstCall, parent, libSVMFile, cd, corpus);
    }

    
    
    @Override
    protected Result run(Database db) {
        DBSCAN<SparseNumberVector> dbscan = new DBSCAN<>(dist, epsilon, minPts);
        return dbscan.run(db);

    }
}
