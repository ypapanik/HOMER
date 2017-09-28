/*
 * Copyright (C) 2015 Yannis Papanikolaou
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
package gr.auth.csd.mlkd.mlclassification.homer.clusterer;

import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.AGNES;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.AnderbergHierarchicalClustering;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.CompleteLinkageMethod;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.HDBSCANLinearMemory;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.HierarchicalClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.SLINK;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.SingleLinkageMethod;
import de.lmu.ifi.dbs.elki.algorithm.clustering.hierarchical.extraction.SimplifiedHierarchyExtraction;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.DendrogramModel;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.result.Result;
import de.lmu.ifi.dbs.elki.utilities.datastructures.hierarchy.Hierarchy;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import gr.auth.csd.mlkd.preprocessing.Labels;
import java.util.List;

/**
 *
 * @author Yannis Papanikolaou
 */
public class Hierarchical extends RecursiveLabelClustering {

    private final String method;
    private final int minPts;
    public Hierarchical(Labels labels, int max, int num, String m, String df) {
        super(labels, max, num, df);
        method = m;
        minPts = max;
    }

    @Override
    protected Result run(Database db) {
        ListParameterization params = new ListParameterization();
        HierarchicalClusteringAlgorithm a;
        if("anderberg".equals(method)) a = new AnderbergHierarchicalClustering(dist, SingleLinkageMethod.STATIC);
        else if("agnes".equals(method)) a = new AGNES(dist, /*Single*/CompleteLinkageMethod.STATIC);
        else if("slink".equals(method)) a = new SLINK(dist);
        else a = new HDBSCANLinearMemory(dist, minPts);

        //ExtractFlatClusteringFromHierarchy e = new ExtractFlatClusteringFromHierarchy(a, numOfClusters , ExtractFlatClusteringFromHierarchy.OutputMode.STRICT_PARTITIONS, false);
        SimplifiedHierarchyExtraction e = new SimplifiedHierarchyExtraction(a, numOfClusters);
        Clustering<DendrogramModel> m = e.run(db);
        List<Cluster<DendrogramModel>> clusters = m.getToplevelClusters();
        //System.out.println(clusters.toString());
//        for(Cluster<DendrogramModel> c:clusters) {
//            rec(c, m.getClusterHierarchy());
//        }
        

        return m;
    }
    
    private void rec(Cluster<DendrogramModel> c, Hierarchy h) {
        DBIDs ids = c.getIDs();
        for (DBIDIter it = c.getIDs().iter(); it.valid(); it.advance()) {
                System.out.print(it.internalGetIndex()+", ");
        }
        Hierarchy.Iter<Cluster<DendrogramModel>> it2 = h.iterChildren(c);
        while(it2.valid()) {
            Cluster<DendrogramModel> get = it2.get();
            rec(get,h);
            it2.advance();
        }
    }
    

}
