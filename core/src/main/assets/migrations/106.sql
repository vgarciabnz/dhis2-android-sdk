# New tables for visualization objects
CREATE TABLE Visualization (_id INTEGER PRIMARY KEY AUTOINCREMENT, uid TEXT NOT NULL UNIQUE, code TEXT, name TEXT, displayName TEXT, created TEXT, lastUpdated TEXT, description TEXT, displayDescription TEXT, displayFormName TEXT, type TEXT, hideTitle INTEGER, hideSubtitle INTEGER, hideEmptyColumns INTEGER, hideEmptyRows INTEGER, hideEmptyRowItems TEXT, hideLegend INTEGER, showHierarchy INTEGER, rowTotals INTEGER, rowSubTotals INTEGER, colTotals INTEGER, colSubTotals INTEGER, showDimensionLabels INTEGER, percentStackedValues INTEGER, noSpaceBetweenColumns INTEGER, skipRounding INTEGER, displayDensity TEXT, digitGroupSeparator TEXT, relativePeriods TEXT, filterDimensions TEXT, rowDimensions TEXT, columnDimensions TEXT, organisationUnitLevels TEXT, userOrganisationUnit INTEGER, userOrganisationUnitChildren INTEGER, userOrganisationUnitGrandChildren INTEGER, organisationUnits TEXT, periods TEXT);
CREATE TABLE VisualizationCategoryDimensionLink (_id INTEGER PRIMARY KEY AUTOINCREMENT, visualization TEXT NOT NULL, category TEXT NOT NULL,  categoryOption TEXT NOT NULL, FOREIGN KEY (category) REFERENCES Category (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (visualization) REFERENCES Visualization (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, FOREIGN KEY (categoryOption) REFERENCES CategoryOption (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED, UNIQUE (visualization, category, categoryOption));
CREATE TABLE DataDimensionItem (_id INTEGER PRIMARY KEY AUTOINCREMENT, visualization TEXT NOT NULL, dataDimensionItemType TEXT,  indicator TEXT,  dataElement TEXT, dataElementOperand TEXT,  reportingRate TEXT,  programIndicator TEXT,  programDataElement TEXT,  programAttribute TEXT,  validationRule TEXT, FOREIGN KEY (visualization) REFERENCES Visualization (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED);