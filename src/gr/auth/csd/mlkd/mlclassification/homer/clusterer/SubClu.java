package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.em.DiagonalGaussianModelFactory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.em.EM;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.SUBCLU;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.subspace.DimensionSelectingSubspaceDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class SubClu<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    public SubClu(Labels labels, int max, int num, String distFunction) {
        super(labels, max, num, distFunction);
    }
    
    @Override
    protected Result run(Database db) {
        // To fix the random seed, use: new RandomFactory(seed);
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
        ListParameterization params = new ListParameterization();
        double epsilon = 0.1;
        int minpts = 5;
        //SUBCLU subclu = new SUBCLU((DimensionSelectingSubspaceDistanceFunction) dist, epsilon, minpts);
        params.addParameter(SUBCLU.EPSILON_ID, epsilon);
        params.addParameter(SUBCLU.MINPTS_ID, minpts);
        SUBCLU subclu = ClassGenericsUtil.parameterizeOrAbort(SUBCLU.class, params);
        return subclu.run(db);

    }
}
