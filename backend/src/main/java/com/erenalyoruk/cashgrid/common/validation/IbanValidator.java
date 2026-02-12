package com.erenalyoruk.cashgrid.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigInteger;

public class IbanValidator implements ConstraintValidator<ValidIban, String> {

    private static final int TR_IBAN_LENGTH = 26;
    private static final String TR_PREFIX = "TR";
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String iban = value.replaceAll("\\s", "").toUpperCase();

        // TR IBAN must be exactly 26 characters
        if (iban.length() != TR_IBAN_LENGTH) {
            return false;
        }

        // Must start with TR
        if (!iban.startsWith(TR_PREFIX)) {
            return false;
        }

        // Characters 3-26 must be digits
        String afterCountryCode = iban.substring(2);
        if (!afterCountryCode.matches("\\d+")) {
            return false;
        }

        // Mod-97 check (ISO 7064)
        return validateMod97(iban);
    }

    private boolean validateMod97(String iban) {
        // Move first 4 chars to the end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numeric = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numeric.append(Character.getNumericValue(c));
            } else {
                numeric.append(c);
            }
        }

        BigInteger ibanNumber = new BigInteger(numeric.toString());
        return ibanNumber.mod(MOD_97).intValue() == 1;
    }
}
