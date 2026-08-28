# MCC-Based Transaction Intelligence & Date-Level Expense Analytics Platform

A personal finance analytics platform that automatically categorizes transaction data using **Merchant Category Codes (MCC)** and provides **date-level transaction drill-down** for more precise spending analysis.

Built from a real-world usability problem: most payment applications provide spending summaries primarily at monthly or yearly levels, making it difficult to quickly inspect and summarize transactions for a **specific day**. The system addresses this by combining structured MCC-based categorization with detailed daily transaction analytics.

## Problem Statement

When reviewing personal transactions, two problems became apparent:

* Transaction descriptions are inconsistent and unreliable for automatically identifying merchant categories.
* Monthly or yearly spending summaries make it difficult to answer simple questions such as **"How much did I spend on the day I went out?"**

An initial approach using fuzzy matching on transaction descriptions proved difficult to scale because merchant names and descriptions can vary significantly. Maintaining large collections of merchant-specific rules also becomes increasingly difficult as transaction volume grows.

This project uses **MCC as a structured categorization signal** while retaining additional fallback mechanisms for transactions where MCC information is unavailable.

## Key Features

* **MCC-based automatic transaction categorization**
* CSV statement import with flexible column/header matching
* Multi-level categorization strategy with fallback handling
* Payee-level category caching for previously resolved merchants
* Manual category correction
* **Date-level transaction drill-down**
* Overall expense and income reporting
* Category-wise expense summaries
* Support for P2P transactions where MCC is unavailable
* Transactions requiring uncertain classification are explicitly marked for review rather than being incorrectly categorized

## Categorization Strategy

Transactions are categorized using the following priority order:

```text
Transaction
     │
     ▼
Income Check
     │
     ▼
Payee Category Cache
     │
     ▼
MCC Mapping
     │
     ▼
P2P Detection
     │
     ▼
Merchant Keyword Fallback
     │
     ▼
Business / Person Heuristic
     │
     ▼
Needs Review
```

### 1. Income Detection

If the transaction is marked as `CREDIT`, it is classified as **Income** without relying on MCC.

### 2. Payee Category Cache

Previously resolved payees are checked first.

Both automatic and manual resolutions are stored in `payee_category_cache`, allowing future transactions from the same payee to be categorized through a direct lookup.

### 3. MCC Classification

Transactions containing a valid MCC are matched against the application's seeded MCC-to-category mapping based on **ISO 18245 merchant category classifications**.

### 4. P2P Detection

Transactions without a usable MCC or explicitly marked as `N/A` can be treated as person-to-person transfers rather than forcing them into a merchant category.

### 5. Keyword Fallback

Known merchant fragments can be used when MCC information is unavailable.

Examples include:

```text
SWIGGY
UBER
AMAZON
```

### 6. Business / Person Heuristic

Business-name indicators such as `PVT`, `LTD`, `STORE`, etc. provide an additional heuristic when stronger signals are unavailable.

### 7. Needs Review

If no reliable rule matches, the transaction is surfaced for review instead of making an unreliable guess.

## Date-Level Expense Analytics

A key feature of the application is **date-level drill-down**.

Instead of only presenting:

```text
August 2026
Total Spending: ₹25,000
```

the application allows users to move from an overall report into individual dates:

```text
August 20
    ├── Restaurant       ₹850
    ├── Transport        ₹240
    └── Shopping         ₹1,200

August 21
    ├── Food             ₹450
    └── Transport        ₹180
```

This makes it easier to investigate and summarize spending associated with a particular day or event without manually searching through an entire transaction history.

## Technology Stack

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven
* MySQL

### Frontend

* React
* JavaScript
* CSS

### Data Processing

* CSV statement parsing
* MCC-based categorization
* Merchant/payee matching
* Category caching

## System Architecture

```text
                CSV Transaction Statement
                         │
                         ▼
                 CSV Import Service
                         │
                         ▼
              Transaction Processing
                         │
                         ▼
             Categorization Service
              │       │       │
              │       │       └── Fallback Rules
              │       └────────── MCC Mapping
              └────────────────── Payee Cache
                         │
                         ▼
                      MySQL
                         │
                         ▼
                   REST APIs
                         │
                         ▼
                  React Frontend
                    │         │
                    ▼         ▼
               Dashboard   Date Drilldown
```

## CSV Import

The importer supports flexible headers and does not require a fixed column order.

Headers are matched case-insensitively.

| Purpose   | Accepted Headers                                                      |
| --------- | --------------------------------------------------------------------- |
| Date      | Date, Transaction Date, Txn Date                                      |
| Payee     | Description, Payee, Merchant, Narration, Details, Transaction Details |
| Type      | Type, Transaction Type, Dr/Cr                                         |
| Amount    | Amount, Amount (INR), Amount (Rs)                                     |
| MCC       | MCC, MCC Code                                                         |
| Reference | Reference, UTR, Transaction ID, Ref No                                |

MCC is optional. `N/A` can be used for transactions where MCC is unavailable, such as P2P transfers.

Example:

```csv
Date,Transaction Details,Type,Amount,MCC
12/08/2026,SWIGGY BANGALORE,DEBIT,458,5812
```

## Database Design

The application uses MySQL with the following primary entities:

```text
Transaction
     │
     ├── Category
     │
     ├── PayeeCategoryCache
     │
     └── MccCategoryMapping
```

### Main Components

* `Transaction` — stores imported transaction records
* `MccCategoryMapping` — stores MCC-to-category mappings
* `PayeeCategoryCache` — stores previously resolved payees
* `AppCategory` — represents supported expense/income categories

## Project Structure

```text
expense-tracker/
│
├── backend/
│   ├── src/main/java/com/expensetracker/
│   │   │
│   │   ├── entity/
│   │   │   ├── Transaction.java
│   │   │   ├── PayeeCategoryCache.java
│   │   │   ├── MccCategoryMapping.java
│   │   │   ├── AppCategory.java
│   │   │   └── TransactionType.java
│   │   │
│   │   ├── repository/
│   │   │   ├── TransactionRepository.java
│   │   │   ├── PayeeCategoryCacheRepository.java
│   │   │   └── MccCategoryMappingRepository.java
│   │   │
│   │   ├── service/
│   │   │   ├── CsvImportService.java
│   │   │   ├── CategorizationService.java
│   │   │   ├── ReportService.java
│   │   │   └── DataSeedService.java
│   │   │
│   │   ├── controller/
│   │   │   ├── TransactionController.java
│   │   │   └── ReportController.java
│   │   │
│   │   ├── dto/
│   │   │   ├── CategorySummaryDTO.java
│   │   │   ├── CategoryUpdateRequest.java
│   │   │   ├── ReportSummaryDTO.java
│   │   │   └── TransactionDTO.java
│   │   │
│   │   ├── config/
│   │   │   └── CorsConfig.java
│   │   │
│   │   └── ExpenseTrackerApplication.java
│   │
│   ├── src/main/resources/
│   │   └── application.properties
│   │
│   └── pom.xml
│
├── frontend/
│   ├── public/
│   │   └── index.html
│   │
│   ├── src/
│   │   ├── components/
│   │   │   ├── UploadPage.js
│   │   │   ├── Dashboard.js
│   │   │   ├── DateDrilldown.js
│   │   │   └── TransactionTable.js
│   │   │
│   │   ├── api.js
│   │   │   └── API communication with backend
│   │   │
│   │   ├── App.js
│   │   │   └── Tab navigation shell
│   │   │
│   │   ├── index.js
│   │   │
│   │   └── styles/
│   │       └── app.css
│   │           └── Ledger / passbook visual theme
│   │
│   ├── package.json
│   └── package-lock.json
│
├── .gitignore
└── README.md
```
## Running the Application

### Prerequisites

* Java 21
* Maven
* MySQL
* Node.js and npm

### Backend

1. Create a MySQL database or allow the application to create it automatically.
2. Configure the database credentials using your local environment/configuration.
3. Navigate to the backend:

```bash
cd backend
```

4. Run:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Frontend

Navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the application:

```bash
npm start
```

The frontend runs on:

```text
http://localhost:3000
```

and communicates with the backend through:

```text
http://localhost:8080/api
```

## Engineering Decisions

### Why MCC?

Transaction descriptions alone are not always reliable enough for categorization. Merchant names can be inconsistent, abbreviated, or represented differently across transactions.

The initial fuzzy-matching approach also required maintaining increasingly large merchant-specific rules.

MCC provides a structured merchant classification signal that can be used before applying less reliable text-based heuristics.

### Why a Payee Cache?

Once a user or the categorization system has resolved a payee, repeating the complete categorization process for every future transaction is unnecessary.

The cache allows previously resolved payees to be categorized efficiently while also learning from manual corrections.

### Why Date-Level Drill-Down?

A monthly total tells a user **how much** they spent but not necessarily **when** the spending occurred.

Date-level drill-down provides a more useful way to investigate individual days, trips, outings, or events without searching through the complete transaction history.

## Future Improvements

* Support for additional bank/UPI statement formats
* Authentication and multi-user support
* Advanced spending trends and visualizations


## Project Motivation

This project originated from a real-world personal finance problem rather than a generic CRUD application.

The goal was to explore how structured transaction metadata, rule-based categorization, caching, REST APIs, and date-level analytics could be combined to create a more useful personal finance experience.
