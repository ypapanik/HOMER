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
package gr.auth.csd.mlkd.utils;

import java.util.TreeSet;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 */
public class ConfMatrix {

    private int tp=0, fp=0, fn=0, tn=0;

    public int getTp() {
        return tp;
    }

    public int getFp() {
        return fp;
    }

    public int getFn() {
        return fn;
    }

    public int getTn() {
        return tn;
    }

    public ConfMatrix(TreeSet<Integer> pred, TreeSet<Integer> truth, int numLabels) {
        //add true and predicted labels to a single set and then iterate over it
        TreeSet<Integer> all = new TreeSet<>(truth);
        all.addAll(pred);
        System.out.println(truth+" "+pred);
        for (Integer label : all) {
            if (label>numLabels) {
                System.out.println(label);
                continue;
            }
            if (truth.contains(label)) {
                if (pred.contains(label)) {
                    tp++;
                } else {
                    fn++;
                }
            } else {
                if (pred.contains(label)) {
                    fp++;
                } else {
                    tn++;
                }
            }
        }
    }
    
    public ConfMatrix(TreeSet<Integer> pred, TreeSet<Integer> truth, double[] tpPerLabel, 
            double[] fnPerLabel, double[] fpPerLabel, double[] tnPerLabel, int numLabels) {
        //add true and predicted labels to a single set and then iterate over it
        TreeSet<Integer> all = new TreeSet<>(truth);
        all.addAll(pred);
        for (Integer label : all) {
            int j = label;
            if (j>numLabels) {
                //System.out.println(label);
                continue;
            }
            if (truth.contains(label)) {
                if (pred.contains(label)) {
                    tpPerLabel[j]++;
                    tp++;
                } else {
                    fnPerLabel[j]++;
                    fn++;
                }
            } else {
                if (pred.contains(label)) {
                    fpPerLabel[j]++;
                    fp++;
                    //System.out.println(doc+" "+truth+" "+pred);
                } else {
                    tnPerLabel[j]++;
                    tn++;
                }
            }
        }
    }

}
