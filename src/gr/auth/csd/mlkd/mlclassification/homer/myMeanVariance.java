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

import de.lmu.ifi.dbs.elki.math.MeanVariance;

/**
 *
 * @author Yannis Papanikolaou <ypapanik@csd.auth.gr>
 */
public class myMeanVariance extends MeanVariance {
      @Override
  public void put(double val) {
    n += 1.0;
    final double delta = val - m1;
    m1 += delta / n;
    //System.out.println(delta/n);
    // The next line needs the *new* mean!
    m2 += delta * (val - m1);
  }
}
