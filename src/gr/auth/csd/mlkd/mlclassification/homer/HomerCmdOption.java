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
package gr.auth.csd.mlkd.mlclassification.homer;

import gr.auth.csd.mlkd.utils.CmdOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author Yannis Papanikolaou
 */
public class HomerCmdOption extends CmdOption {

    @Option(name = "-treeFile", usage = "HOMER label hierarchy")
    public String treeFile = "tree";
    
    @Option(name = "-maxClusterSize", usage = "HOMER maxClusterSize")
    public int maxClusterSize = 40;
    
    @Option(name = "-numOfClusters", usage = "number of clusters at each level")
    public int numOfClusters = 3;
    
    @Option(name = "-vectorMethod", usage = "Vectorizing Method, 1 or 2")
    public String vectorMethod = "cd";//1-cd or 2-llda
    @Option(name = "-clusteringMethod", usage = "1, 2, 3")
    public String clusteringMethod = "kmeans";//1-balanced kmeans, 2-optics, 3-hierarchical, 4-dbscan
    @Option(name = "-hierarchicalMethod", usage = "hierarchical method")
    public String hierarchicalMethod = "anderberg";//1-anderberg, 2-agnes, 3-hdbscan
    @Option(name = "-classifier", usage = "base classifier")
    public String classifier = "BR"; //1- BR, 2- LLDA
    @Option(name = "-epsilon", usage = "epsilon parameter fo optics")
    public double epsilon = 0.001;
    
    @Option(name = "-distanceFunction", usage = "distance function")
    public String distanceFunction = "Jacc";
    //"sqEucl", "Jacc", "Hamm",  "Cos", "JS"
        

    public HomerCmdOption(String[] args) {
        super(args);
        CmdLineParser parser = new CmdLineParser(this);
        if (args.length == 0) {
            parser.printUsage(System.out);
            return;
        }
        try {
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            Logger.getLogger(CmdOption.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
