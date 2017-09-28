/*
 * Copyright (C) 2016 Yannis Papanikolaou <ypapanik@csd.auth.gr>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.auth.csd.mlkd.mlclassification.homer;

import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDArrayIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateSilhouette;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.NoiseHandling;
import de.lmu.ifi.dbs.elki.logging.statistics.DoubleStatistic;
import de.lmu.ifi.dbs.elki.logging.statistics.LongStatistic;
import de.lmu.ifi.dbs.elki.logging.statistics.StringStatistic;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.result.EvaluationResult;
import de.lmu.ifi.dbs.elki.utilities.FormatUtil;
import java.util.List;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 */
public class myEvaluateSilhouette<O> extends EvaluateSilhouette<O> {

    public myEvaluateSilhouette(DistanceFunction distance, NoiseHandling noiseOption, boolean penalize) {
        super(distance, noiseOption, penalize);
    }

    public myEvaluateSilhouette(DistanceFunction distance, boolean mergenoise) {
        super(distance, mergenoise);
    }

    @Override
    public double evaluateClustering(Database db, Relation<O> rel, DistanceQuery<O> dq, Clustering<?> c) {
        List<? extends Cluster<?>> clusters = c.getAllClusters();
        myMeanVariance msil = new myMeanVariance();
        int ignorednoise = 0;
        for (Cluster<?> cluster : clusters) {
            ArrayDBIDs ids = DBIDUtil.ensureArray(cluster.getIDs());
            double[] as = new double[ids.size()]; // temporary storage.
            DBIDArrayIter it1 = ids.iter(), it2 = ids.iter();
            for (it1.seek(0); it1.valid(); it1.advance()) {
                // a: In-cluster distances
                double a = as[it1.getOffset()]; // Already computed distances
                for (it2.seek(it1.getOffset() + 1); it2.valid(); it2.advance()) {
                    final double dist = dq.distance(it1, it2);
                    a += dist;
                    as[it2.getOffset()] += dist;
                }
                a /= (ids.size() - 1);
                // b: minimum average distance to other clusters:
                double b = Double.POSITIVE_INFINITY;
                for (Cluster<?> ocluster : clusters) {
                    if (ocluster == /* yes, reference identity */ cluster) {
                        continue; // Same cluster
                    }
                    if (ocluster.size() <= 1 || ocluster.isNoise()) {
                                continue; 
                    }
                    final DBIDs oids = ocluster.getIDs();
                    double btmp = 0.;
                    for (DBIDIter it3 = oids.iter(); it3.valid(); it3.advance()) {
                        btmp += dq.distance(it1, it3);
                    }
                    btmp /= oids.size(); // Average
                    b = btmp < b ? btmp : b; // Minimum average
                }
                // One cluster only?
                b = b < Double.POSITIVE_INFINITY ? b : a;
                Double v = (b - a) / (b > a ? b : a);
                if(!v.isNaN()) msil.put(v);
            }
        }
        double penalty = 1.;
        final double meansil = /*penalty */ msil.getMean();
        //final double stdsil = penalty * msil.getSampleStddev();
        EvaluationResult ev = EvaluationResult.findOrCreate(db.getHierarchy(), c, "Internal Clustering Evaluation", "internal evaluation");
        EvaluationResult.MeasurementGroup g = ev.findOrCreateGroup("Distance-based Evaluation");
        //g.addMeasure("Silhouette +-" + FormatUtil.NF2.format(stdsil), meansil, -1., 1., 0., false);
        db.getHierarchy().resultChanged(ev);
        return meansil;
    }

}
