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

package io.github.mzmine.util;

import java.util.List;
import org.jfree.data.xy.XYDataset;

import io.github.mzmine.modules.visualization.spectra.simplespectra.SpectraPlot;

public class SpectraPlotUtils {

  /**
   * Removes all label generators of datasets that are not of the given type.
   * 
   * @param plot Plot to apply this method to.
   * @param ignore List of class objects of the instances to ignore.
   */
  public static void clearDatasetLabelGenerators(SpectraPlot plot,
      List<Class<? extends XYDataset>> ignore) {
    for (int i = 0; i < plot.getXYPlot().getDatasetCount(); i++) {
      XYDataset dataset = plot.getXYPlot().getDataset(i);
      // check if object of dataset is an instance of ignore.class
      boolean remove = true;
      for (Class<? extends XYDataset> datasetClass : ignore) {
        if ((datasetClass.isInstance(dataset)))
          remove = false;
      }

      if (remove)
        plot.getXYPlot().getRendererForDataset(dataset).setDefaultItemLabelGenerator(null);
    }
  }

  /**
   * Removes all label generators of datasets that are not of the given type.
   * 
   * @param plot Plot to apply this method to.
   * @param ignore Class object of the instances to ignore.
   */
  public static void clearDatasetLabelGenerators(SpectraPlot plot,
      Class<? extends XYDataset> ignore) {
    for (int i = 0; i < plot.getXYPlot().getDatasetCount(); i++) {
      XYDataset dataset = plot.getXYPlot().getDataset(i);
      // check if object of dataset is an instance of ignore.class
      if (!(ignore.isInstance(dataset)))
        plot.getXYPlot().getRendererForDataset(dataset).setDefaultItemLabelGenerator(null);
    }
  }
}
