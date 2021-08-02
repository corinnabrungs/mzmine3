/*
 * Copyright 2006-2021 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package io.github.mzmine.datamodel.features.types.numbers;

import io.github.mzmine.datamodel.features.types.annotations.FormulaAnnotationType;
import io.github.mzmine.datamodel.features.types.numbers.abstr.ScoreType;


/**
 * A {@link ScoreType} that combines multiple other scores. This type is typically used right after
 * other score types to signal which scores were combined. In the case of {@link
 * FormulaAnnotationType}, CombinedScore combines {@link IsotopePatternScoreType} and {@link
 * MsMsScoreType}, and the relative mass differnce into one score.
 */
public class CombinedScoreType extends ScoreType {

  @Override
  public String getHeaderString() {
    return "Combined score";
  }

}
