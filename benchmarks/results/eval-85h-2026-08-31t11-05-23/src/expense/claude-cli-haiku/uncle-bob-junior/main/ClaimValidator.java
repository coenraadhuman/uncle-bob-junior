import java.util.*;

public class ClaimValidator {
    private static final int RECEIPT_THRESHOLD_EUROS = 25;
    
    public ClaimValidation validate(ExpenseClaim claim) {
        List<String> errors = new ArrayList<>();
        
        if (claim.amountEuros() <= 0) {
            errors.add("Amount must be positive");
        }
        
        if (claim.amountEuros() > RECEIPT_THRESHOLD_EUROS && !claim.receiptAttached()) {
            errors.add("Receipt required for claims over " + RECEIPT_THRESHOLD_EUROS + " euros");
        }
        
        return errors.isEmpty() 
            ? new ClaimValidation(true, List.of())
            : new ClaimValidation(false, errors);
    }
}
