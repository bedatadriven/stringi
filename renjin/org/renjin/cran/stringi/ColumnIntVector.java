/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.cran.stringi;

import org.renjin.primitives.matrix.Matrix;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

/**
 * Wrapper class to present a column from a matrix as a vector.
 */
public class ColumnIntVector extends IntVector {
  private Matrix matrix;
  private int col;

  public ColumnIntVector(Matrix matrix, int col) {
    this.matrix = matrix;
    this.col = col;
  }

  @Override
  public boolean isConstantAccessTime() {
    return matrix.getVector().isConstantAccessTime();
  }

  @Override
  public int length() {
    return matrix.getNumRows();
  }

  @Override
  public int getElementAsInt(int i) {
    return matrix.getElementAsInt(i, col);
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new ColumnIntVector(matrix, col);
  }

}
