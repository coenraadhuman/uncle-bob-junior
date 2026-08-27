import jakarta.validation.constraints.Email;

public class User {
    @Email(message = "Invalid email address")
    private String email;
}
