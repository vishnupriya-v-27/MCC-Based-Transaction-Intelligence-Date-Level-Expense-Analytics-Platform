import React, { useState } from 'react';
import UploadPage from './components/UploadPage';
import Dashboard from './components/Dashboard';
import DateDrilldown from './components/DateDrilldown';

const TABS = [
  { id: 'upload', label: 'Import' },
  { id: 'dashboard', label: 'Overview' },
  { id: 'drilldown', label: 'Day ledger' },
];

export default function App() {
  const [activeTab, setActiveTab] = useState('upload');
  const [refreshKey, setRefreshKey] = useState(0);

  const handleImported = () => {
    setRefreshKey((k) => k + 1);
    setActiveTab('dashboard');
  };

  return (
    <div className="app-shell">
      <div className="top-bar">
        <div className="brand">
          <span className="brand-mark">Ledger</span>
          <span className="brand-sub">MCC expense tracker</span>
        </div>
        <div className="nav-tabs">
          {TABS.map((tab) => (
            <button
              key={tab.id}
              className={`nav-tab ${activeTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {activeTab === 'upload' && <UploadPage onImported={handleImported} />}
      {activeTab === 'dashboard' && <Dashboard key={refreshKey} />}
      {activeTab === 'drilldown' && <DateDrilldown />}
    </div>
  );
}
