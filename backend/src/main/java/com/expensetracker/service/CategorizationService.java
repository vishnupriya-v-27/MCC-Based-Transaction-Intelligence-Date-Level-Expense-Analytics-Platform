package com.expensetracker.service;

import com.expensetracker.entity.*;
import com.expensetracker.repository.MccCategoryMappingRepository;
import com.expensetracker.repository.PayeeCategoryCacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Categorization priority order (checked top to bottom, first hit wins):
 *   1. CACHE       - payee has already been resolved before (manually or automatically)
 *   2. MCC         - a real MCC code is present on the transaction and maps to a known category
 *   3. P2P         - transaction is a person-to-person transfer with no merchant MCC (explicit "N/A" case)
 *   4. KEYWORD     - payee text contains a strong keyword signal (e.g. "UBER", "ELECTRICITY BOARD")
 *   5. HEURISTIC   - fallback guess: does this look like a business or a person?
 *   6. NEEDS_REVIEW- nothing matched; surfaced to the user for a manual decision
 *
 * Every automatic resolution (except NEEDS_REVIEW) is written back into the payee cache
 * so the next transaction from the same payee is a single lookup instead of a full re-run.
 */
@Service
public class CategorizationService {

    private final PayeeCategoryCacheRepository cacheRepository;
    private final MccCategoryMappingRepository mccMappingRepository;

    public CategorizationService(PayeeCategoryCacheRepository cacheRepository,
                                  MccCategoryMappingRepository mccMappingRepository) {
        this.cacheRepository = cacheRepository;
        this.mccMappingRepository = mccMappingRepository;
    }

    public static class Result {
        public final AppCategory appCategory;
        public final String isoCategoryName;
        public final String source;

        public Result(AppCategory appCategory, String isoCategoryName, String source) {
            this.appCategory = appCategory;
            this.isoCategoryName = isoCategoryName;
            this.source = source;
        }
    }

    private static final List<String> TRAVEL_KEYWORDS = List.of(
            "UBER", "OLA", "RAPIDO", "IRCTC", "REDBUS", "METRO", "PETROL", "FUEL", "INDIAN OIL", "HPCL", "BPCL", "AIRLINES", "AIRWAYS", "FASTAG"
    );
    private static final List<String> FOOD_KEYWORDS = List.of(
            "SWIGGY", "ZOMATO", "RESTAURANT", "CAFE", "FOOD", "DOMINOS", "PIZZA", "BAKERY", "HOTEL"
    );
    private static final List<String> GROCERY_KEYWORDS = List.of(
            "BIGBASKET", "BLINKIT", "ZEPTO", "GROFERS", "DMART", "SUPERMARKET", "KIRANA", "GROCERY"
    );
    private static final List<String> BILLS_KEYWORDS = List.of(
            "ELECTRICITY", "BROADBAND", "AIRTEL", "JIO", "VODAFONE", "VI ", "RECHARGE", "GAS BOARD", "WATER BOARD", "DTH", "BSNL"
    );
    private static final List<String> SHOPPING_KEYWORDS = List.of(
            "AMAZON", "FLIPKART", "MYNTRA", "AJIO", "MEESHO", "RELIANCE TRENDS", "MALL"
    );
    private static final List<String> ENTERTAINMENT_KEYWORDS = List.of(
            "NETFLIX", "HOTSTAR", "PRIME VIDEO", "SPOTIFY", "BOOKMYSHOW", "PVR", "INOX", "GAANA"
    );
    private static final List<String> HEALTH_KEYWORDS = List.of(
            "PHARMACY", "HOSPITAL", "CLINIC", "APOLLO", "MEDPLUS", "DIAGNOSTIC", "MEDICAL", "PRACTO"
    );
    private static final List<String> EDUCATION_KEYWORDS = List.of(
            "SCHOOL", "COLLEGE", "UNIVERSITY", "TUITION", "COURSE", "UDEMY", "BYJU"
    );
    private static final List<String> INVESTMENT_KEYWORDS = List.of(
            "ZERODHA", "GROWW", "MUTUAL FUND", "SIP", "STOCK", "UPSTOX", "NPS"
    );

    public Result categorize(Transaction tx) {

        // Income is determined purely by PhonePe's own CREDIT/DEBIT flag, not MCC.
        if (tx.getType() == TransactionType.CREDIT) {
            return new Result(AppCategory.INCOME, tx.getIsoCategoryName(), "PHONEPE_TYPE");
        }

        String payeeNormalized = tx.getPayeeNormalized();

        // 1. CACHE
        if (payeeNormalized != null) {
            Optional<PayeeCategoryCache> cached = cacheRepository.findByPayeeNormalized(payeeNormalized);
            if (cached.isPresent()) {
                PayeeCategoryCache c = cached.get();
                return new Result(c.getAppCategory(), tx.getIsoCategoryName(), "CACHE");
            }
        }

        // 2. MCC
        if (tx.getMccCode() != null && !tx.getMccCode().isBlank() && !tx.getMccCode().equalsIgnoreCase("N/A")) {
            Optional<MccCategoryMapping> mapping = mccMappingRepository.findByMccCode(tx.getMccCode().trim());
            if (mapping.isPresent()) {
                MccCategoryMapping m = mapping.get();
                return new Result(m.getAppCategory(), m.getIsoCategoryName(), "MCC");
            }
        }

        // 3. Explicit P2P (no MCC, marked N/A - typical of UPI person-to-person transfers)
        if (tx.isP2p() || (tx.getMccCode() != null && tx.getMccCode().equalsIgnoreCase("N/A"))) {
            return new Result(AppCategory.TRANSFERS_P2P, null, "P2P");
        }

        // 4. Keyword fallback
        String upperPayee = payeeNormalized != null ? payeeNormalized.toUpperCase(Locale.ROOT) : "";
        if (containsAny(upperPayee, FOOD_KEYWORDS)) return new Result(AppCategory.FOOD_AND_DINING, null, "KEYWORD");
        if (containsAny(upperPayee, GROCERY_KEYWORDS)) return new Result(AppCategory.GROCERIES, null, "KEYWORD");
        if (containsAny(upperPayee, TRAVEL_KEYWORDS)) return new Result(AppCategory.TRAVEL_AND_TRANSPORT, null, "KEYWORD");
        if (containsAny(upperPayee, SHOPPING_KEYWORDS)) return new Result(AppCategory.SHOPPING, null, "KEYWORD");
        if (containsAny(upperPayee, BILLS_KEYWORDS)) return new Result(AppCategory.BILLS_AND_UTILITIES, null, "KEYWORD");
        if (containsAny(upperPayee, ENTERTAINMENT_KEYWORDS)) return new Result(AppCategory.ENTERTAINMENT, null, "KEYWORD");
        if (containsAny(upperPayee, HEALTH_KEYWORDS)) return new Result(AppCategory.HEALTHCARE, null, "KEYWORD");
        if (containsAny(upperPayee, EDUCATION_KEYWORDS)) return new Result(AppCategory.EDUCATION, null, "KEYWORD");
        if (containsAny(upperPayee, INVESTMENT_KEYWORDS)) return new Result(AppCategory.INVESTMENTS, null, "KEYWORD");

        // 5. Business vs person heuristic - very rough signal: all-caps multi-word business-like
        // strings with no personal-name pattern get a generic SHOPPING guess; anything else is
        // treated as a probable P2P transfer rather than guessed wrong with false confidence.
        if (looksLikeBusiness(upperPayee)) {
            return new Result(AppCategory.SHOPPING, null, "HEURISTIC");
        }

        // 6. Needs review - honest fallback, not forced into a category we can't justify
        return new Result(AppCategory.NEEDS_REVIEW, null, "NEEDS_REVIEW");
    }

    /** Writes an automatic or manual resolution into the cache so future lookups are O(1). */
    public void updateCache(String payeeNormalized, AppCategory category, String mccCode, boolean manual) {
        if (payeeNormalized == null || payeeNormalized.isBlank()) return;
        PayeeCategoryCache entry = cacheRepository.findByPayeeNormalized(payeeNormalized)
                .orElse(new PayeeCategoryCache());
        entry.setPayeeNormalized(payeeNormalized);
        entry.setAppCategory(category);
        entry.setMccCode(mccCode);
        entry.setFromManualCorrection(manual || entry.isFromManualCorrection());
        cacheRepository.save(entry);
    }

    private boolean containsAny(String text, List<String> keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private boolean looksLikeBusiness(String upperPayee) {
        if (upperPayee.isBlank()) return false;
        String[] businessMarkers = {"PVT", "LTD", "LLP", "STORE", "MART", "SHOP", "ENTERPRISES", "SERVICES", "TRADERS"};
        for (String marker : businessMarkers) {
            if (upperPayee.contains(marker)) return true;
        }
        return false;
    }
}
