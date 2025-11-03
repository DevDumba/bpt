package com.example.bpt.util;

public class AccountUtils {

    /**
            * Normalizes account number to standard format.
            * Example:
            * Input: 205-52131-68 → Output: 205-000052131-68
            */
    public static String normalizeAccountNumber(String accountNumber) {
        if (accountNumber == null || !accountNumber.contains("-")) {
            return accountNumber;
        }

        String[] parts = accountNumber.split("-");
        if (parts.length != 3) {
            return accountNumber; // invalid structure, skip normalization
        }

        String bankCode = parts[0];
        String middlePart = parts[1];
        String controlDigits = parts[2];

        // Validate that all parts are numeric
        if (!bankCode.matches("\\d{3}") || !middlePart.matches("\\d+") || !controlDigits.matches("\\d{2}")) {
            return accountNumber;
        }

        // Pad middle part with zeros on the left to reach 13 digits
        String paddedMiddle = String.format("%013d", Long.parseLong(middlePart));

        return bankCode + "-" + paddedMiddle + "-" + controlDigits;
    }

}
