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

/*
 * This module was prepared by Abi Sarvepalli, Christopher Jensen, and Zheng Zhang at the Dorrestein
 * Lab (University of California, San Diego).
 *
 * It is freely available under the GNU GPL licence of MZmine2.
 *
 * For any questions or concerns, please refer to:
 * https://groups.google.com/forum/#!forum/molecular_networking_bug_reports
 *
 * Credit to the Du-Lab development team for the initial commitment to the MGF export module.
 */

package io.github.mzmine.modules.io.export_features_sirius;

import io.github.mzmine.datamodel.features.FeatureListRow;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import io.github.mzmine.datamodel.MZmineProject;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineModuleCategory;
import io.github.mzmine.modules.MZmineProcessingModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.taskcontrol.Task;
import io.github.mzmine.util.ExceptionUtils;
import io.github.mzmine.util.ExitCode;

public class SiriusExportModule implements MZmineProcessingModule {
  private static final String MODULE_NAME = "Export for SIRIUS";
  private static final String MODULE_DESCRIPTION =
      "This method exports a MGF file that contains for each feature, (1) the deconvoluted MS1 isotopic pattern, and (2) the MS/MS spectrum (highest precursor ion intensity). This file can be open and processed with Sirius, https://bio.informatik.uni-jena.de/software/sirius/.";

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
      @NotNull Collection<Task> tasks) {
    SiriusExportTask task = new SiriusExportTask(parameters);
    tasks.add(task);
    return ExitCode.OK;

  }

  public static void exportSingleFeatureList(FeatureListRow row) {

    try {
      ParameterSet parameters =
          MZmineCore.getConfiguration().getModuleParameters(SiriusExportModule.class);

      ExitCode exitCode = parameters.showSetupDialog(true);
      if (exitCode != ExitCode.OK)
        return;
      // Open file
      final SiriusExportTask task = new SiriusExportTask(parameters);
      task.runSingleRow(row);
    } catch (Exception e) {
      e.printStackTrace();
      MZmineCore.getDesktop().displayErrorMessage(
          "Error while exporting feature to SIRIUS: " + ExceptionUtils.exceptionToString(e));
    }

  }

  public static void exportSingleRows(FeatureListRow[] row) {
    try {
      ParameterSet parameters =
          MZmineCore.getConfiguration().getModuleParameters(SiriusExportModule.class);

      ExitCode exitCode = parameters.showSetupDialog(true);
      if (exitCode != ExitCode.OK)
        return;
      // Open file
      final SiriusExportTask task = new SiriusExportTask(parameters);
      task.runSingleRows(row);
    } catch (Exception e) {
      e.printStackTrace();
      MZmineCore.getDesktop().displayErrorMessage(
          "Error while exporting feature to SIRIUS: " + ExceptionUtils.exceptionToString(e));
    }
  }

  @Override
  public @NotNull MZmineModuleCategory getModuleCategory() {
    return MZmineModuleCategory.FEATURELISTEXPORT;
  }

  @Override
  public @NotNull Class<? extends ParameterSet> getParameterSetClass() {
    return SiriusExportParameters.class;
  }

}

/*
 * "If you use the SIRIUS export module, cite MZmine and the following article: Duhrkop, M.
 * Fleischauer, M. Ludwig, A. A. Aksenov, A. V. Melnik, M. Meusel, P. C. Dorrestein, J. Rousu, and
 * S. Boecker, Sirius 4: a rapid tool for turning tandem mass spectra into metabolite structure
 * information, Nat methods, 2019. 8:5
 *
 * [Link](http://dx.doi.org/10.1038/s41592-019-0344-8), and
 * [Link](https://jcheminf.springeropen.com/articles/10.1186/s13321-016-0116-8)
 */
