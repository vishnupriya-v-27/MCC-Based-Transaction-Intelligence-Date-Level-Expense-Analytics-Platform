package com.expensetracker.controller;

import com.expensetracker.dto.CategoryUpdateRequest;
import com.expensetracker.dto.TransactionDTO;
import com.expensetracker.entity.Transaction;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.service.CategorizationService;
import com.expensetracker.service.CsvImportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CsvImportService csvImportService;
    private final CategorizationService categorizationService;

    public TransactionController(TransactionRepository transactionRepository,
                                  CsvImportService csvImportService,
                                  CategorizationService categorizationService) {
        this.transactionRepository = transactionRepository;
        this.csvImportService = csvImportService;
        this.categorizationService = categorizationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }
        try {
            CsvImportService.ImportResult result = csvImportService.importCsv(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to read file: " + e.getMessage()));
        }
    }

    @GetMapping
    public List<TransactionDTO> getAll() {
        return transactionRepository.findAllByOrderByTransactionDateDesc()
                .stream().map(TransactionDTO::from).collect(Collectors.toList());
    }

    @GetMapping("/by-date")
    public List<TransactionDTO> getByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return transactionRepository.findByTransactionDate(date)
                .stream().map(TransactionDTO::from).collect(Collectors.toList());
    }

    @GetMapping("/by-range")
    public List<TransactionDTO> getByRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return transactionRepository.findByTransactionDateBetweenOrderByTransactionDateAsc(start, end)
                .stream().map(TransactionDTO::from).collect(Collectors.toList());
    }

    /** Re-runs auto-categorization for every transaction on a given date. */
    @PostMapping("/recategorize/by-date")
    public ResponseEntity<?> recategorizeByDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Transaction> transactions = transactionRepository.findByTransactionDate(date);
        for (Transaction tx : transactions) {
            CategorizationService.Result result = categorizationService.categorize(tx);
            tx.setAppCategory(result.appCategory);
            if (result.isoCategoryName != null) tx.setIsoCategoryName(result.isoCategoryName);
            tx.setCategorizationSource(result.source);
        }
        transactionRepository.saveAll(transactions);
        return ResponseEntity.ok(Map.of(
                "date", date.toString(),
                "recategorizedCount", transactions.size(),
                "transactions", transactions.stream().map(TransactionDTO::from).collect(Collectors.toList())
        ));
    }

    /** Re-runs auto-categorization for a single transaction. */
    @PostMapping("/{id}/recategorize")
    public ResponseEntity<?> recategorizeOne(@PathVariable Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        CategorizationService.Result result = categorizationService.categorize(tx);
        tx.setAppCategory(result.appCategory);
        if (result.isoCategoryName != null) tx.setIsoCategoryName(result.isoCategoryName);
        tx.setCategorizationSource(result.source);
        transactionRepository.save(tx);
        return ResponseEntity.ok(TransactionDTO.from(tx));
    }

    /** Manual correction: updates this transaction and (by default) caches the payee for future auto-categorization. */
    @PutMapping("/{id}/category")
    public ResponseEntity<?> confirmCategory(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest request) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        tx.setAppCategory(request.getAppCategory());
        tx.setCategorizationSource("MANUAL");
        tx.setManuallyConfirmed(true);
        transactionRepository.save(tx);

        if (request.isApplyToAllFromSamePayee()) {
            categorizationService.updateCache(tx.getPayeeNormalized(), request.getAppCategory(), tx.getMccCode(), true);
        }
        return ResponseEntity.ok(TransactionDTO.from(tx));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}
