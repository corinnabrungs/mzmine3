/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.glyceroandglycerophospholipids;

import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.modules.dataprocessing.id_lipididentification.lipidannotationmodules.LipidAnnotationModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.selectors.FeatureListsParameter;
import io.github.mzmine.taskcontrol.Task;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;

public class GlyceroAndGlycerophospholipidAnnotationModule extends LipidAnnotationModule {

  @Override
  public @NotNull String getName() {
    return "Glycero- and Glycerophospholipid annotation";
  }

  @Override
  public @NotNull String getDescription() {
    return "This method searches for Glycero- and Glycerophospholipids.";
  }

  @Override
  public FeatureListsParameter getFeatureListsParameter() {
    return GlyceroAndGlycerophospholipidAnnotationParameters.featureLists;
  }

  @Override
  public Task createAnnotationTask(ParameterSet parameters, FeatureList featureList,
      Instant moduleCallDate) {
    return new GlyceroAndGlycerophospholipidAnnotationTask(parameters, featureList, moduleCallDate);
  }

  @Override
  public Class<? extends ParameterSet> getParameterSetClass() {
    return GlyceroAndGlycerophospholipidAnnotationParameters.class;
  }
}
