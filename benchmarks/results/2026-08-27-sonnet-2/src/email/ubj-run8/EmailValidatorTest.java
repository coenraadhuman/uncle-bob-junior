package com.example.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    @Test
    void acceptsStandardAddress() {
        assertTrue(EmailValidator.isValid("participant_001@example.com"));
    }

    @Test
    void acceptsAddressWithPlusTagAndSubdomain() {
        assertTrue(EmailValidator.isValid("first.last+tag@mail.example.co.uk"));
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
    void rejectsMissingAtSymbol() {
        assertFalse(EmailValidator.isValid("participant.example.com"));
    }

    @Test
    void rejectsMissingDomain() {
        assertFalse(EmailValidator.isValid("participant@"));
    }

    @Test
    void rejectsMissingTopLevelDomain() {
        assertFalse(EmailValidator.isValid("participant@example"));
    }

    @Test
    void rejectsMultipleAtSymbols() {
        assertFalse(EmailValidator.isValid("participant@@example.com"));
    }

    @Test
    void rejectsWhitespaceInsideAddress() {
        assertFalse(EmailValidator.isValid("participant @example.com"));
    }

    @Test
    void rejectsAddressExceedingMaxLength() {
        String tooLong = "a".repeat(250) + "@example.com";
        assertFalse(EmailValidator.isValid(tooLong));
    }
}
