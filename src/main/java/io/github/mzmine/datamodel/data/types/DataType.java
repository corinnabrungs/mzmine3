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

package io.github.mzmine.datamodel.data.types;

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.data.ModularDataModel;
import io.github.mzmine.datamodel.data.ModularFeatureListRow;
import io.github.mzmine.datamodel.data.types.fx.DataTypeCellFactory;
import io.github.mzmine.datamodel.data.types.fx.DataTypeCellValueFactory;
import io.github.mzmine.datamodel.data.types.fx.EditableDataTypeCellFactory;
import io.github.mzmine.datamodel.data.types.modifiers.EditableColumnType;
import io.github.mzmine.datamodel.data.types.modifiers.NullColumnType;
import io.github.mzmine.datamodel.data.types.modifiers.SubColumnsFactory;
import javafx.beans.property.Property;
import javafx.scene.control.TreeTableColumn;

/**
 * Class of data types: Provides formatters. Should be added to one {@link ModularDataModel}
 * 
 * @author Robin Schmid (robinschmid@uni-muenster.de)
 *
 * @param <T>
 */
public abstract class DataType<T extends Property<?>> {
  protected Logger logger = Logger.getLogger(this.getClass().getName());

  public DataType() {}

  /**
   * A formatted string representation of the value
   * 
   * @return the formatted representation of the value (or an empty String)
   */
  @Nonnull
  public String getFormattedString(@Nonnull T property) {
    if (property.getValue() != null)
      return property.getValue().toString();
    else
      return "";
  }

  /**
   * The header string (name) of this data type
   * 
   * @return
   */
  @Nonnull
  public abstract String getHeaderString();

  /**
   * Creates a TreeTableColumn or null if the value is not represented in a column. A
   * {@link SubColumnsFactory} DataType can also add multiple sub columns to the main column
   * generated by this class.
   * 
   * @param raw null if this is a FeatureListRow column. For Feature columns: the raw data file
   *        specifies the feature.
   * 
   * @return the TreeTableColumn or null if this DataType.value is not represented in a column
   */
  @Nullable
  public TreeTableColumn<ModularFeatureListRow, T> createColumn(final @Nullable RawDataFile raw) {
    if (this instanceof NullColumnType)
      return null;
    // create column
    TreeTableColumn<ModularFeatureListRow, T> col = new TreeTableColumn<>(getHeaderString());

    if (this instanceof SubColumnsFactory) {
      col.setSortable(false);
      // add sub columns (no value factory needed for parent column)
      List<TreeTableColumn<ModularFeatureListRow, ?>> children =
          ((SubColumnsFactory) this).createSubColumns(raw);
      col.getColumns().addAll(children);
      return col;
    } else {
      col.setSortable(true);
      // define observable
      DataTypeCellValueFactory cvFactory = new DataTypeCellValueFactory<>(raw, this);
      col.setCellValueFactory(cvFactory);
      // value representation
      if (this instanceof EditableColumnType) {
        col.setCellFactory(new EditableDataTypeCellFactory<>(raw, this));
        col.setEditable(true);
        col.setOnEditCommit(event -> {
          T data = event.getNewValue();
          if (data != null) {
            if (raw == null)
              event.getRowValue().getValue().set(this, data);
            else
              event.getRowValue().getValue().getFeatures().get(raw).set(this, data);
          }
        });
      } else
        col.setCellFactory(new DataTypeCellFactory<>(raw, this));
    }
    return col;
  }

  public boolean checkValidValue(Object value) {
    if (value == null)
      return true;
    else {
      try {
        cast(value);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
  }

  public T cast(Object value) {
    return (T) value;
  }

  // TODO dirty hack to make this a "singleton"
  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
