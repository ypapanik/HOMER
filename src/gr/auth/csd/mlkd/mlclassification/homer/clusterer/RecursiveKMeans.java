package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class RecursiveKMeans<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {
    

    public RecursiveKMeans(Labels labels, int maxClusterSize, int numOfClusters, String df) {
        super(labels, maxClusterSize, numOfClusters, df);
    }

    @Override
    protected Result run(Database db) {

        
        // To fix the random seed, use: new RandomFactory(seed);
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
        KMeansLloyd<S> km = new KMeansLloyd<>(dist, numOfClusters, 0, init);
        return km.run(db);

    }
}
