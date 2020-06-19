# Creates the table DataSetAggregatedDataSync
CREATE TABLE DataSetAggregatedDataSync (_id INTEGER PRIMARY KEY AUTOINCREMENT, dataSet TEXT NOT NULL UNIQUE, lastPeriods INTEGER NOT NULL, futurePeriods INTEGER NOT NULL, dataElementsHash INTEGER NOT NULL, organisationUnitHash INTEGER NOT NULL, lastUpdated TEXT NOT NULL, FOREIGN KEY (dataSet) REFERENCES DataSet (uid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED);