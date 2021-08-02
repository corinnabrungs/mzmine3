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

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.RowGroup;
import io.github.mzmine.datamodel.features.correlation.CorrelationRowGroup;
import io.github.mzmine.datamodel.features.correlation.R2RCorrelationData;
import io.github.mzmine.datamodel.features.correlation.R2RMap;
import io.github.mzmine.datamodel.features.correlation.RowsRelationship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javafx.collections.ObservableList;

/**
 * @author Robin Schmid (https://github.com/robinschmid)
 */
public class CorrelationGroupingUtils {

  private static final Logger logger = Logger.getLogger(CorrelationGroupingUtils.class.getName());

  /**
   * Set all groups to their rows
   *
   * @param groups the list of rows
   */
  public static void setGroupsToAllRows(List<RowGroup> groups) {
    for (RowGroup g : groups) {
      g.setGroupToAllRows();
    }
  }

  /**
   * Create list of correlated rows
   *
   * @param flist feature list
   * @return a list of all groups within the feature list
   */
  public static List<RowGroup> createCorrGroups(FeatureList flist) {
    logger.log(Level.INFO, "Corr: Creating correlation groups for {0}", flist.getName());

    try {
      R2RMap<RowsRelationship> corrMap = flist.getMs1CorrelationMap();
      if (corrMap == null) {
        logger.log(Level.INFO,
            "Feature list ({0}) contains no grouped rows. First run a grouping module",
            flist.getName());
        return List.of();
      }

      List<RowGroup> groups = new ArrayList<>();
      HashMap<FeatureListRow, CorrelationRowGroup> used = new HashMap<>();

      int c = 0;
      ObservableList<RawDataFile> raw = flist.getRawDataFiles();
      // add all connections
      for (Entry<Integer, RowsRelationship> e : corrMap.entrySet()) {
        RowsRelationship r2r = e.getValue();
        FeatureListRow rowA = r2r.getRowA();
        FeatureListRow rowB = r2r.getRowB();
        if (r2r instanceof R2RCorrelationData data) {
          // already added?
          CorrelationRowGroup group = used.get(rowA);
          CorrelationRowGroup group2 = used.get(rowB);
          // merge groups if both present
          if (group != null && group2 != null && group.getGroupID() != group2.getGroupID()) {
            // copy all to group1 and remove g2
            for (FeatureListRow r : group2.getRows()) {
              group.add(r);
              used.put(r, group);
            }
            groups.remove(group2);
          } else if (group == null && group2 == null) {
            // create new group with both rows
            group = new CorrelationRowGroup(raw, groups.size());
            group.addAll(rowA, rowB);
            groups.add(group);
            // mark as used
            used.put(rowA, group);
            used.put(rowB, group);
          } else if (group2 == null) {
            group.add(rowB);
            used.put(rowB, group);
          } else if (group == null) {
            group2.add(rowA);
            used.put(rowA, group2);
          }
        }
        // report back progress
        c++;
      }
      // sort by retention time
      Collections.sort(groups);

      // reset index
      for (int i = 0; i < groups.size(); i++) {
        groups.get(i).setGroupID(i);
      }

      logger.info("Corr: DONE: Creating correlation groups");
      return groups;
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while creating groups", e);
      return null;
    }
  }


  /**
   * Stream all R2RCorrelationData found in PKLRowGroups (is distinct)
   *
   * @param FeatureList
   * @return
   */
  public static Stream<R2RCorrelationData> streamFrom(FeatureList FeatureList) {
    if (FeatureList.getGroups() == null) {
      return Stream.empty();
    }
    return FeatureList.getGroups().stream().filter(g -> g instanceof CorrelationRowGroup)
        .map(g -> ((CorrelationRowGroup) g).getCorrelation()).flatMap(Arrays::stream) // R2GCorr
        .flatMap(r2g -> r2g.getCorrelation() == null ? null
            : r2g.getCorrelation().stream() //
                .filter(r2r -> r2r.getRowA().equals(r2g.getRow()))); // a is always the lower id
  }

  public static Stream<R2RCorrelationData> streamFrom(FeatureListRow[] rows) {
    return Arrays.stream(rows).map(FeatureListRow::getGroup).filter(Objects::nonNull)
        .filter(g -> g instanceof CorrelationRowGroup).distinct()
        .map(g -> ((CorrelationRowGroup) g).getCorrelation())
        .flatMap(Arrays::stream) // R2GCorr
        .flatMap(r2g -> r2g.getCorrelation() == null ? null
            : r2g.getCorrelation().stream() //
                .filter(r2r -> r2r.getRowA().equals(r2g.getRow()))); // a is always the lower id
  }
}
