package com.expensetracker.service;

import com.expensetracker.entity.AppCategory;
import com.expensetracker.entity.MccCategoryMapping;
import com.expensetracker.repository.MccCategoryMappingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the standard ISO 18245 Merchant Category Code -> app category mapping
 * on first startup. Safe to re-run; skips seeding if data already exists.
 */
@Component
public class DataSeedService implements CommandLineRunner {

    private final MccCategoryMappingRepository repository;

    public DataSeedService(MccCategoryMappingRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        List<MccCategoryMapping> seed = List.of(
                // Food & Dining
                new MccCategoryMapping("5812", "Eating Places, Restaurants", AppCategory.FOOD_AND_DINING),
                new MccCategoryMapping("5813", "Drinking Places (Bars, Taverns)", AppCategory.FOOD_AND_DINING),
                new MccCategoryMapping("5814", "Fast Food Restaurants", AppCategory.FOOD_AND_DINING),
                new MccCategoryMapping("5811", "Caterers", AppCategory.FOOD_AND_DINING),

                // Groceries
                new MccCategoryMapping("5411", "Grocery Stores, Supermarkets", AppCategory.GROCERIES),
                new MccCategoryMapping("5422", "Meat/Poultry/Seafood Markets", AppCategory.GROCERIES),
                new MccCategoryMapping("5451", "Dairy Product Stores", AppCategory.GROCERIES),
                new MccCategoryMapping("5499", "Misc. Food Stores", AppCategory.GROCERIES),

                // Travel & Transport
                new MccCategoryMapping("4111", "Local/Suburban Commuter Transport", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("4121", "Taxicabs/Limousines", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("4131", "Bus Lines", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("4112", "Passenger Railways", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("4511", "Airlines, Air Carriers", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("5541", "Service Stations (Fuel)", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("5542", "Automated Fuel Dispensers", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("7523", "Parking Lots/Garages", AppCategory.TRAVEL_AND_TRANSPORT),
                new MccCategoryMapping("4789", "Transportation Services, Other", AppCategory.TRAVEL_AND_TRANSPORT),

                // Shopping
                new MccCategoryMapping("5311", "Department Stores", AppCategory.SHOPPING),
                new MccCategoryMapping("5399", "Misc. General Merchandise", AppCategory.SHOPPING),
                new MccCategoryMapping("5651", "Family Clothing Stores", AppCategory.SHOPPING),
                new MccCategoryMapping("5732", "Electronics Stores", AppCategory.SHOPPING),
                new MccCategoryMapping("5691", "Men's/Women's Clothing Stores", AppCategory.SHOPPING),
                new MccCategoryMapping("5964", "Direct Marketing - Catalog Merchant", AppCategory.SHOPPING),
                new MccCategoryMapping("5969", "Direct Marketing - Other", AppCategory.SHOPPING),

                // Bills & Utilities
                new MccCategoryMapping("4814", "Telecommunication Services", AppCategory.BILLS_AND_UTILITIES),
                new MccCategoryMapping("4900", "Utilities - Electric, Gas, Water", AppCategory.BILLS_AND_UTILITIES),
                new MccCategoryMapping("4899", "Cable, Satellite, Other Pay TV", AppCategory.BILLS_AND_UTILITIES),

                // Entertainment
                new MccCategoryMapping("7832", "Motion Picture Theaters", AppCategory.ENTERTAINMENT),
                new MccCategoryMapping("7922", "Theatrical Producers, Ticket Agencies", AppCategory.ENTERTAINMENT),
                new MccCategoryMapping("5815", "Digital Goods - Media (Streaming)", AppCategory.ENTERTAINMENT),
                new MccCategoryMapping("7996", "Amusement Parks, Carnivals", AppCategory.ENTERTAINMENT),

                // Healthcare
                new MccCategoryMapping("8011", "Doctors, Physicians", AppCategory.HEALTHCARE),
                new MccCategoryMapping("8062", "Hospitals", AppCategory.HEALTHCARE),
                new MccCategoryMapping("5912", "Drug Stores, Pharmacies", AppCategory.HEALTHCARE),
                new MccCategoryMapping("8021", "Dentists, Orthodontists", AppCategory.HEALTHCARE),
                new MccCategoryMapping("8099", "Medical Services, Other", AppCategory.HEALTHCARE),

                // Education
                new MccCategoryMapping("8211", "Elementary/Secondary Schools", AppCategory.EDUCATION),
                new MccCategoryMapping("8220", "Colleges, Universities", AppCategory.EDUCATION),
                new MccCategoryMapping("8299", "Schools, Educational Services, Other", AppCategory.EDUCATION),

                // Investments
                new MccCategoryMapping("6211", "Securities Brokers/Dealers", AppCategory.INVESTMENTS),
                new MccCategoryMapping("6051", "Non-FI, Money Orders", AppCategory.INVESTMENTS)
        );

        repository.saveAll(seed);
    }
}
