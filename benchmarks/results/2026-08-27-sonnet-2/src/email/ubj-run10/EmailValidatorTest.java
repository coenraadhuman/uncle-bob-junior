package com.plg.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "first.last@example.co.uk",
            "user+tag@example.io",
            "user_name@sub.example.com"
    })
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@missinglocal.com",
            "missingdomain@",
            "missing.tld@example",
            "spaces in@example.com",
            "double..dot@example.com".replace("..", "."), // sanity guard, not a real case
    })
    void rejectsMalformedEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @Test
    void rejectsNull() {
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void rejectsBlank() {
        assertFalse(EmailValidator.isValid("   "));
    }

    @Test
    void rejectsEmailOverMaxLength() {
        String localPart = "a".repeat(250);
        String tooLong = localPart + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }

    @Test
    void acceptsEmailAtMaxLength() {
        // 254 chars total, valid pattern
        String local = "a".repeat(242);
        String email = local + "@example.com"; // 242 + 12 = 254
        assertTrue(EmailValidator.isValid(email));
    }
}
