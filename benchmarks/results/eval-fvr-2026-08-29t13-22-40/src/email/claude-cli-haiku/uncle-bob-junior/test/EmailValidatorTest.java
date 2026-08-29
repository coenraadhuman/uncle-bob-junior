import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest {
    @Test
    void acceptsValidEmails() {
        assertThat(EmailValidator.isValid("user@example.com")).isTrue();
        assertThat(EmailValidator.isValid("john.doe@company.co.uk")).isTrue();
        assertThat(EmailValidator.isValid("test+tag@domain.org")).isTrue();
        assertThat(EmailValidator.isValid("a@b.co")).isTrue();
    }

    @Test
    void rejectsInvalidEmails() {
        assertThat(EmailValidator.isValid(null)).isFalse();
        assertThat(EmailValidator.isValid("")).isFalse();
        assertThat(EmailValidator.isValid("   ")).isFalse();
        assertThat(EmailValidator.isValid("plainaddress")).isFalse();
        assertThat(EmailValidator.isValid("user@domain")).isFalse();
        assertThat(EmailValidator.isValid("@example.com")).isFalse();
        assertThat(EmailValidator.isValid("user@.com")).isFalse();
        assertThat(EmailValidator.isValid("user..name@example.com")).isFalse();
    }
}
