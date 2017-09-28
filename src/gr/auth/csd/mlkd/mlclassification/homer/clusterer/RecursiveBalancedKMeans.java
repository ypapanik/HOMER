package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import gr.auth.csd.mlkd.preprocessing.Labels;
import tutorial.clustering.SameSizeKMeansAlgorithm;

public class RecursiveBalancedKMeans<S extends SparseNumberVector> extends RecursiveKMeans<S> {

    public RecursiveBalancedKMeans(Labels labels, int maxClusterSize, int numOfClusters, String df) {
        super(labels, maxClusterSize, numOfClusters, df);
    }

    @Override
    protected Result run(Database db) {
        // To fix the random seed, use: new RandomFactory(seed);
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
        
        SameSizeKMeansAlgorithm km = new SameSizeKMeansAlgorithm (dist, numOfClusters, 0, init);
        return km.run(db);

    }
}
