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

package io.github.mzmine.modules.dataprocessing.featdet_denormalize_by_inject_time;

import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExitCode;
import io.github.mzmine.util.MemoryMapStorage;
import java.time.Instant;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class DenormalizeScansMultiplyByInjectTimeModule implements MZmineProcessingModule {

  private static final String MODULE_NAME = "Denormalize scans (multiply by injection time)";
  private static final String MODULE_DESCRIPTION =
      "This module multiplies the intensities of a scan"
          + " or mass list by the injection time to denormalize scans that were acquired in a trapped MS "
          + "instrument. The intensities are usually divided by the injection time (accumulation time "
          + "for normalization - this is reverted). Without injection time, this module leaves the original data.";

  @Override
  public @NotNull String getName() {
    return MODULE_NAME;
  }

  @Override
  public @NotNull String getDescription() {
    return MODULE_DESCRIPTION;
  }

  @Override
  @NotNull
  public ExitCode runModule(@NotNull MZmineProject project, @NotNull ParameterSet parameters,
      @NotNull Collection<Task> tasks, @NotNull Instant moduleCallDate) {

    RawDataFile[] dataFiles = parameters.getParameter(
            DenormalizeScansMultiplyByInjectTimeParameters.dataFiles).getValue()
        .getMatchingRawDataFiles();
    final MemoryMapStorage storage = MemoryMapStorage.forMassList();
    for (RawDataFile dataFile : dataFiles) {
      Task newTask = new DenormalizeScansMultiplyByInjectTimeTask(project, dataFile, parameters,
          storage, moduleCallDate);
      tasks.add(newTask);
    }

    return ExitCode.OK;

  }

  @Override
  public @NotNull MZmineModuleCategory getModuleCategory() {
    return MZmineModuleCategory.RAWDATAFILTERING;
  }

  @Override
  public @NotNull Class<? extends ParameterSet> getParameterSetClass() {
    return DenormalizeScansMultiplyByInjectTimeParameters.class;
  }

}
