import React, { useEffect, useState } from 'react';
import { api } from '../api';

function formatCurrency(value) {
  const n = Number(value || 0);
  return n.toLocaleString('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 });
}

export default function Dashboard() {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.getReportSummary()
      .then(setReport)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="card"><p className="loading-text">Loading report…</p></div>;
  if (error) return <div className="card"><p className="error-text">{error}</p></div>;
  if (!report || report.totalTransactions === 0) {
    return (
      <div className="card">
        <div className="empty-state">
          <div className="empty-state-title">No transactions yet</div>
          <p>Import a statement from the Import tab to see your report here.</p>
        </div>
      </div>
    );
  }

  const maxCategoryAmount = Math.max(...report.categoryBreakdown.map((c) => Number(c.totalAmount)));

  return (
    <>
      <div className="passbook">
        <div className="passbook-row">
          <div className="passbook-label">Income</div>
          <div className="passbook-desc">Total credited</div>
          <div className="passbook-amount credit">{formatCurrency(report.totalIncome)}</div>
        </div>
        <div className="passbook-row">
          <div className="passbook-label">Expenses</div>
          <div className="passbook-desc">Total debited</div>
          <div className="passbook-amount debit">{formatCurrency(report.totalExpenses)}</div>
        </div>
        <div className="passbook-row">
          <div className="passbook-label">Balance</div>
          <div className="passbook-desc">Net for this dataset</div>
          <div className={`passbook-amount ${Number(report.netBalance) >= 0 ? 'credit' : 'debit'}`}>
            {formatCurrency(report.netBalance)}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="metric-grid">
          <div className="metric">
            <div className="metric-label">Transactions</div>
            <div className="metric-value">{report.totalTransactions}</div>
          </div>
          <div className="metric">
            <div className="metric-label">Needs review</div>
            <div className="metric-value gold">{report.needsReviewCount}</div>
          </div>
          <div className="metric">
            <div className="metric-label">Categories used</div>
            <div className="metric-value">{report.categoryBreakdown.length}</div>
          </div>
        </div>

        <h3 className="card-title">Spend by category</h3>
        <p className="card-subtitle">Auto-categorized from MCC, cached payees, and keyword fallbacks.</p>

        {report.categoryBreakdown.map((c) => (
          <div className="ledger-bar-row" key={c.category}>
            <div className="ledger-bar-name">{c.category}</div>
            <div className="ledger-bar-track">
              <div
                className="ledger-bar-fill"
                style={{ width: `${maxCategoryAmount > 0 ? (Number(c.totalAmount) / maxCategoryAmount) * 100 : 0}%` }}
              />
            </div>
            <div className="ledger-bar-amount">{formatCurrency(c.totalAmount)}</div>
            <div className="ledger-bar-pct">{c.percentageOfTotal}%</div>
          </div>
        ))}
      </div>
    </>
  );
}
