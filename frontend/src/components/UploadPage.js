import React, { useState, useRef } from 'react';
import { api } from '../api';

export default function UploadPage({ onImported }) {
  const [dragging, setDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const inputRef = useRef(null);

  const handleFile = async (file) => {
    if (!file) return;
    if (!file.name.toLowerCase().endsWith('.csv')) {
      setError('Please choose a .csv file exported from your statement.');
      return;
    }
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await api.uploadCsv(file);
      setResult(res);
    } catch (e) {
      setError(e.message || 'Import failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2 className="card-title">Import a statement</h2>
      <p className="card-subtitle">
        Upload your PhonePe (or other) statement CSV, enriched with an MCC column. Columns are matched by
        name, so date / description / type / amount / mcc can be in any order.
      </p>

      <div
        className={`upload-zone ${dragging ? 'dragging' : ''}`}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragging(false);
          handleFile(e.dataTransfer.files[0]);
        }}
      >
        <div className="upload-icon">＋</div>
        <div className="upload-text">
          {loading ? 'Importing…' : 'Drop your CSV here, or click to browse'}
        </div>
        <div className="upload-hint">.csv up to 10MB</div>
        <input
          ref={inputRef}
          type="file"
          accept=".csv"
          onChange={(e) => handleFile(e.target.files[0])}
        />
      </div>

      {error && <div className="error-text">{error}</div>}

      {result && (
        <div className="import-summary">
          Read <strong>{result.totalRows}</strong> rows &middot; imported{' '}
          <strong>{result.imported}</strong> &middot; skipped <strong>{result.skipped}</strong>
          {result.errors && result.errors.length > 0 && (
            <div style={{ marginTop: 8, color: 'var(--paper-faint)' }}>
              {result.errors.slice(0, 5).map((e, i) => (
                <div key={i}>{e}</div>
              ))}
              {result.errors.length > 5 && <div>…and {result.errors.length - 5} more</div>}
            </div>
          )}
          <div style={{ marginTop: 14 }}>
            <button className="btn btn-primary" onClick={onImported}>
              View overview →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
