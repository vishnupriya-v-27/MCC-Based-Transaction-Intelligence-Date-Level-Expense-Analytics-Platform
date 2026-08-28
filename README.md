# Ledger — MCC-based expense tracker

Java 21 + Spring Boot 3.3 + MySQL backend, React frontend. Imports a statement CSV (enriched with an MCC column), auto-categorizes each transaction into a fixed set of buckets (Food & Dining, Travel & Transport, Income, etc.), and gives you an overall report plus a day-by-day drill-down.

## How categorization works (in order, first match wins)

1. Income check — if PhonePe marked the row CREDIT, it's Income. This never depends on MCC.
2. Cache — has this exact payee been resolved before (automatically or manually)? Reuse it.
3. MCC — a real MCC code on the row, matched against the seeded ISO 18245 mapping table.
4. P2P — no MCC / explicit N/A, treated as a person-to-person transfer.
5. Keyword fallback — known merchant name fragments (SWIGGY, UBER, AMAZON, etc.).
6. Business/person heuristic — rough guess based on business-name markers (PVT, LTD, STORE...).
7. Needs review — nothing matched; surfaced honestly instead of guessed.

Every automatic or manual resolution is written into payee_category_cache, so the next transaction from the same payee is a single lookup instead of a full re-run.

## Backend setup (Spring Tool Suite / Eclipse)

1. Create the MySQL database (or let it auto-create): the app is configured to auto-create expense_tracker on first connection via createDatabaseIfNotExist=true. Just make sure MySQL is running locally and the credentials below match your setup.

2. Open backend/src/main/resources/application.properties and check/update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

3. In Spring Tool Suite: File → Update → Clean -> Maven → . Let it download dependencies.

4. Run ExpenseTrackerApplication.java as a Spring Boot App . Tables are created automatically (spring.jpa.hibernate.ddl-auto=update), and the MCC lookup table is seeded on first startup.

5. Backend runs on http://localhost:8080.

## Frontend setup

```bash
cd frontend
npm install
npm start
```

Runs on http://localhost:3000 and talks to the backend at http://localhost:8080/api.

## CSV format expected

Headers are matched case-insensitively and can be in any order. Extra columns are ignored.

| Purpose              | Accepted header names (any one)                                       |
| -------------------- | --------------------------------------------------------------------- |
| Date                 | Date, Transaction Date, Txn Date                                      |
| Payee                | Description, Payee, Merchant, Narration, Details, Transaction Details |
| Type                 | Type, Transaction Type, Dr/Cr                                         |
| Amount               | Amount, Amount (INR), Amount (Rs)                                     |
| MCC (optional)       | MCC, MCC Code — use N/A for P2P transfers with no merchant            |
| Reference (optional) | Reference, UTR, Transaction ID, Ref No                                |

Example row:

```csv
Date,Transaction Details,Type,Amount,MCC
12/08/2026,SWIGGY BANGALORE,DEBIT,458,5812
```

## Project structure

```text
backend/
  src/main/java/com/expensetracker/
    entity/        Transaction, PayeeCategoryCache, MccCategoryMapping, enums
    repository/     Spring Data JPA repositories
    service/        CsvImportService, CategorizationService, ReportService, DataSeedService
    controller/     TransactionController, ReportController
    config/         CORS config
  src/main/resources/application.properties
frontend/
  src/
    components/     UploadPage, Dashboard, DateDrilldown, TransactionTable
    api.js          fetch wrapper for the backend
    App.js          tab navigation shell
    styles/app.css  ledger/passbook visual theme
```
