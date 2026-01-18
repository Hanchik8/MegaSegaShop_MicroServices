package org.example.megasegashop.payment.service;

public final class LuhnValidator {
    private LuhnValidator() {
    }

    public static boolean isValid(String digits) {
        if (digits == null || digits.isBlank()) {
            return false;
        }
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            char ch = digits.charAt(i);
            if (!Character.isDigit(ch)) {
                return false;
            }
            int digit = ch - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
