/*
 * Copyright 2006-2022 The MZmine Development Team
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

package io.github.mzmine.modules.visualization.networking.visual;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.features.FeatureList;
import io.github.mzmine.datamodel.features.FeatureListRow;
import io.github.mzmine.datamodel.features.correlation.R2RMap;
import io.github.mzmine.datamodel.features.correlation.RowsRelationship;
import io.github.mzmine.datamodel.features.correlation.RowsRelationship.Type;
import io.github.mzmine.modules.dataprocessing.id_gnpsresultsimport.GNPSLibraryMatch;
import io.github.mzmine.util.FilteredGraphUtils;
import io.github.mzmine.util.GraphStreamUtils;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class FeatureNetworkPane extends NetworkPane {

  /**
   * Max width in graph units. 1 is the distance between nodes
   */
  public static final float MAX_NODE_WIDTH_GU = 0.3f;
  public static final float MIN_NODE_WIDTH_GU = 0.02f;
  private static final Logger logger = Logger.getLogger(FeatureNetworkPane.class.getName());

  // currently set dynamic node styles like color, size, label
  private final EnumMap<GraphStyleAttribute, NodeAtt> dynamicNodeStyle = new EnumMap<>(
      GraphStyleAttribute.class);
  private final EnumMap<GraphStyleAttribute, EdgeAtt> dynamicEdgeStyle = new EnumMap<>(
      GraphStyleAttribute.class);
  // style values need to be set as float - double crashes in the javafx thread for graphstream
  private final Map<NodeAtt, Range<Float>> attributeRanges = new HashMap<>();
  // for non numeric values: store all objects and provide indexes
  private final Map<NodeAtt, Map<String, Integer>> attributeCategoryValuesMap = new HashMap<>();

  // the network generator
  private final FeatureNetworkGenerator generator = new FeatureNetworkGenerator();
  // data
  private FeatureList featureList;
  private FeatureListRow[] rows;
  private Map<Type, R2RMap<RowsRelationship>> relationMaps;

  // currently set values
  private boolean onlyBest;

  private final IntegerProperty bNeighbors = new SimpleIntegerProperty(1);
  private boolean showNetRelationsEdges;
  private boolean collapse = true;
  private boolean showIonEdges = true;
  private boolean showMs2SimEdges;
  private boolean ms1FeatureShapeEdges = false;

  private Graph fullGraph;

  /**
   * Create the panel.
   */
  public FeatureNetworkPane() {
    this(false);
  }

  public FeatureNetworkPane(boolean showTitle) {
    super("Ion identity networks (IINs)", showTitle);
    fullGraph = new MultiGraph("Multi-Graph");
    setFilteredGraph();
    addMenu();
  }
  private void addMenu() {
    Pane menu = getPnSettings();
    menu.setVisible(true);

    showEdgeLabels = false;
    showNodeLabels = true;
    collapse = true;

    // defaults
    dynamicNodeStyle.put(GraphStyleAttribute.COLOR, NodeAtt.RT);
    dynamicNodeStyle.put(GraphStyleAttribute.SIZE, NodeAtt.LOG10_SUM_INTENSITY);
    dynamicNodeStyle.put(GraphStyleAttribute.LABEL, NodeAtt.LABEL);
    dynamicNodeStyle.put(GraphStyleAttribute.CLASS, null);

    menu.getChildren().add(new Label("Color:"));
    ComboBox<NodeAtt> comboNodeColor = new ComboBox<>(
        FXCollections.observableArrayList(NodeAtt.values()));
    comboNodeColor.setTooltip(new Tooltip("Node color"));
    comboNodeColor.getSelectionModel().select(NodeAtt.RT);
    menu.getChildren().add(comboNodeColor);
    comboNodeColor.setOnAction(e -> {
      NodeAtt selectedItem = comboNodeColor.getSelectionModel().getSelectedItem();
      setAttributeForAllNodes(GraphStyleAttribute.COLOR, selectedItem);
    });

    menu.getChildren().add(new Label("Size:"));
    ComboBox<NodeAtt> comboNodeSize = new ComboBox<>(
        FXCollections.observableArrayList(NodeAtt.values()));
    comboNodeSize.setTooltip(new Tooltip("Node size"));
    comboNodeSize.getSelectionModel().select(NodeAtt.LOG10_SUM_INTENSITY);
    menu.getChildren().add(comboNodeSize);
    comboNodeSize.setOnAction(e -> {
      NodeAtt selectedItem = comboNodeSize.getSelectionModel().getSelectedItem();
      setAttributeForAllNodes(GraphStyleAttribute.SIZE, selectedItem);
    });

    menu.getChildren().add(new Label("Label:"));
    ComboBox<NodeAtt> comboNodeLabel = new ComboBox<>(
        FXCollections.observableArrayList(NodeAtt.values()));
    comboNodeLabel.setTooltip(new Tooltip("Node label"));
    comboNodeLabel.getSelectionModel().select(NodeAtt.LABEL);
    menu.getChildren().add(comboNodeLabel);
    comboNodeLabel.setOnAction(e -> {
      NodeAtt selectedItem = comboNodeLabel.getSelectionModel().getSelectedItem();
      setAttributeForAllNodes(GraphStyleAttribute.LABEL, selectedItem);
    });

    menu.getChildren().add(new Label("Edge:"));
    ComboBox<EdgeAtt> comboEdgeLabel = new ComboBox<>(
        FXCollections.observableArrayList(EdgeAtt.values()));
    comboEdgeLabel.setTooltip(new Tooltip("Edge label"));
    comboEdgeLabel.getSelectionModel().select(EdgeAtt.LABEL);
    menu.getChildren().add(comboEdgeLabel);
    comboEdgeLabel.setOnAction(e -> {
      EdgeAtt selectedItem = comboEdgeLabel.getSelectionModel().getSelectedItem();
      setAttributeForAllEdges(GraphStyleAttribute.LABEL, selectedItem);
    });

    menu.setStyle("-fx-padding: 15; -fx-spacing: 15;");

    // #######################################################
    // add buttons
    ToggleButton toggleCollapseIons = new ToggleButton("Collapse ions");
    toggleCollapseIons.setMaxWidth(Double.MAX_VALUE);
    toggleCollapseIons.setSelected(collapse);
    toggleCollapseIons.selectedProperty()
        .addListener((o, old, value) -> collapseIonNodes(toggleCollapseIons.isSelected()));

    ToggleButton toggleShowMS2SimEdges = new ToggleButton("Show MS2 sim");
    toggleShowMS2SimEdges.setMaxWidth(Double.MAX_VALUE);
    toggleShowMS2SimEdges.setSelected(true);
    toggleShowMS2SimEdges.selectedProperty()
        .addListener((o, old, value) -> setShowMs2SimEdges(toggleShowMS2SimEdges.isSelected()));

    ToggleButton toggleShowRelations = new ToggleButton("Show relational edges");
    toggleShowRelations.setMaxWidth(Double.MAX_VALUE);
    toggleShowRelations.setSelected(true);
    toggleShowRelations.selectedProperty()
        .addListener((o, old, value) -> setConnectByNetRelations(toggleShowRelations.isSelected()));

    ToggleButton toggleShowIonIdentityEdges = new ToggleButton("Show ion edges");
    toggleShowIonIdentityEdges.setMaxWidth(Double.MAX_VALUE);
    toggleShowIonIdentityEdges.setSelected(true);
    toggleShowIonIdentityEdges.selectedProperty().addListener(
        (o, old, value) -> showIonIdentityEdges(toggleShowIonIdentityEdges.isSelected()));

    ToggleButton toggleShowEdgeLabel = new ToggleButton("Show edge label");
    toggleShowEdgeLabel.setMaxWidth(Double.MAX_VALUE);
    toggleShowEdgeLabel.setSelected(showEdgeLabels);
    toggleShowEdgeLabel.selectedProperty()
        .addListener((o, old, value) -> showEdgeLabels(toggleShowEdgeLabel.isSelected()));

    ToggleButton toggleShowNodeLabel = new ToggleButton("Show node label");
    toggleShowNodeLabel.setMaxWidth(Double.MAX_VALUE);
    toggleShowNodeLabel.setSelected(showNodeLabels);
    toggleShowNodeLabel.selectedProperty()
        .addListener((o, old, value) -> showNodeLabels(toggleShowNodeLabel.isSelected()));

    Button showGNPSMatches = new Button("GNPS matches");
    showGNPSMatches.setMaxWidth(Double.MAX_VALUE);
    showGNPSMatches.setOnAction(e -> showGNPSMatches());

    Button showLibraryMatches = new Button("Library matches");
    showLibraryMatches.setMaxWidth(Double.MAX_VALUE);
    showLibraryMatches.setOnAction(e -> showLibraryMatches());

    Spinner<Integer> nodeNeighbours = new Spinner<>(1, Integer.MAX_VALUE, 3, 1);
    Label l = new Label("No. of node neighbours:");
    bNeighbors.bind(nodeNeighbours.valueProperty());

    Button updateGraphButton = new Button("Update graph");
    updateGraphButton.setMaxWidth(Double.MAX_VALUE);
    updateGraphButton.setOnAction(e -> updateGraph());

    Button showOriginalGraphButton = new Button("Show original graph");
    showOriginalGraphButton.setMaxWidth(Double.MAX_VALUE);


    // finally add buttons
    VBox pnRightMenu = new VBox(4, toggleCollapseIons, toggleShowMS2SimEdges, toggleShowRelations,
        toggleShowIonIdentityEdges, toggleShowEdgeLabel, toggleShowNodeLabel, showGNPSMatches,
        showLibraryMatches, l, nodeNeighbours, updateGraphButton, showOriginalGraphButton);
    pnRightMenu.setSpacing(10);
    pnRightMenu.setPadding(new Insets(0, 20, 10, 20));
    this.setRight(pnRightMenu);
  }

  private void setAttributeForAllNodes(GraphStyleAttribute attribute, NodeAtt prop) {
    dynamicNodeStyle.put(attribute, prop);
    switch (attribute) {
      case COLOR -> applyNodeColorStyle();
      case LABEL -> applyLabelStyle(GraphObject.NODE);
      case SIZE -> applyNodeSizeStyle();
    }
  }

  private void setAttributeForAllEdges(GraphStyleAttribute attribute, EdgeAtt prop) {
    dynamicEdgeStyle.put(attribute, prop);
    switch (attribute) {
      case COLOR -> applyNodeColorStyle();
      case LABEL -> applyLabelStyle(GraphObject.EDGE);
      case SIZE -> applyNodeSizeStyle();
    }
  }

  private void updateGraph() {
    if (getMouseClickedNode() == null) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setContentText("Please click on any node First!!");
      alert.showAndWait();
    }
    else
    {
      generator.createGraphWithNeighboringNodes(graph, GraphStreamUtils.getNodeNeighbors(getMouseClickedNode(),bNeighbors.get()));
    }
  }


  /**
   * Show GNPS library match
   */
  private void showGNPSMatches() {
    int n = 0;
    for (Node node : fullGraph) {
      String name = (String) node.getAttribute(GNPSLibraryMatch.ATT.COMPOUND_NAME.getKey());
      if (name != null) {
        node.setAttribute("ui.label", name);
        n++;
      }
    }
    logger.info("Show " + n + " GNPS library matches");
  }

  /**
   * Show spectral library matches
   */
  private void showLibraryMatches() {
    int n = 0;
    for (Node node : fullGraph) {
      String name = (String) node.getAttribute(NodeAtt.SPECTRAL_LIB_MATCH_SUMMARY.toString());
      if (name != null) {
        node.setAttribute("ui.label", name);
        n++;
      }
    }
    logger.info("Show " + n + " spectral library matches");
  }

  private void showIonIdentityEdges(boolean selected) {
    showIonEdges = selected;
    collapseIonNodes(collapse);
  }

  public void collapseIonNodes(boolean collapse) {
    this.collapse = collapse;
    for (Node node : fullGraph) {
      NodeType type = (NodeType) node.getAttribute(NodeAtt.TYPE.toString());
      if (type != null) {
        switch (type) {
          case NEUTRAL_LOSS_CENTER:
          case ION_FEATURE:
            setVisible(node, !collapse);
            break;
          case NEUTRAL_M:
            break;
          case SINGLE_FEATURE:
            break;
          default:
            break;
        }
      }
    }

    fullGraph.edges().forEach(edge -> {
      EdgeType type = (EdgeType) edge.getAttribute(EdgeAtt.TYPE.toString());
      if (type != null) {
        switch (type) {
          case ION_IDENTITY:
            setVisible(edge, !collapse && showIonEdges);
            break;
          case MS2_SIMILARITY_NEUTRAL_M_TO_FEATURE:
          case MS2_SIMILARITY_NEUTRAL_M:
          case MS2_SIMILARITY:
            setVisible(edge, showMs2SimEdges);
            break;
          case NETWORK_RELATIONS:
            setVisible(edge, showNetRelationsEdges);
            break;
          default:
            break;
        }
      }
      // only if both nodes are visible
      if (!isVisible(edge.getSourceNode()) || !isVisible(edge.getTargetNode())) {
        setVisible(edge, false);
      }
    });
  }

  @Override
  public void clear() {
    super.clear();
  }

  public void createNewGraph(FeatureListRow[] rows) {
    this.rows = rows;
    attributeRanges.clear();
    attributeCategoryValuesMap.clear();

    clear();
    generator.createNewGraph(rows, fullGraph, onlyBest, relationMaps, ms1FeatureShapeEdges);
    clearSelections();
    showEdgeLabels(showEdgeLabels);
    showNodeLabels(showNodeLabels);

    // last state
    collapseIonNodes(collapse);

    // apply dynamic style
    applyDynamicStyles();
  }

  private void applyDynamicStyles() {
    applyNodeSizeStyle();
    applyNodeColorStyle();
    applyLabelStyle(GraphObject.NODE);

    // edges
    applyLabelStyle(GraphObject.EDGE);

  }

  private void applyNodeSizeStyle() {
    NodeAtt nodeAttSize = dynamicNodeStyle.get(GraphStyleAttribute.SIZE);
    // min / max values of the specific attributes
    final Range<Float> sizeValueRange =
        nodeAttSize.isNumber() ? attributeRanges.computeIfAbsent(nodeAttSize,
            nodeAtt -> computeValueRange(rows, nodeAttSize)) : null;
    // for non numeric values - give each Object an index
    final Map<String, Integer> sizeValueMap = nodeAttSize.isNumber() ? null
        : attributeCategoryValuesMap.computeIfAbsent(nodeAttSize,
            att -> indexAllValues(nodeAttSize));
    final int numSizeValues = sizeValueMap == null ? 0 : sizeValueMap.size();

    for (Node node : fullGraph) {
      NodeType type = (NodeType) node.getAttribute(NodeAtt.TYPE.toString());
      if (type == NodeType.NEUTRAL_M || type == NodeType.NEUTRAL_LOSS_CENTER) {
        continue;
      }
      // set size
      try {
        Object sizeValue = node.getAttribute(nodeAttSize.toString());
        if (sizeValue != null) {
          // differentiate between numeric values and a list of discrete values
          float size = 0;
          if (sizeValueRange != null) {
            size = interpolateIntensity(Float.parseFloat(sizeValue.toString()),
                sizeValueRange.lowerEndpoint(), sizeValueRange.upperEndpoint());
          } else if (sizeValueMap != null) {
            // non numeric values - use index
            int index = sizeValueMap.getOrDefault(sizeValue.toString(), 0);
            size = index / (float) numSizeValues;
          }
          size = Math.max(MIN_NODE_WIDTH_GU, size * MAX_NODE_WIDTH_GU);
          // set as graphical units for zoom effect
          // otherwise use fixed number of pixels
          node.setAttribute("ui.size", size + "gu");
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, "Error while setting size attribute. " + ex.getMessage(), ex);
      }
    }
  }

  private void applyNodeColorStyle() {
    NodeAtt nodeAttColor = dynamicNodeStyle.get(GraphStyleAttribute.COLOR);
    final Range<Float> colorValueRange =
        nodeAttColor.isNumber() ? attributeRanges.computeIfAbsent(nodeAttColor,
            nodeAtt -> computeValueRange(rows, nodeAttColor)) : null;

    final Map<String, Integer> colorValueMap = nodeAttColor.isNumber() ? null
        : attributeCategoryValuesMap.computeIfAbsent(nodeAttColor,
            att -> indexAllValues(nodeAttColor));
    final int numColorValues = colorValueMap == null ? 0 : colorValueMap.size();

    for (Node node : fullGraph) {
      NodeType type = (NodeType) node.getAttribute(NodeAtt.TYPE.toString());
      if (type == NodeType.NEUTRAL_M || type == NodeType.NEUTRAL_LOSS_CENTER) {
        continue;
      }
      try {
        if (nodeAttColor == NodeAtt.NONE) {
          node.removeAttribute("ui.class");
        } else {
          // make colors a gradient
          Object colorValue = node.getAttribute(nodeAttColor.toString());
          if (colorValue != null) {
            node.setAttribute("ui.class", "GRADIENT");
            // differentiate between numeric values and a list of discrete values
            if (colorValueRange != null) {
              final float interpolated = interpolateIntensity(
                  Float.parseFloat(colorValue.toString()), colorValueRange.lowerEndpoint(),
                  colorValueRange.upperEndpoint());
              node.setAttribute("ui.color", interpolated);
            } else if (colorValueMap != null) {
              // non numeric values - use index
              int index = colorValueMap.getOrDefault(colorValue.toString(), 0);
              node.setAttribute("ui.color", index / (float) numColorValues);
            }
          }
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, "Error while setting color attribute. " + ex.getMessage(), ex);
        logger.log(Level.SEVERE, ex.getMessage(), ex);
      }
    }
  }

  private void applyLabelStyle(GraphObject target) {
    final String att = getStyleAttribute(target, GraphStyleAttribute.LABEL);
    target.stream(fullGraph).forEach(node -> {
      try {
        node.setAttribute("ui.label", getOrElseString(node, att, ""));
      } catch (Exception ex) {
        logger.log(Level.SEVERE, "Error while setting label attribute. " + ex.getMessage(), ex);
      }
    });
  }

  /**
   * Get style attribute
   *
   * @param target   the target object to style
   * @param styleAtt the styling attribute of the node or edge
   * @return either a {@link NodeAtt} or {@link EdgeAtt}
   */
  public String getStyleAttribute(GraphObject target, GraphStyleAttribute styleAtt) {
    return Objects.toString(getDynamicStyle(target).get(styleAtt), null);
  }

  /**
   * get the dynamic style map for target
   *
   * @param target edge or node as target
   * @return style map
   */
  public EnumMap<GraphStyleAttribute, ?> getDynamicStyle(GraphObject target) {
    return switch (target) {
      case NODE -> dynamicNodeStyle;
      case EDGE -> dynamicEdgeStyle;
    };
  }

  /**
   * Index all objects found in all rows for an attribute
   *
   * @param attribute the node attribute for this row
   * @return map of all objects found and their idexes in their original order
   */
  private Map<String, Integer> indexAllValues(NodeAtt attribute) {
    Map<String, Integer> map = new HashMap<>();
    int currentIndex = 0;
    for (Node node : fullGraph) {
      try {
        String object = Objects.requireNonNullElse(node.getAttribute(attribute.toString()), "")
            .toString();
        if (object.isEmpty()) {
          continue;
        }
        if (!map.containsKey(object)) {
          map.put(object, currentIndex);
          currentIndex++;
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
    return map;
  }

  private Range<Float> computeValueRange(FeatureListRow[] rows, NodeAtt attribute) {
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;

    for (FeatureListRow row : rows) {
      try {
        Object object = attribute.getValue(row);
        if (object == null) {
          continue;
        }
        float value = Float.parseFloat(object.toString());
        if (value < min) {
          min = value;
        }
        if (value > max) {
          max = value;
        }
      } catch (Exception ex) {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }

    if (Float.compare(Float.POSITIVE_INFINITY, min) == 0) {
      min = 0;
    }
    if (Float.compare(Float.NEGATIVE_INFINITY, max) == 0) {
      max = 1;
    }
    return Range.closed(min, max);
  }

  /**
   * ratio (0-1) between min and maxIntensity
   *
   * @param value the intensity value
   * @return a value between 0-1 (including)
   */
  protected float interpolateIntensity(float value, float min, float max) {
    return (float) Math.min(1.0, Math.max(0.0, (value - min) / (max - min)));
  }

  public void setConnectByNetRelations(boolean connectByNetRelations) {
    this.showNetRelationsEdges = connectByNetRelations;
    collapseIonNodes(collapse);
  }

  public void setOnlyBest(boolean onlyBest) {
    this.onlyBest = onlyBest;
  }

  public void dispose() {
    fullGraph.clear();
  }

  public void setShowMs2SimEdges(boolean ms2SimEdges) {
    this.showMs2SimEdges = ms2SimEdges;
    collapseIonNodes(collapse);
  }

  public FeatureList getFeatureList() {
    return featureList;
  }

  /**
   * All the peaklist
   */
  public void setFeatureList(FeatureList featureList) {
    this.featureList = featureList;
    if (featureList != null) {
      relationMaps = featureList.getRowMaps();
      createNewGraph(featureList.getRows().toArray(FeatureListRow[]::new));
    } else {
      clear();
    }
  }

  public void setUseMs1FeatureShapeEdges(boolean ms1FeatureShapeEdges) {
    this.ms1FeatureShapeEdges = ms1FeatureShapeEdges;
  }
 public void setFilteredGraph()
 {
   FilteredGraphUtils fgu = new FilteredGraphUtils();
   fgu.addContentsOfFullGraph(fullGraph,graph);
 }

 public Graph getFullGraph()
 {
   return fullGraph;
 }
}
