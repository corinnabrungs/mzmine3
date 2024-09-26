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

package io.github.mzmine.gui.mainwindow.workspaces;

import static io.github.mzmine.util.javafx.FxMenuUtil.addMenuItem;
import static io.github.mzmine.util.javafx.FxMenuUtil.addModuleMenuItem;
import static io.github.mzmine.util.javafx.FxMenuUtil.addModuleMenuItems;
import static io.github.mzmine.util.javafx.FxMenuUtil.addSeparator;

import io.github.mzmine.gui.DesktopService;
import io.github.mzmine.gui.MZmineGUI;
import io.github.mzmine.gui.WindowLocation;
import io.github.mzmine.gui.mainwindow.UsersTab;
import io.github.mzmine.gui.mainwindow.dependenciestab.DependenciesTab;
import io.github.mzmine.gui.mainwindow.introductiontab.MZmineIntroductionTab;
import io.github.mzmine.javafx.concurrent.threading.FxThread;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.MZmineRunnableModule;
import io.github.mzmine.modules.batchmode.BatchModeModule;
import io.github.mzmine.modules.batchmode.ModuleQuickSelectDialog;
import io.github.mzmine.modules.dataprocessing.featdet_adapchromatogrambuilder.ModularADAPChromatogramBuilderModule;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.minimumsearch.MinimumSearchFeatureResolverModule;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.noiseamplitude.NoiseAmplitudeResolverModule;
import io.github.mzmine.modules.dataprocessing.featdet_chromatogramdeconvolution.savitzkygolay.SavitzkyGolayResolverModule;
import io.github.mzmine.modules.dataprocessing.featdet_imagebuilder.ImageBuilderModule;
import io.github.mzmine.modules.dataprocessing.featdet_imsexpander.ImsExpanderModule;
import io.github.mzmine.modules.dataprocessing.featdet_maldispotfeaturedetection.MaldiSpotFeatureDetectionModule;
import io.github.mzmine.modules.dataprocessing.featdet_msn.MsnFeatureDetectionModule;
import io.github.mzmine.modules.dataprocessing.featdet_msn_tree.MsnTreeFeatureDetectionModule;
import io.github.mzmine.modules.dataprocessing.featdet_targeted.TargetedFeatureDetectionModule;
import io.github.mzmine.modules.dataprocessing.filter_clearannotations.ClearFeatureAnnotationsModule;
import io.github.mzmine.modules.dataprocessing.filter_interestingfeaturefinder.AnnotateIsomersModule;
import io.github.mzmine.modules.dataprocessing.group_imagecorrelate.ImageCorrelateGroupingModule;
import io.github.mzmine.modules.dataprocessing.group_metacorrelate.corrgrouping.CorrelateGroupingModule;
import io.github.mzmine.modules.dataprocessing.group_metacorrelate.export.ExportCorrAnnotationModule;
import io.github.mzmine.modules.dataprocessing.id_biotransformer.BioTransformerModule;
import io.github.mzmine.modules.dataprocessing.id_ecmscalcpotential.CalcEcmsPotentialModule;
import io.github.mzmine.modules.dataprocessing.id_formulapredictionfeaturelist.FormulaPredictionFeatureListModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.addionannotations.AddIonNetworkingModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.checkmsms.IonNetworkMSMSCheckModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.clearionids.ClearIonIdentitiesModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.formula.createavgformulas.CreateAvgNetworkFormulasModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.formula.prediction.FormulaPredictionIonNetworkModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.ionidnetworking.IonNetworkingModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.refinement.IonNetworkRefinementModule;
import io.github.mzmine.modules.dataprocessing.id_ion_identity_networking.relations.IonNetRelationsModule;
import io.github.mzmine.modules.dataprocessing.id_lipidid.annotation_modules.LipidAnnotationModule;
import io.github.mzmine.modules.dataprocessing.id_localcsvsearch.LocalCSVDatabaseSearchModule;
import io.github.mzmine.modules.dataprocessing.id_ms2search.Ms2SearchModule;
import io.github.mzmine.modules.dataprocessing.id_nist.NistMsSearchModule;
import io.github.mzmine.modules.dataprocessing.id_online_reactivity.OnlineLcReactivityModule;
import io.github.mzmine.modules.dataprocessing.id_spectral_library_match.SpectralLibrarySearchModule;
import io.github.mzmine.modules.io.export_ccsbase.CcsBaseExportModule;
import io.github.mzmine.modules.io.export_compoundAnnotations_csv.CompoundAnnotationsCSVExportModule;
import io.github.mzmine.modules.io.export_features_all_speclib_matches.ExportAllIdsGraphicalModule;
import io.github.mzmine.modules.io.export_features_csv.CSVExportModularModule;
import io.github.mzmine.modules.io.export_features_csv_legacy.LegacyCSVExportModule;
import io.github.mzmine.modules.io.export_features_featureML.FeatureMLExportModularModule;
import io.github.mzmine.modules.io.export_features_gnps.fbmn.GnpsFbmnExportAndSubmitModule;
import io.github.mzmine.modules.io.export_features_gnps.gc.GnpsGcExportAndSubmitModule;
import io.github.mzmine.modules.io.export_features_metaboanalyst.MetaboAnalystExportModule;
import io.github.mzmine.modules.io.export_features_mgf.AdapMgfExportModule;
import io.github.mzmine.modules.io.export_features_msp.AdapMspExportModule;
import io.github.mzmine.modules.io.export_features_mztabm.MZTabmExportModule;
import io.github.mzmine.modules.io.export_features_sirius.SiriusExportModule;
import io.github.mzmine.modules.io.export_features_sql.SQLExportModule;
import io.github.mzmine.modules.io.export_features_venn.VennExportModule;
import io.github.mzmine.modules.io.export_library_analysis_csv.LibraryAnalysisCSVExportModule;
import io.github.mzmine.modules.io.export_library_gnps_batch.GNPSLibraryBatchExportModule;
import io.github.mzmine.modules.io.export_msmsquality.MsMsQualityExportModule;
import io.github.mzmine.modules.io.export_network_graphml.NetworkGraphMlExportModule;
import io.github.mzmine.modules.io.import_feature_networks.ImportFeatureNetworksSimpleModule;
import io.github.mzmine.modules.io.projectload.ProjectLoadModule;
import io.github.mzmine.modules.io.projectsave.ProjectSaveAsModule;
import io.github.mzmine.modules.io.projectsave.ProjectSaveModule;
import io.github.mzmine.modules.io.spectraldbsubmit.batch.LibraryBatchGenerationModule;
import io.github.mzmine.modules.tools.batchwizard.BatchWizardModule;
import io.github.mzmine.modules.tools.isotopepatternpreview.IsotopePatternPreviewModule;
import io.github.mzmine.modules.tools.qualityparameters.QualityParametersModule;
import io.github.mzmine.modules.tools.timstofmaldiacq.TimsTOFMaldiAcquisitionModule;
import io.github.mzmine.modules.tools.timstofmaldiacq.imaging.SimsefImagingSchedulerModule;
import io.github.mzmine.modules.visualization.chromatogram.ChromatogramVisualizerModule;
import io.github.mzmine.modules.visualization.feat_histogram.FeatureHistogramPlotModule;
import io.github.mzmine.modules.visualization.fx3d.Fx3DVisualizerModule;
import io.github.mzmine.modules.visualization.histo_feature_correlation.FeatureCorrelationHistogramModule;
import io.github.mzmine.modules.visualization.image.ImageVisualizerModule;
import io.github.mzmine.modules.visualization.injection_time.InjectTimeAnalysisModule;
import io.github.mzmine.modules.visualization.intensityplot.IntensityPlotModule;
import io.github.mzmine.modules.visualization.kendrickmassplot.KendrickMassPlotModule;
import io.github.mzmine.modules.visualization.massvoltammogram.MassvoltammogramFromFeatureListModule;
import io.github.mzmine.modules.visualization.massvoltammogram.MassvoltammogramFromFileModule;
import io.github.mzmine.modules.visualization.msms.MsMsVisualizerModule;
import io.github.mzmine.modules.visualization.network_overview.FeatureNetworkOverviewModule;
import io.github.mzmine.modules.visualization.otherdetectors.multidetector.MultidetectorVisualizerModule;
import io.github.mzmine.modules.visualization.projectmetadata.ProjectMetadataTab;
import io.github.mzmine.modules.visualization.raw_data_summary.RawDataSummaryModule;
import io.github.mzmine.modules.visualization.rawdataoverview.RawDataOverviewModule;
import io.github.mzmine.modules.visualization.rawdataoverviewims.IMSRawDataOverviewModule;
import io.github.mzmine.modules.visualization.scan_histogram.CorrelatedFeaturesMzHistogramModule;
import io.github.mzmine.modules.visualization.scan_histogram.ScanHistogramModule;
import io.github.mzmine.modules.visualization.scatterplot.ScatterPlotVisualizerModule;
import io.github.mzmine.modules.visualization.spectra.msn_tree.MSnTreeVisualizerModule;
import io.github.mzmine.modules.visualization.twod.TwoDVisualizerModule;
import io.github.mzmine.modules.visualization.vankrevelendiagram.VanKrevelenDiagramModule;
import io.github.mzmine.util.javafx.WindowsMenu;
import io.mzio.links.MzioMZmineLinks;
import io.mzio.users.client.UserAuthStore;
import io.mzio.users.gui.fx.UsersViewState;
import io.mzio.users.user.CurrentUserService;
import java.util.EnumSet;
import java.util.List;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

public sealed interface Workspace permits Academic {

  String getName();

  MenuBar buildMainMenu(EnumSet<WorkspaceTags> tags);

  //--------------------------------------------------------
  // Default methods below here
  //--------------------------------------------------------

  /**
   * @return A duplicate-free list of all modules covered by the main menu of this workspace.
   */
  default List<Class<? extends MZmineRunnableModule>> getAllModules(EnumSet<WorkspaceTags> tags) {
    return buildMainMenu(tags).getMenus().stream().map(WorkspaceMenuUtils::extractModules)
        .flatMap(List::stream).distinct().toList();
  }

  default Menu buildDefaultProjectMenu() {
    final Menu menu = new Menu(MainMenuEntries.PROJECT.toString());
    final Menu recentProjects = new Menu("Recent projects");

    menu.setOnShowing(_ -> WorkspaceMenuUtils.fillRecentProjects(recentProjects));

    addModuleMenuItem(menu, ProjectLoadModule.class, KeyCode.O, KeyCombination.SHORTCUT_DOWN);
    addModuleMenuItem(menu, ProjectSaveModule.class, KeyCode.S, KeyCombination.SHORTCUT_DOWN);
    addModuleMenuItem(menu, ProjectSaveAsModule.class, KeyCode.S, KeyCombination.SHORTCUT_DOWN,
        KeyCombination.SHIFT_DOWN);
    addMenuItem(menu, "Close project", MZmineGUI::requestCloseProject, KeyCode.Q,
        KeyCombination.SHORTCUT_DOWN);
    addSeparator(menu);
    addModuleMenuItem(menu, BatchModeModule.class, KeyCode.B, KeyCombination.SHORTCUT_DOWN);
    addSeparator(menu);
    addMenuItem(menu, "Sample metadata",
        () -> MZmineCore.getDesktop().addTab(new ProjectMetadataTab()), KeyCode.M,
        KeyCombination.SHORTCUT_DOWN);
    addSeparator(menu);
    addMenuItem(menu, "Set preferences",
        () -> MZmineCore.getConfiguration().getPreferences().showSetupDialog(true), KeyCode.P,
        KeyCombination.SHORTCUT_DOWN);
    addMenuItem(menu, "Save configuration", WorkspaceMenuUtils::saveConfiguration, null);
    addMenuItem(menu, "Load configuration", WorkspaceMenuUtils::loadConfiguration, null);

    return menu;
  }

  default Menu buildDefaultLcMsSubMenu() {
    return addModuleMenuItems("LC-MS", ModularADAPChromatogramBuilderModule.class,
        TargetedFeatureDetectionModule.class);
  }

  default Menu buildDefaultGcMsSubMenu() {
    return addModuleMenuItems("GC-MS", ModularADAPChromatogramBuilderModule.class/*,
        SpectralDeconvolutionGCModule.class*/);
  }

  default Menu buildDefaultImsMsSubMenu() {
    return addModuleMenuItems("LC-IMS-MS", ModularADAPChromatogramBuilderModule.class,
        ImsExpanderModule.class, ModularADAPChromatogramBuilderModule.class);
  }

  default Menu buildDefaultImagingSubMenu() {
    return addModuleMenuItems("MS imaging/Spots", ImageBuilderModule.class,
        MaldiSpotFeatureDetectionModule.class);
  }

  default Menu buildDefaultMsnSubMenu() {
    return addModuleMenuItems("MSn", MsnTreeFeatureDetectionModule.class,
        MsnFeatureDetectionModule.class);
  }

  default Menu buildDefaultResolvingSubMenu() {
    return addModuleMenuItems("Resolving", MinimumSearchFeatureResolverModule.class,
        NoiseAmplitudeResolverModule.class, SavitzkyGolayResolverModule.class);
  }

  default Menu buildDefaultFeatureListExportSubMenu() {
    final Menu menu = new Menu("Export feature list");

    addModuleMenuItems(menu, "Graphics", ExportAllIdsGraphicalModule.class);
    addModuleMenuItems(menu, CSVExportModularModule.class, CompoundAnnotationsCSVExportModule.class,
        LegacyCSVExportModule.class, MZTabmExportModule.class, SQLExportModule.class,
        AdapMspExportModule.class, AdapMgfExportModule.class, GnpsFbmnExportAndSubmitModule.class,
        GnpsGcExportAndSubmitModule.class, SiriusExportModule.class,
        ImportFeatureNetworksSimpleModule.class, ExportCorrAnnotationModule.class,
        NetworkGraphMlExportModule.class, FeatureMLExportModularModule.class,
        CcsBaseExportModule.class);
    addModuleMenuItems(menu, "Statistics", VennExportModule.class, MetaboAnalystExportModule.class);
    addModuleMenuItems(menu, "Libraries", LibraryBatchGenerationModule.class,
        GNPSLibraryBatchExportModule.class);

    return menu;
  }

  default Menu buildDefaultFeatureGroupingSubMenu() {
    final Menu groupingMenu = addModuleMenuItems("Feature grouping", CorrelateGroupingModule.class,
        IonNetworkingModule.class, AddIonNetworkingModule.class, IonNetworkRefinementModule.class,
        IonNetRelationsModule.class, FormulaPredictionIonNetworkModule.class,
        CreateAvgNetworkFormulasModule.class, IonNetworkMSMSCheckModule.class,
        ClearIonIdentitiesModule.class);
    groupingMenu.getItems().add(new SeparatorMenuItem());
    addModuleMenuItems(groupingMenu, OnlineLcReactivityModule.class, AnnotateIsomersModule.class);
    groupingMenu.getItems().addAll(new SeparatorMenuItem(),
        new ModuleMenuItem(ImageCorrelateGroupingModule.class, null));
    return groupingMenu;
  }

  default Menu buildDefaultAnnotationSubMenu() {
    final Menu menu = new Menu("Annotation");

    addModuleMenuItems(menu, "Search precursor mass", LocalCSVDatabaseSearchModule.class,
        BioTransformerModule.class);
    addModuleMenuItems(menu, "Search spectra", SpectralLibrarySearchModule.class,
        LipidAnnotationModule.class, NistMsSearchModule.class,
        FormulaPredictionFeatureListModule.class, Ms2SearchModule.class);
    addModuleMenuItems(menu, "EC-MS workflow", CalcEcmsPotentialModule.class);
    menu.getItems().add(new SeparatorMenuItem());
    addModuleMenuItems(menu, ClearFeatureAnnotationsModule.class);
    return menu;
  }

  default Menu buildDefaultVisualizationMenu() {
    final Menu menu = new Menu("Visualization");

    final Menu rawDataVis = addModuleMenuItems(menu, "Raw data", RawDataOverviewModule.class,
        IMSRawDataOverviewModule.class, ImageVisualizerModule.class,
        MultidetectorVisualizerModule.class);
    addSeparator(rawDataVis);
    addModuleMenuItems(rawDataVis, ChromatogramVisualizerModule.class, TwoDVisualizerModule.class,
        Fx3DVisualizerModule.class, MsMsVisualizerModule.class,
        MassvoltammogramFromFileModule.class);
    addSeparator(rawDataVis);
    addModuleMenuItems(rawDataVis, RawDataSummaryModule.class, ScanHistogramModule.class,
        InjectTimeAnalysisModule.class);

    final Menu featureVis = addModuleMenuItems(menu, "Feature list",
        FeatureNetworkOverviewModule.class, CorrelatedFeaturesMzHistogramModule.class,
        FeatureCorrelationHistogramModule.class);
    addSeparator(featureVis);
    addModuleMenuItems(featureVis, ScatterPlotVisualizerModule.class,
        FeatureHistogramPlotModule.class, IntensityPlotModule.class);
    addSeparator(featureVis);
    addModuleMenuItems(featureVis, KendrickMassPlotModule.class, VanKrevelenDiagramModule.class,
        MassvoltammogramFromFeatureListModule.class);

    addModuleMenuItems(menu, MSnTreeVisualizerModule.class);

    return menu;
  }

  default Menu buildDefaultToolsMenu() {
    final Menu menu = new Menu("Tools");
    addMenuItem(menu, "Quick search", ModuleQuickSelectDialog::openQuickSearch, KeyCode.F,
        KeyCombination.SHORTCUT_DOWN);
    addModuleMenuItems(menu, IsotopePatternPreviewModule.class, QualityParametersModule.class);
    addModuleMenuItems(menu, "Libraries", LibraryAnalysisCSVExportModule.class,
        MsMsQualityExportModule.class);
    addModuleMenuItems(menu, "timsTOF fleX", TimsTOFMaldiAcquisitionModule.class,
        SimsefImagingSchedulerModule.class);
    return menu;
  }

  default Menu buildDefaultWizardMenu() {
    final Menu menu = new Menu("mzwizard");
    addModuleMenuItem(menu, BatchWizardModule.class, KeyCode.W, KeyCombination.SHORTCUT_DOWN);
    return menu;
  }

  default Menu buildDefaultWindowsMenu() {
    final WindowsMenu menu = new WindowsMenu();

    final Menu tasks = new Menu("Task manager");
    addMenuItem(tasks, "Hide",
        () -> MZmineGUI.handleTaskManagerLocationChange(WindowLocation.HIDDEN));
    addMenuItem(tasks, "Main window",
        () -> MZmineGUI.handleTaskManagerLocationChange(WindowLocation.MAIN));
    addMenuItem(tasks, "External window",
        () -> MZmineGUI.handleTaskManagerLocationChange(WindowLocation.EXTERNAL));
    addMenuItem(tasks, "Tab", () -> MZmineGUI.handleTaskManagerLocationChange(WindowLocation.TAB));

    menu.getItems().addAll(tasks, new SeparatorMenuItem());
    return menu;
  }

  default Menu buildDefaultHelpMenu() {
    final Menu menu = new Menu("Help");
    addMenuItem(menu, "Open documentation", () -> MZmineCore.getDesktop()
        .openWebPage("https://mzmine.github.io/mzmine_documentation/"));
    addMenuItem(menu, "Open landing page",
        () -> MZmineCore.getDesktop().addTab(new MZmineIntroductionTab()));

    addSeparator(menu);

    addMenuItem(menu, "Open landing page",
        () -> MZmineCore.getDesktop().addTab(new MZmineIntroductionTab()));
    addMenuItem(menu, "Support", () -> MZmineCore.getDesktop()
        .openWebPage("https://mzmine.github.io/mzmine_documentation/troubleshooting.html"));
    addMenuItem(menu, "Report an issue",
        () -> MZmineCore.getDesktop().openWebPage("http://mzmine.github.io/support.html"));
    addMenuItem(menu, "Show log file", WorkspaceMenuUtils::handleShowLogFile);

    addSeparator(menu);

    addMenuItem(menu, "Check for updates", WorkspaceMenuUtils::versionCheck);

    addSeparator(menu);

    addMenuItem(menu, "About mzmine", MZmineGUI::showAboutWindow);
    addMenuItem(menu, "Dependencies", () -> MZmineCore.getDesktop().addTab(new DependenciesTab()));
    return menu;
  }

  default Menu buildDefaultUsersMenu() {
    final Menu menu = new Menu("Users");
    addMenuItem(menu, "Manage users", () -> FxThread.runLater(UsersTab::showTab), KeyCode.U,
        KeyCombination.SHORTCUT_DOWN); // why fx thread?
    addMenuItem(menu, "Sign in / Sign up", () -> UsersTab.showTab(UsersViewState.LOGIN));
    addMenuItem(menu, "Remove user", () -> addMenuItem(menu, "Remove user",
        () -> UserAuthStore.removeUserFile(CurrentUserService.getUser())));
    addMenuItem(menu, "Open users directory", WorkspaceMenuUtils::openUsersDirectory);
    addMenuItem(menu, "Manage user online",
        () -> DesktopService.getDesktop().openWebPage(MzioMZmineLinks.USER_CONSOLE.getUrl()));
    return menu;
  }
}
