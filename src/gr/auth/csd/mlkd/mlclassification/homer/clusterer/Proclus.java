package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.PROCLUS;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class Proclus<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    public Proclus(Labels labels, int max, int num, String distFunction) {
        super(labels, max, num, distFunction);
    }


    @Override
    protected Result run(Database db) {
        RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);
        ListParameterization params = new ListParameterization();
        params.addParameter(PROCLUS.Parameterizer.K_ID, this.numOfClusters);
        params.addParameter(PROCLUS.Parameterizer.L_ID, 10);
        params.addParameter(PROCLUS.Parameterizer.K_I_ID, 5);
        params.addParameter(PROCLUS.Parameterizer.M_I_ID, 1);
        params.addParameter(PROCLUS.Parameterizer.SEED_ID, RandomFactory.DEFAULT);
        PROCLUS proclus = ClassGenericsUtil.parameterizeOrAbort(PROCLUS.class, params);
        return proclus.run(db);

    }
}
