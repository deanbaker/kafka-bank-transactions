package com.digio.kafka.transactions;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class LegacyBankingSystem {

    private final List<String> customers = Arrays.asList("Alice", "Bob", "Charlie", "Dan", "Edwardo", "Fran");
    private final List<String> categories = Arrays.asList("CG01", "CG02", "CG03", "CG04");
    private final Random rand = new Random(90210);

    public Transaction getTransaction() {

        return new Transaction()
                .setAmount(rand.nextInt(100))
                .setCustomer(randomCustomer())
                .setCategory(categories.get(rand.nextInt(categories.size())))
                .setOccurred(new Date().getTime());
    }

    private String randomCustomer() {

        return customers.get(rand.nextInt(customers.size()));
    }
}
