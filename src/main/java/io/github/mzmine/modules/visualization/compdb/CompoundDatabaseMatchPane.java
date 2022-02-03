package io.github.mzmine.modules.visualization.compdb;

import io.github.mzmine.datamodel.features.ModularFeatureListRow;
import io.github.mzmine.datamodel.features.compoundannotations.CompoundDBAnnotation;
import io.github.mzmine.datamodel.features.types.DataType;
import io.github.mzmine.datamodel.features.types.annotations.CompoundNameType;
import io.github.mzmine.datamodel.features.types.annotations.InChIStructureType;
import io.github.mzmine.datamodel.features.types.annotations.compounddb.DatabaseMatchInfoType;
import io.github.mzmine.datamodel.features.types.annotations.iin.IonTypeType;
import io.github.mzmine.datamodel.features.types.numbers.NeutralMassType;
import io.github.mzmine.datamodel.features.types.numbers.PrecursorMZType;
import io.github.mzmine.modules.visualization.molstructure.Structure2DComponent;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.Nullable;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

public class CompoundDatabaseMatchPane extends BorderPane {

  private static final Logger logger = Logger.getLogger(CompoundDatabaseMatchPane.class.getName());

  private static final String VALUE_UNAVAILABLE = "N/A";
  private final GridPane entries;
  private final CompoundDBAnnotation annotation;
  private final ModularFeatureListRow row

  private static final List<DataType<?>> staticFields = List.of(new CompoundNameType(),
      new NeutralMassType(), new IonTypeType(), new PrecursorMZType(), new DatabaseMatchInfoType());

  public CompoundDatabaseMatchPane(CompoundDBAnnotation annotation) {
    this.annotation = annotation;
    entries = buildGridPane(annotation);
  }

  private static Node buildStructurePane(@Nullable final CompoundDBAnnotation annotation) {
    final String smiles = annotation.getSmiles();
    final String inchi = annotation.get(new InChIStructureType());

    IAtomContainer structure = null;
    if (smiles != null) {
      structure = parseSmiles(inchi);
    } else if (inchi != null) {
      structure = parseInchi(inchi);
    }

    try {
      return structure != null ? new Structure2DComponent(structure) : new FlowPane();
    } catch (CDKException e) {
      logger.log(Level.WARNING, "Cannot initialize Structure2DComponent.", e);
      return null;
    }
  }

  @Nullable
  private static IAtomContainer parseInchi(String inchi) {
    InChIGeneratorFactory factory;
    IAtomContainer molecule;
    if (inchi != null) {
      try {
        factory = InChIGeneratorFactory.getInstance();
        // Get InChIToStructure
        InChIToStructure inchiToStructure = factory.getInChIToStructure(inchi,
            DefaultChemObjectBuilder.getInstance());
        molecule = inchiToStructure.getAtomContainer();
        return molecule;
      } catch (CDKException e) {
        String errorMessage = "Could not load 2D structure\n" + "Exception: ";
        logger.log(Level.WARNING, errorMessage, e);
        return null;
      }
    } else {
      return null;
    }
  }

  private static IAtomContainer parseSmiles(String smiles) {
    SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
    IAtomContainer molecule;
    if (smilesParser != null) {
      try {
        molecule = smilesParser.parseSmiles(smiles);
        return molecule;
      } catch (InvalidSmilesException e1) {
        String errorMessage = "Could not load 2D structure\n" + "Exception: ";
        logger.log(Level.WARNING, errorMessage, e1);
        return null;
      }
    } else {
      return null;
    }
  }

  private static GridPane buildGridPane(@Nullable final CompoundDBAnnotation annotation) {
    final GridPane pane = new GridPane();
    if (annotation == null) {
      return pane;
    }

    int rowCounter = 0;
    for (DataType<?> type : staticFields) {
      final Label label = new Label(type.getHeaderString());
      Object value = annotation.get(type);
      String strValue = value != null ? type.getFormattedStringCheckType(value) : VALUE_UNAVAILABLE;
      final Label valueLabel = new Label(strValue);
      pane.add(label, 0, rowCounter);
      pane.add(valueLabel, 1, rowCounter);
      rowCounter++;
    }

    for (Entry<DataType<?>, Object> entry : annotation.getReadOnlyMap().entrySet()) {
      final DataType<?> type = entry.getKey();
      final Object value = entry.getValue();

      if (staticFields.contains(type)) {
        continue;
      }

      final Label label = new Label(type.getHeaderString());
      String strValue = value != null ? type.getFormattedStringCheckType(value) : VALUE_UNAVAILABLE;
      final Label valueLabel = new Label(strValue);
      valueLabel.setOnMouseClicked(e -> type.getDoubleClickAction());

      pane.add(label, 0, rowCounter);
      pane.add(valueLabel, 1, rowCounter);
      rowCounter++;
    }

    return pane;
  }
}
