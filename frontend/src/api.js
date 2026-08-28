const BASE_URL = 'http://localhost:8080/api';

async function handle(response) {
  if (!response.ok) {
    let message = `Request failed (${response.status})`;
    try {
      const body = await response.json();
      if (body.error) message = body.error;
    } catch (_) {}
    throw new Error(message);
  }
  return response.json();
}

export const api = {
  uploadCsv(file) {
    const formData = new FormData();
    formData.append('file', file);
    return fetch(`${BASE_URL}/transactions/upload`, {
      method: 'POST',
      body: formData,
    }).then(handle);
  },

  getReportSummary() {
    return fetch(`${BASE_URL}/report/summary`).then(handle);
  },

  getAllTransactions() {
    return fetch(`${BASE_URL}/transactions`).then(handle);
  },

  getTransactionsByDate(dateStr) {
    return fetch(`${BASE_URL}/transactions/by-date?date=${dateStr}`).then(handle);
  },

  recategorizeByDate(dateStr) {
    return fetch(`${BASE_URL}/transactions/recategorize/by-date?date=${dateStr}`, {
      method: 'POST',
    }).then(handle);
  },

  recategorizeOne(id) {
    return fetch(`${BASE_URL}/transactions/${id}/recategorize`, {
      method: 'POST',
    }).then(handle);
  },

  updateCategory(id, appCategory, applyToAllFromSamePayee = true) {
    return fetch(`${BASE_URL}/transactions/${id}/category`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ appCategory, applyToAllFromSamePayee }),
    }).then(handle);
  },
};

export const CATEGORY_OPTIONS = [
  'FOOD_AND_DINING',
  'GROCERIES',
  'TRAVEL_AND_TRANSPORT',
  'SHOPPING',
  'BILLS_AND_UTILITIES',
  'ENTERTAINMENT',
  'HEALTHCARE',
  'EDUCATION',
  'INVESTMENTS',
  'TRANSFERS_P2P',
  'INCOME',
  'NEEDS_REVIEW',
];

export const CATEGORY_LABELS = {
  FOOD_AND_DINING: 'Food & Dining',
  GROCERIES: 'Groceries',
  TRAVEL_AND_TRANSPORT: 'Travel & Transport',
  SHOPPING: 'Shopping',
  BILLS_AND_UTILITIES: 'Bills & Utilities',
  ENTERTAINMENT: 'Entertainment',
  HEALTHCARE: 'Healthcare',
  EDUCATION: 'Education',
  INVESTMENTS: 'Investments',
  TRANSFERS_P2P: 'Transfers (P2P)',
  INCOME: 'Income',
  NEEDS_REVIEW: 'Needs Review',
};
