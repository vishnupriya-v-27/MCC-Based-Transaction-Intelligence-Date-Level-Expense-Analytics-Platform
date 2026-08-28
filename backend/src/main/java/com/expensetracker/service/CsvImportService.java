package com.expensetracker.service;

import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses the PhonePe statement CSV (enriched with an extra MCC column) without
 * relying on any external CSV library, since column layouts from statement exports
 * are inconsistent and a hand-rolled quote-aware parser is easier to adapt.
 *
 * Expected (case-insensitive, order-independent) headers - extra columns are ignored:
 *   Date, Description/Payee/Merchant, Type (DEBIT/CREDIT), Amount, MCC (optional), Reference/UTR (optional)
 */
@Service
public class CsvImportService {

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd MMM yyyy")
    );

    private final TransactionRepository transactionRepository;
    private final CategorizationService categorizationService;

    public CsvImportService(TransactionRepository transactionRepository, CategorizationService categorizationService) {
        this.transactionRepository = transactionRepository;
        this.categorizationService = categorizationService;
    }

    public static class ImportResult {
        public int totalRows;
        public int imported;
        public int skipped;
        public List<String> errors = new ArrayList<>();
    }

    public ImportResult importCsv(MultipartFile file) throws IOException {
        ImportResult result = new ImportResult();
        List<Transaction> toSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.errors.add("File is empty.");
                return result;
            }
            List<String> headers = parseLine(headerLine);
            Map<String, Integer> columnIndex = buildColumnIndex(headers);

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;
                result.totalRows++;
                try {
                    Transaction tx = parseRow(parseLine(line), columnIndex);
                    if (tx != null) {
                        CategorizationService.Result catResult = categorizationService.categorize(tx);
                        tx.setAppCategory(catResult.appCategory);
                        if (catResult.isoCategoryName != null) {
                            tx.setIsoCategoryName(catResult.isoCategoryName);
                        }
                        tx.setCategorizationSource(catResult.source);

                        if (!catResult.source.equals("NEEDS_REVIEW")) {
                            categorizationService.updateCache(tx.getPayeeNormalized(), catResult.appCategory, tx.getMccCode(), false);
                        }
                        toSave.add(tx);
                        result.imported++;
                    } else {
                        result.skipped++;
                    }
                } catch (Exception e) {
                    result.skipped++;
                    result.errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }

        transactionRepository.saveAll(toSave);
        return result;
    }

    private Map<String, Integer> buildColumnIndex(List<String> headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            index.put(normalizeHeader(headers.get(i)), i);
        }
        return index;
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private Transaction parseRow(List<String> fields, Map<String, Integer> columnIndex) {
        String dateStr = getField(fields, columnIndex, "date", "transactiondate", "txndate");
        String payee = getField(fields, columnIndex, "description", "payee", "merchant", "narration", "details", "transactiondetails");
        String typeStr = getField(fields, columnIndex, "type", "transactiontype", "drcr");
        String amountStr = getField(fields, columnIndex, "amount", "amountinr", "amountrs", "debitcredit");
        String mcc = getField(fields, columnIndex, "mcc", "mcccode");
        String reference = getField(fields, columnIndex, "reference", "utr", "transactionid", "refno");

        if (dateStr == null || payee == null || amountStr == null) {
            throw new IllegalArgumentException("Missing required column (date/payee/amount)");
        }

        Transaction tx = new Transaction();
        tx.setTransactionDate(parseDate(dateStr));
        tx.setPayeeRaw(payee.trim());
        tx.setPayeeNormalized(normalizePayee(payee));
        tx.setAmount(parseAmount(amountStr));
        tx.setType(parseType(typeStr, amountStr));
        tx.setMccCode(mcc != null ? mcc.trim() : null);
        tx.setReferenceId(reference != null ? reference.trim() : null);
        tx.setP2p(mcc != null && mcc.trim().equalsIgnoreCase("N/A"));

        return tx;
    }

    private String getField(List<String> fields, Map<String, Integer> columnIndex, String... possibleNames) {
        for (String name : possibleNames) {
            Integer idx = columnIndex.get(name);
            if (idx != null && idx < fields.size()) {
                String value = fields.get(idx);
                return value == null || value.isBlank() ? null : value;
            }
        }
        return null;
    }

    private LocalDate parseDate(String raw) {
        String trimmed = raw.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        throw new IllegalArgumentException("Unrecognized date format: " + raw);
    }

    private BigDecimal parseAmount(String raw) {
        String cleaned = raw.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isBlank()) throw new IllegalArgumentException("Unparseable amount: " + raw);
        return new BigDecimal(cleaned).abs();
    }

    private TransactionType parseType(String typeStr, String amountStr) {
        if (typeStr != null) {
            String upper = typeStr.trim().toUpperCase(Locale.ROOT);
            if (upper.contains("CREDIT") || upper.equals("CR")) return TransactionType.CREDIT;
            if (upper.contains("DEBIT") || upper.equals("DR")) return TransactionType.DEBIT;
        }
        // fallback: negative amount string implies debit, positive implies credit
        return amountStr.trim().startsWith("-") ? TransactionType.DEBIT : TransactionType.DEBIT;
    }

    private String normalizePayee(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    /** Minimal quote-aware CSV line splitter (handles quoted fields containing commas). */
    private List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
