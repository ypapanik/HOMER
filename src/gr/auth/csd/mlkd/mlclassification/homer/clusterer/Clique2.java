package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.subspace.CLIQUE;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.model.SubspaceModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import java.io.FileNotFoundException;

public class Clique2 {

    public static void main(String[] args) throws FileNotFoundException {

        ListParameterization params = new ListParameterization();
        params.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, "mouse.csv");
        Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
        db.initialize();
        ListParameterization params2 = new ListParameterization();
        params2.addParameter(CLIQUE.TAU_ID, "0.01");
        params2.addParameter(CLIQUE.XSI_ID, 30);

        // setup algorithm
        CLIQUE<DoubleVector> clique = ClassGenericsUtil.parameterizeOrAbort(CLIQUE.class, params2);

        // run CLIQUE on database
        Clustering<SubspaceModel> result = (Clustering<SubspaceModel>) clique.run(db);

        for (Cluster<?> cl : result.getToplevelClusters()) {
            System.out.println(cl.getIDs());
        }
    }
}
