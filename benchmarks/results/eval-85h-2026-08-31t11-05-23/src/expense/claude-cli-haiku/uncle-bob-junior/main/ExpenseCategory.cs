using System;
using System.Collections.Generic;
using System.Linq;

// Domain Models
public enum ExpenseCategory
{
    Travel,
    Meals,
    Equipment
}

public class Money : IEquatable<Money>
{
    public decimal Amount { get; }
    public const string Currency = "EUR";

    public Money(decimal amount)
    {
        if (amount <= 0)
            throw new ArgumentException("Amount must be positive", nameof(amount));
        Amount = amount;
    }

    public bool Equals(Money other) => other != null && Amount == other.Amount;
    public override bool Equals(object obj) => Equals(obj as Money);
    public override int GetHashCode() => Amount.GetHashCode();
    public override string ToString() => $"{Amount:F2} {Currency}";
}

public class ExpenseClaim
{
    public string EmployeeId { get; }
    public Money Amount { get; }
    public ExpenseCategory Category { get; }
    public DateTime Date { get; }
    public bool ReceiptAttached { get; }
    public string Description { get; }
    public ClaimStatus Status { get; private set; }

    public ExpenseClaim(
        string employeeId,
        Money amount,
        ExpenseCategory category,
        DateTime date,
        bool receiptAttached,
        string description)
    {
        EmployeeId = employeeId;
        Amount = amount;
        Category = category;
        Date = date;
        ReceiptAttached = receiptAttached;
        Description = description;
        Status = ClaimStatus.Pending;
    }

    public void ApproveByManager() => Status = ClaimStatus.ApprovedByManager;
    public void ApproveByFinance() => Status = ClaimStatus.ApprovedByFinance;
    public void Reject() => Status = ClaimStatus.Rejected;
}

public enum ClaimStatus
{
    Pending,
    ApprovedByManager,
    ApprovedByFinance,
    Rejected
}

public class ValidationResult
{
    public bool IsValid { get; }
    public List<string> Errors { get; }

    private ValidationResult(bool isValid, List<string> errors)
    {
        IsValid = isValid;
        Errors = errors;
    }

    public static ValidationResult Valid() => new(true, new());
    public static ValidationResult Invalid(List<string> errors) => new(false, errors);
}

// Validators
public class ClaimValidator
{
    private const decimal ReceiptThreshold = 25m;

    public ValidationResult Validate(ExpenseClaim claim)
    {
        var errors = new List<string>();

        if (!IsKnownCategory(claim.Category))
            errors.Add($"Unknown category: {claim.Category}");

        if (claim.Amount.Amount > ReceiptThreshold && !claim.ReceiptAttached)
            errors.Add($"Receipt required for amounts over {ReceiptThreshold} EUR");

        return errors.Count > 0
            ? ValidationResult.Invalid(errors)
            : ValidationResult.Valid();
    }

    private bool IsKnownCategory(ExpenseCategory category)
    {
        return Enum.IsDefined(typeof(ExpenseCategory), category);
    }
}

public class MonthlyCapsEnforcer
{
    private static readonly Dictionary<ExpenseCategory, decimal> Caps = new()
    {
        { ExpenseCategory.Travel, 500m },
        { ExpenseCategory.Meals, 150m },
        { ExpenseCategory.Equipment, 1000m }
    };

    public ValidationResult EnforceCaps(
        string employeeId,
        ExpenseClaim newClaim,
        IEnumerable<ExpenseClaim> approvedThisMonth)
    {
        var errors = new List<string>();
        var sumThisMonth = approvedThisMonth
            .Where(c => c.EmployeeId == employeeId && c.Category == newClaim.Category)
            .Sum(c => c.Amount.Amount);

        var cap = Caps[newClaim.Category];
        var remaining = cap - sumThisMonth;
        
        if (newClaim.Amount.Amount > remaining)
            errors.Add(
                $"Exceeds {newClaim.Category} monthly cap (€{cap}). " +
                $"Already claimed: €{sumThisMonth}, remaining: €{remaining}");

        return errors.Count > 0
            ? ValidationResult.Invalid(errors)
            : ValidationResult.Valid();
    }
}

public class ApprovalRouter
{
    private const decimal ManagerThreshold = 200m;
    private const decimal FinanceThreshold = 1000m;

    public ApprovalRoute Route(ExpenseClaim claim)
    {
        if (claim.Amount.Amount > FinanceThreshold)
            return ApprovalRoute.Finance;
        if (claim.Amount.Amount > ManagerThreshold)
            return ApprovalRoute.Manager;
        return ApprovalRoute.AutoApproved;
    }
}

public enum ApprovalRoute
{
    AutoApproved,
    Manager,
    Finance
}

// Reporting
public class EmployeeMonthlyReimbursement
{
    public string EmployeeId { get; }
    public int Year { get; }
    public int Month { get; }
    public Dictionary<ExpenseCategory, Money> ApprovedByCategory { get; }
    public Money TotalApproved { get; }

    public EmployeeMonthlyReimbursement(
        string employeeId,
        int year,
        int month,
        Dictionary<ExpenseCategory, Money> approvedByCategory,
        Money totalApproved)
    {
        EmployeeId = employeeId;
        Year = year;
        Month = month;
        ApprovedByCategory = approvedByCategory;
        TotalApproved = totalApproved;
    }
}

public class ReimbursementReporter
{
    public List<EmployeeMonthlyReimbursement> GenerateMonthlyReport(
        IEnumerable<ExpenseClaim> allClaims,
        int year,
        int month)
    {
        var financeApproved = allClaims
            .Where(c => c.Date.Year == year && 
                        c.Date.Month == month && 
                        c.Status == ClaimStatus.ApprovedByFinance)
            .GroupBy(c => c.EmployeeId)
            .Select(g => BuildReimbursement(g, year, month))
            .OrderBy(r => r.EmployeeId)
            .ToList();

        return financeApproved;
    }

    private static EmployeeMonthlyReimbursement BuildReimbursement(
        IGrouping<string, ExpenseClaim> employeeGroup,
        int year,
        int month)
    {
        var byCategory = employeeGroup
            .GroupBy(c => c.Category)
            .ToDictionary(
                g => g.Key,
                g => new Money(g.Sum(c => c.Amount.Amount)));

        var total = new Money(byCategory.Values.Sum(m => m.Amount));

        return new EmployeeMonthlyReimbursement(
            employeeGroup.Key,
            year,
            month,
            byCategory,
            total);
    }
}

// Processor
public class ExpenseClaimProcessor
{
    private readonly ClaimValidator claimValidator;
    private readonly MonthlyCapsEnforcer capsEnforcer;
    private readonly ApprovalRouter router;
    private readonly List<ExpenseClaim> allClaims;

    public ExpenseClaimProcessor()
    {
        claimValidator = new ClaimValidator();
        capsEnforcer = new MonthlyCapsEnforcer();
        router = new ApprovalRouter();
        allClaims = new List<ExpenseClaim>();
    }

    public ProcessingResult ProcessClaim(ExpenseClaim claim)
    {
        var validation = claimValidator.Validate(claim);
        if (!validation.IsValid)
            return ProcessingResult.Rejected(validation.Errors);

        var approvedThisMonth = GetApprovedForPeriod(claim.EmployeeId, claim.Date.Year, claim.Date.Month);
        var capCheck = capsEnforcer.EnforceCaps(claim.EmployeeId, claim, approvedThisMonth);
        
        if (!capCheck.IsValid)
            return ProcessingResult.Rejected(capCheck.Errors);

        allClaims.Add(claim);
        var route = router.Route(claim);

        return route switch
        {
            ApprovalRoute.AutoApproved => ApproveAndReturn(claim, ProcessingStatus.AutoApproved),
            ApprovalRoute.Manager => ProcessingResult.AwaitingManagerApproval(claim),
            ApprovalRoute.Finance => ProcessingResult.AwaitingFinanceApproval(claim),
            _ => throw new InvalidOperationException("Unknown route")
        };
    }

    public List<EmployeeMonthlyReimbursement> GenerateMonthlyReport(int year, int month)
    {
        var reporter = new ReimbursementReporter();
        return reporter.GenerateMonthlyReport(allClaims, year, month);
    }

    public IReadOnlyList<ExpenseClaim> GetAllClaims() => allClaims.AsReadOnly();

    private List<ExpenseClaim> GetApprovedForPeriod(string employeeId, int year, int month)
    {
        return allClaims
            .Where(c => c.EmployeeId == employeeId &&
                        c.Date.Year == year &&
                        c.Date.Month == month &&
                        c.Status == ClaimStatus.ApprovedByFinance)
            .ToList();
    }

    private ProcessingResult ApproveAndReturn(ExpenseClaim claim, ProcessingStatus status)
    {
        claim.ApproveByFinance();
        return new ProcessingResult(status, claim, new());
    }
}

public class ProcessingResult
{
    public ProcessingStatus Status { get; }
    public ExpenseClaim Claim { get; }
    public List<string> RejectionReasons { get; }

    private ProcessingResult(ProcessingStatus status, ExpenseClaim claim, List<string> reasons)
    {
        Status = status;
        Claim = claim;
        RejectionReasons = reasons;
    }

    public static ProcessingResult AwaitingManagerApproval(ExpenseClaim claim) =>
        new(ProcessingStatus.AwaitingManagerApproval, claim, new());

    public static ProcessingResult AwaitingFinanceApproval(ExpenseClaim claim) =>
        new(ProcessingStatus.AwaitingFinanceApproval, claim, new());

    public static ProcessingResult Rejected(List<string> reasons) =>
        new(ProcessingStatus.Rejected, null, reasons);
}

public enum ProcessingStatus
{
    AutoApproved,
    AwaitingManagerApproval,
    AwaitingFinanceApproval,
    Rejected
}
