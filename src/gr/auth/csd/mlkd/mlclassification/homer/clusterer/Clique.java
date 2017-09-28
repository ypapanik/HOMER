package gr.auth.csd.mlkd.mlclassification.homer.clusterer;


import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE;
import de.lmu.ifi.dbs.elki.data.SparseNumberVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;

public class Clique<S extends SparseNumberVector> extends RecursiveLabelClustering<S> {

    public Clique(Labels labels, int max, int num, String distFunction) {
        super(labels, max, num, distFunction);
    }



    @Override
    protected Result run(Database db) {
        ListParameterization params = new ListParameterization();
        params.addParameter(CLIQUE.TAU_ID, "0.2");
        params.addParameter(CLIQUE.XSI_ID, 10);
        CLIQUE<S> clique = ClassGenericsUtil.parameterizeOrAbort(CLIQUE.class, params);
        return clique.run(db);

    }
}
