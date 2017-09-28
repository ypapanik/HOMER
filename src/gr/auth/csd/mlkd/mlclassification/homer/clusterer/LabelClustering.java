package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.SparseDoubleVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.DatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.MultipleObjectsBundleDatabaseConnection;
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle;
import de.lmu.ifi.dbs.elki.datasource.filter.typeconversions.SparseVectorFieldFilter;
import de.lmu.ifi.dbs.elki.datasource.parser.LibSVMFormatParser;
import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.NumberVectorDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SparseEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SparseManhattanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.ChiSquaredDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.probabilistic.SqrtJensenShannonDivergenceDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.set.HammingDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.set.JaccardSimilarityDistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateDaviesBouldin;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateSilhouette;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.NoiseHandling;
import de.lmu.ifi.dbs.elki.result.Result;
import gnu.trove.set.hash.TIntHashSet;
import gr.auth.csd.mlkd.mlclassification.homer.Tree;
import gr.auth.csd.mlkd.mlclassification.homer.myEvaluateSilhouette;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LabelClustering {

    public final Tree<TIntHashSet> hierarchy;
    public final Labels labels;
    protected static int id = 0;
    
    JaccardSimilarityDistanceFunction distJacc = new JaccardSimilarityDistanceFunction();
    HammingDistanceFunction distHamm =  HammingDistanceFunction.STATIC;
    SquaredEuclideanDistanceFunction distSqEucl = SquaredEuclideanDistanceFunction.STATIC;
    SparseEuclideanDistanceFunction distSparsEucl = SparseEuclideanDistanceFunction.STATIC;
    CosineDistanceFunction distCos = CosineDistanceFunction.STATIC;
    ChiSquaredDistanceFunction distChisq = ChiSquaredDistanceFunction.STATIC;
    SqrtJensenShannonDivergenceDistanceFunction distJS = SqrtJensenShannonDivergenceDistanceFunction.STATIC;
    SparseManhattanDistanceFunction distSparsManh = SparseManhattanDistanceFunction.STATIC;
    
    NumberVectorDistanceFunction dist;
    
    public LabelClustering(Labels labels, String distanceFunction) {

        TIntHashSet labelSet = new TIntHashSet();
        this.labels = labels;
        for (String label : labels.getLabels()) {
            labelSet.add(labels.getIndex(label));
        }
        hierarchy = new Tree<>(labelSet);
        
        if(null != distanceFunction) switch (distanceFunction) {
            case "sqEucl":
                dist = distSqEucl;
                break;
            case "sparsEucl":
                dist = (NumberVectorDistanceFunction) distSparsEucl;
                break;
            case "Jacc":
                dist = distJacc;
                break;
            case "Hamm":
                dist = distHamm;
                break;
            case "Cos":
                dist = distCos;
                break;
            case "Chisq":
                dist = distChisq;
                break;
            case "JS":
                dist = distJS;
                break;
            case "Manh":
                dist = (NumberVectorDistanceFunction) distSparsManh;
                break;
            default:
                System.out.println("Erroneous distance function type");
                break;
        }
        
    }

    protected abstract Result run(Database db);
    
    public MultipleObjectsBundle parse(String libSVMFile) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(libSVMFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LabelClustering.class.getName()).log(Level.SEVERE, null, ex);
        }

        LibSVMFormatParser parser = new LibSVMFormatParser(SparseDoubleVector.FACTORY);
        SparseVectorFieldFilter<SparseDoubleVector> filter = new SparseVectorFieldFilter<>();
        return filter.filter(parser.parse(inputStream));
    }

    protected void printResults(Clustering<Model> c, DBIDRange ids) {

        int i = 0;
        for (Cluster<Model> clu : c.getAllClusters()) {
            System.out.println("Cluster " + "." + i);
            //System.out.println("#" + i + ": " + clu.getNameAutomatic());
            //System.out.println("Size: " + clu.size());
            System.out.print("Objects: ");
            for (DBIDIter it = clu.getIDs().iter(); it.valid(); it.advance()) {
            // NumberVector v = rel.get(it);

                // Offset within our DBID range: "line number"
                final int offset = ids.getOffset(it);
                System.out.print(" " + (offset + 1) + ",");
                //System.out.print(" " + labels.getLabel(offset + 1) + ",");
            }
            System.out.println(clu.getIDs().size());
            
            ++i;
        }
    }

    protected StaticArrayDatabase createDb(MultipleObjectsBundle objs) {
        DatabaseConnection dbc = new MultipleObjectsBundleDatabaseConnection(objs);
        StaticArrayDatabase db;
        db = new StaticArrayDatabase(dbc, null);
        db.initialize();
        return db;
    }
    
    public double DBIndex(Database db, Clustering<?> run, Relation rel) {
        EvaluateDaviesBouldin e = new EvaluateDaviesBouldin(dist, NoiseHandling.IGNORE_NOISE);
        return e.evaluateClustering(db, rel,  run);
    }
    
    public double silhouette(Database db, Clustering<?> run, Relation rel) {
                DistanceQuery dq = db.getDistanceQuery(rel, dist);
        myEvaluateSilhouette es = new myEvaluateSilhouette(dist, NoiseHandling.IGNORE_NOISE, false);
        return es.evaluateClustering(db, rel, dq, (Clustering) run);
    }
}
