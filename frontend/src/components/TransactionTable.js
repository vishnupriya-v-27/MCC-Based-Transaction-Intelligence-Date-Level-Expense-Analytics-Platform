import React, { useState } from 'react';
import { api, CATEGORY_OPTIONS, CATEGORY_LABELS } from '../api';

function formatCurrency(value) {
  const n = Number(value || 0);
  return n.toLocaleString('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 });
}

export default function TransactionTable({ transactions, onChanged }) {
  const [savingId, setSavingId] = useState(null);

  const handleCategoryChange = async (txn, newCategory) => {
    setSavingId(txn.id);
    try {
      const updated = await api.updateCategory(txn.id, newCategory, true);
      onChanged && onChanged(updated);
    } finally {
      setSavingId(null);
    }
  };

  if (!transactions || transactions.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-title">No transactions</div>
        <p>Nothing recorded for this selection yet.</p>
      </div>
    );
  }

  return (
    <table className="txn-table">
      <thead>
        <tr>
          <th>Payee</th>
          <th>MCC</th>
          <th>Category</th>
          <th>Source</th>
          <th style={{ textAlign: 'right' }}>Amount</th>
        </tr>
      </thead>
      <tbody>
        {transactions.map((t) => (
          <tr key={t.id}>
            <td className="txn-payee">{t.payee}</td>
            <td className="txn-mcc">{t.mccCode || '—'}</td>
            <td>
              <select
                className="category-select"
                value={t.appCategory}
                disabled={savingId === t.id}
                onChange={(e) => handleCategoryChange(t, e.target.value)}
              >
                {CATEGORY_OPTIONS.map((opt) => (
                  <option key={opt} value={opt}>{CATEGORY_LABELS[opt]}</option>
                ))}
              </select>
            </td>
            <td>
              {t.appCategory === 'NEEDS_REVIEW' ? (
                <span className="badge needs-review">Needs review</span>
              ) : t.manuallyConfirmed ? (
                <span className="badge manual">Manual</span>
              ) : (
                <span className="badge">{t.categorizationSource}</span>
              )}
            </td>
            <td className={`txn-amount ${t.type === 'CREDIT' ? 'credit' : 'debit'}`}>
              {t.type === 'CREDIT' ? '+' : '−'}{formatCurrency(t.amount)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
