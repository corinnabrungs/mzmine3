/*
 * Copyright (c) 2004-2024 The mzmine Development Team
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

package io.github.mzmine.modules.dataprocessing.featdet_baselinecorrection;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.featuredata.IntensityTimeSeries;
import io.github.mzmine.gui.chartbasics.simplechart.providers.impl.AnyXYProvider;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.minimumsearch.MinimumSearchFeatureResolver;
import io.github.mzmine.util.MemoryMapStorage;
import it.unimi.dsi.fastutil.ints.IntList;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class UnivariateBaselineCorrector extends AbstractResolverBaselineCorrector {

  public UnivariateBaselineCorrector() {
    super(null, 5, "", null);
  }

  public UnivariateBaselineCorrector(@Nullable MemoryMapStorage storage, int numSamples,
      @NotNull String suffix, @Nullable MinimumSearchFeatureResolver resolver) {
    super(storage, numSamples, suffix, resolver);
  }

  /**
   * @param xDataToCorrect    the data to correct
   * @param yDataToCorrect    the data to correct
   * @param numValues         corresponding number of values - input arrays may be longer
   * @param xDataFiltered     might be the whole x data or peaks removed
   * @param yDataFiltered     might be the whole y data or peaks removed
   * @param numValuesFiltered number of filtered data points
   * @param addPreview        add preview datasets
   */
  @Override
  protected void subSampleAndCorrect(final double[] xDataToCorrect, final double[] yDataToCorrect,
      int numValues, double[] xDataFiltered, double[] yDataFiltered, int numValuesFiltered,
      final boolean addPreview) {
    // TODO change parameter to step size or window size or calculate from parameters
    int stepSize = numSamples;
    IntList subsampleIndices = buffer.createSubSampleIndicesFromLandmarks(stepSize);

    XYDataArrays subData = subSampleData(subsampleIndices, xDataFiltered, yDataFiltered,
        numValuesFiltered);

    UnivariateInterpolator interpolator = initializeInterpolator(subData.numValues());
    UnivariateFunction function = interpolator.interpolate(subData.x(), subData.y());

    for (int i = 0; i < numValues; i++) {
      // must be above zero, but not bigger than the original value.
      yDataToCorrect[i] = Math.min(
          Math.max(yDataToCorrect[i] - function.value(xDataToCorrect[i]), 0), yDataToCorrect[i]);
    }

    if (addPreview) {
      additionalData.add(new AnyXYProvider(Color.RED, "baseline", numValues, j -> xDataToCorrect[j],
          j -> function.value(xDataToCorrect[j])));

      additionalData.add(
          new AnyXYProvider(Color.BLUE, "samples", subData.numValues(), subData::getX,
              subData::getY));
    }
  }


  @Override
  public <T extends IntensityTimeSeries> T correctBaseline(T timeSeries) {
    additionalData.clear();
    final int numValues = timeSeries.getNumberOfValues();
    buffer.extractDataIntoBuffer(timeSeries);

    if (resolver != null) {
      // 1. remove baseline on a copy
      double[] copyX = Arrays.copyOf(xBuffer(), numValues);
      double[] copyY = Arrays.copyOf(yBuffer(), numValues);

      // inplace baseline correct on copyX and Y
      subSampleAndCorrect(copyX, copyY, numValues, copyX, copyY, numValues, false);

      // 2. detect peaks and remove the ranges from the original data
      // resolver sets some data points to 0 if < chromatographic threshold
      final List<Range<Double>> resolved = resolver.resolve(copyX, copyY);
      // 3. remove baseline finally on original data
      final int numPointsInRemovedArray = buffer.removeRangesFromArrays(resolved);

      // use the data with features removed
      subSampleAndCorrect(xBuffer(), yBuffer(), numValues, xBufferRemovedPeaks(),
          yBufferRemovedPeaks(), numPointsInRemovedArray, isPreview());
    } else {
      // use the original data
      subSampleAndCorrect(xBuffer(), yBuffer(), numValues, xBuffer(), yBuffer(), numValues,
          isPreview());
    }
    return createNewTimeSeries(timeSeries, numValues, yBuffer());
  }

  protected abstract UnivariateInterpolator initializeInterpolator(int actualNumberOfSamples);

}
