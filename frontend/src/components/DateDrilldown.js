import React, { useState } from 'react';
import { api } from '../api';
import TransactionTable from './TransactionTable';

function todayIso() {
  const d = new Date();
  return d.toISOString().slice(0, 10);
}

export default function DateDrilldown() {
  const [date, setDate] = useState(todayIso());
  const [transactions, setTransactions] = useState(null);
  const [loading, setLoading] = useState(false);
  const [recategorizing, setRecategorizing] = useState(false);
  const [error, setError] = useState(null);
  const [searched, setSearched] = useState(false);

  const loadDate = async (d) => {
    setLoading(true);
    setError(null);
    setSearched(true);
    try {
      const res = await api.getTransactionsByDate(d);
      setTransactions(res);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleRecategorize = async () => {
    setRecategorizing(true);
    setError(null);
    try {
      const res = await api.recategorizeByDate(date);
      setTransactions(res.transactions);
    } catch (e) {
      setError(e.message);
    } finally {
      setRecategorizing(false);
    }
  };

  const handleTxnUpdated = (updated) => {
    setTransactions((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
  };

  const dayTotal = transactions
    ? transactions.reduce((sum, t) => sum + (t.type === 'DEBIT' ? Number(t.amount) : 0), 0)
    : 0;

  return (
    <div className="card">
      <h2 className="card-title">Day ledger</h2>
      <p className="card-subtitle">Pick a date to see everything recorded that day, and re-run categorization on demand.</p>

      <div className="date-picker-row">
        <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <button className="btn btn-primary" onClick={() => loadDate(date)} disabled={loading}>
          {loading ? 'Loading…' : 'View day'}
        </button>
        {transactions && transactions.length > 0 && (
          <button className="btn" onClick={handleRecategorize} disabled={recategorizing}>
            {recategorizing ? 'Re-categorizing…' : 'Run auto-categorization'}
          </button>
        )}
        {transactions && transactions.length > 0 && (
          <span className="loading-text">
            {transactions.length} transaction{transactions.length !== 1 ? 's' : ''} · spent{' '}
            {dayTotal.toLocaleString('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 })}
          </span>
        )}
      </div>

      {error && <div className="error-text">{error}</div>}

      {searched && !loading && transactions && (
        <TransactionTable transactions={transactions} onChanged={handleTxnUpdated} />
      )}

      {!searched && (
        <div className="empty-state">
          <div className="empty-state-title">Choose a date above</div>
          <p>You'll see every transaction from that day and can trigger re-categorization on it.</p>
        </div>
      )}
    </div>
  );
}
