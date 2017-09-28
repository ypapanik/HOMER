package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.DiagonalGaussianModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class Em<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    int numberOfClusters;
    double delta = 0.9;
    int maxiter = 200;

    public Em(Labels labels, int max, int num, String distFunction) {
        super(labels, max, num, distFunction);
    }


    @Override
    protected Result run(Database db) {
        SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
        // To fix the random seed, use: new RandomFactory(seed);
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
        ListParameterization params = new ListParameterization();
        EM em = new EM(numberOfClusters, delta, new DiagonalGaussianModelFactory(init), maxiter, false);//ClassGenericsUtil.parameterizeOrAbort(EM.class, params);
        return em.run(db);

    }
}
