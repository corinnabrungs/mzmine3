/*
 * Copyright 2006-2018 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.datamodel.data.types.fx;

import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.data.ModularDataModel;
import io.github.mzmine.datamodel.data.ModularFeature;
import io.github.mzmine.datamodel.data.ModularFeatureListRow;
import io.github.mzmine.datamodel.data.types.DataType;
import javafx.beans.property.Property;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;

/**
 * Default data cell type factory
 * 
 * @author Robin Schmid (robinschmid@uni-muenster.de)
 *
 * @param <T>
 */
public class DataTypeCellValueFactory<T extends Property<?>>
    implements Callback<TreeTableColumn.CellDataFeatures<ModularFeatureListRow, T>, T>,
    Function<CellDataFeatures<ModularFeatureListRow, T>, ModularDataModel> {
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private RawDataFile raw;
  private DataType<T> type;
  private final @Nonnull Function<CellDataFeatures<ModularFeatureListRow, T>, ModularDataModel> dataMapSupplier;

  public DataTypeCellValueFactory(RawDataFile raw, DataType<T> type) {
    this(raw, type, null);
  }

  public DataTypeCellValueFactory(RawDataFile raw, DataType<T> type,
      Function<CellDataFeatures<ModularFeatureListRow, T>, ModularDataModel> dataMapSupplier) {
    this.type = type;
    this.raw = raw;
    this.dataMapSupplier = dataMapSupplier == null ? this : dataMapSupplier;
  }

  @Override
  public T call(CellDataFeatures<ModularFeatureListRow, T> param) {
    final ModularDataModel map = dataMapSupplier.apply(param);
    if (map == null) {
      logger.log(Level.WARNING,
          "There was no DataTypeMap for the column of DataType "
              + type.getClass().descriptorString() + " and raw file "
              + (raw == null ? "NONE" : raw.getName()));
      return null;
    }

    return map.get(type);
  }


  /**
   * The default way to get the DataMap. FeatureListRow (for raw==null), Feature for raw!=null.
   */
  @Override
  public ModularDataModel apply(CellDataFeatures<ModularFeatureListRow, T> param) {
    if (raw != null) {
      // find data type map for feature for this raw file
      Map<RawDataFile, ModularFeature> features = param.getValue().getValue().getFeatures();
      // no features
      if (features.get(raw) == null)
        return null;
      return features.get(raw);
    } else {
      // use feature list row DataTypeMap
      return param.getValue().getValue();
    }
  }
}
