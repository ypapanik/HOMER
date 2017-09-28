package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.correlation.ORCLUS;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class Orclus<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    int numberOfClusters;

    public Orclus(Labels labels, int max, int num, String distFunction) {
        super(labels, max, num, distFunction);
    }

    @Override
    protected Result run(Database db) {
        ListParameterization params = new ListParameterization();
        double epsilon = 0.1;
        int minpts = 5;
        //SUBCLU subclu = new SUBCLU((DimensionSelectingSubspaceDistanceFunction) dist, epsilon, minpts);
        params.addParameter(ORCLUS.Parameterizer.ALPHA_ID, 0.5);
        params.addParameter(ORCLUS.Parameterizer.K_ID, this.numberOfClusters);
        params.addParameter(ORCLUS.Parameterizer.K_I_ID, 5);
        params.addParameter(ORCLUS.Parameterizer.L_ID, 5);
        params.addParameter(ORCLUS.Parameterizer.SEED_ID, RandomFactory.DEFAULT);
        ORCLUS orclus = ClassGenericsUtil.parameterizeOrAbort(ORCLUS.class, params);
        return orclus.run(db);

    }
}
