/*
 * Copyright (C) 2015 Yannis Papanikolaou <ypapanik@csd.auth.gr>
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
package de.bwaldvogel.liblinear;

import gnu.trove.map.hash.TIntDoubleHashMap;
import java.io.Serializable;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 */
public class ModelGr  implements Serializable {
    static final long serialVersionUID = 8350540949393369637L;
    double bias;
    int[] label;
    int nr_class;
    int nr_feature;
    SolverType solverType;
    /** feature weight array */
    TIntDoubleHashMap w;
    int wSize;

    public ModelGr(Model model) {
        this.bias = model.bias;
        this.label = model.label;
        this.nr_class = model.nr_class;
        this.nr_feature = model.nr_feature;
        this.solverType = model.solverType;
        wSize = model.w.length;
        w = new TIntDoubleHashMap();
        int counter=0;
        for(int i=0;i<wSize;i++) {
            if(model.w[i]!=0.0) w.put(i, model.w[i]);
        }
    }
    
    public static Model modelGrToModel(ModelGr mg) {
        
        if(mg==null) return null;
        else {
            Model m = new Model();
            m.bias = mg.bias;
            m.label = mg.label;
            m.nr_class = mg.nr_class;
            m.nr_feature = mg.nr_feature;
            m.solverType = mg.solverType;
            m.w = new double[mg.wSize];
            for(int i=0;i<mg.wSize;i++) {
                if(mg.w.containsKey(i)) m.w[i] = mg.w.get(i);
                else m.w[i] = 0.0;
            }
            return m;
        }
    }
}
