using System;
using System.Collections.Generic;
using System.Linq;

public enum ClaimCategory
{
    Travel,
    Meals,
    Equipment
}

public enum ApprovalLevel
{
    None,
    ManagerApproval,
    FinanceApproval
}

public record ExpenseClaim(
    string EmployeeId,
    string EmployeeName,
    decimal Amount,
    ClaimCategory Category,
    DateTime ClaimDate,
    bool ReceiptAttached,
    string Description = "")
{
    public ApprovalLevel RequiredApproval => Amount switch
    {
        > 1000 => ApprovalLevel.FinanceApproval,
        > 200 => ApprovalLevel.ManagerApproval,
        _ => ApprovalLevel.None
    };
}

public record ClaimValidationResult(
    bool IsValid,
    List<string> Errors,
    ApprovalLevel RequiredApproval = ApprovalLevel.None);

public class MonthlyCategoryLimits
{
    private const decimal TravelLimit = 500;
    private const decimal MealsLimit = 150;
    private const decimal EquipmentLimit = 1000;

    public decimal GetLimit(ClaimCategory category) => category switch
    {
        ClaimCategory.Travel => TravelLimit,
        ClaimCategory.Meals => MealsLimit,
        ClaimCategory.Equipment => EquipmentLimit,
        _ => throw new ArgumentOutOfRangeException(nameof(category))
    };
}

public class ExpenseClaimValidator
{
    private const decimal ReceiptRequiredThreshold = 25;
    private readonly MonthlyCategoryLimits limits;

    public ExpenseClaimValidator()
    {
        limits = new MonthlyCategoryLimits();
    }

    public ClaimValidationResult Validate(ExpenseClaim claim)
    {
        var errors = new List<string>();
        
        if (claim.Amount <= 0)
            errors.Add("Amount must be positive");
        
        if (!IsValidCategory(claim.Category))
            errors.Add("Category is not recognized");
        
        if (claim.Amount > ReceiptRequiredThreshold && !claim.ReceiptAttached)
            errors.Add($"Receipt required for amounts over {ReceiptRequiredThreshold} EUR");

        var isValid = errors.Count == 0;
        return new ClaimValidationResult(
            isValid,
            errors,
            isValid ? claim.RequiredApproval : ApprovalLevel.None);
    }

    public ClaimValidationResult ValidateMonthlyLimit(
        ExpenseClaim claim,
        List<ExpenseClaim> approvedClaimsThisMonth)
    {
        var result = Validate(claim);
        
        if (!result.IsValid)
            return result;

        var monthlyTotal = approvedClaimsThisMonth
            .Where(c => c.Category == claim.Category)
            .Sum(c => c.Amount);

        var limit = limits.GetLimit(claim.Category);
        
        if (monthlyTotal + claim.Amount > limit)
            return new ClaimValidationResult(
                false,
                new List<string> { $"{claim.Category} monthly limit exceeded: {monthlyTotal} + {claim.Amount} > {limit}" });

        return result;
    }

    private static bool IsValidCategory(ClaimCategory category)
    {
        return Enum.IsDefined(typeof(ClaimCategory), category);
    }
}

public class ExpenseClaimProcessor
{
    private readonly ExpenseClaimValidator validator;
    private readonly List<ExpenseClaim> approvedClaims;

    public ExpenseClaimProcessor()
    {
        validator = new ExpenseClaimValidator();
        approvedClaims = new List<ExpenseClaim>();
    }

    public ClaimValidationResult ProcessClaim(ExpenseClaim claim)
    {
        var approvedThisMonth = GetApprovedClaimsForMonth(
            claim.EmployeeId,
            claim.ClaimDate.Year,
            claim.ClaimDate.Month);

        var validationResult = validator.ValidateMonthlyLimit(claim, approvedThisMonth);

        if (validationResult.IsValid)
            approvedClaims.Add(claim);

        return validationResult;
    }

    public ReimbursementReport GenerateMonthlyReport(string employeeId, int year, int month)
    {
        var claims = approvedClaims
            .Where(c => c.EmployeeId == employeeId 
                && c.ClaimDate.Year == year 
                && c.ClaimDate.Month == month)
            .ToList();

        var total = claims.Sum(c => c.Amount);
        var byCategory = claims
            .GroupBy(c => c.Category)
            .ToDictionary(g => g.Key, g => g.Sum(c => c.Amount));

        return new ReimbursementReport(employeeId, year, month, total, byCategory, claims);
    }

    private List<ExpenseClaim> GetApprovedClaimsForMonth(
        string employeeId,
        int year,
        int month)
    {
        return approvedClaims
            .Where(c => c.EmployeeId == employeeId 
                && c.ClaimDate.Year == year 
                && c.ClaimDate.Month == month)
            .ToList();
    }
}

public class ReimbursementReport
{
    public string EmployeeId { get; }
    public int Year { get; }
    public int Month { get; }
    public decimal TotalAmount { get; }
    public Dictionary<ClaimCategory, decimal> AmountByCategory { get; }
    public List<ExpenseClaim> Claims { get; }

    public ReimbursementReport(
        string employeeId,
        int year,
        int month,
        decimal totalAmount,
        Dictionary<ClaimCategory, decimal> amountByCategory,
        List<ExpenseClaim> claims)
    {
        EmployeeId = employeeId;
        Year = year;
        Month = month;
        TotalAmount = totalAmount;
        AmountByCategory = amountByCategory;
        Claims = claims;
    }

    public override string ToString()
    {
        var lines = new List<string>();
        lines.Add($"Reimbursement Report - Employee {EmployeeId}");
        lines.Add($"Period: {Month:D2}/{Year}");
        lines.Add($"Total Reimbursement: {TotalAmount:C}");
        lines.Add("Breakdown by category:");
        
        foreach (var (category, amount) in AmountByCategory)
            lines.Add($"  {category}: {amount:C}");
        
        lines.Add("Claims:");
        foreach (var claim in Claims.OrderBy(c => c.ClaimDate))
            lines.Add($"  {claim.ClaimDate:dd-MM-yyyy} {claim.Category} {claim.Amount:C} - {claim.Description}");

        return string.Join(Environment.NewLine, lines);
    }
}

public class ExpenseClaimProcessorTests
{
    public static void RunAll()
    {
        TestValidClaimWithReceipt();
        TestReceiptRequiredOver25();
        TestRejectNegativeAmount();
        TestManagerApprovalThreshold();
        TestFinanceApprovalThreshold();
        TestMonthlyCategoryLimitEnforcement();
        TestReimbursementReportGeneration();
        TestMultipleEmployees();
    }

    private static void TestValidClaimWithReceipt()
    {
        var processor = new ExpenseClaimProcessor();
        var claim = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            100,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true,
            "Flight to Amsterdam");

        var result = processor.ProcessClaim(claim);

        Assert(result.IsValid, "Valid claim with receipt should be accepted");
    }

    private static void TestReceiptRequiredOver25()
    {
        var processor = new ExpenseClaimProcessor();
        
        var claimUnder25 = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            20,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: false,
            "Taxi");

        var result = processor.ProcessClaim(claimUnder25);
        Assert(result.IsValid, "Claim under 25 EUR without receipt should be valid");

        var claimOver25 = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            50,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: false,
            "Train");

        result = processor.ProcessClaim(claimOver25);
        Assert(!result.IsValid && result.Errors.Any(e => e.Contains("Receipt")),
            "Claim over 25 EUR without receipt should require receipt");
    }

    private static void TestRejectNegativeAmount()
    {
        var processor = new ExpenseClaimProcessor();
        var claim = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            -50,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true);

        var result = processor.ProcessClaim(claim);

        Assert(!result.IsValid && result.Errors.Any(e => e.Contains("positive")),
            "Negative amount should be rejected");
    }

    private static void TestManagerApprovalThreshold()
    {
        var processor = new ExpenseClaimProcessor();
        var claim = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            300,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true);

        var result = processor.ProcessClaim(claim);

        Assert(result.IsValid && result.RequiredApproval == ApprovalLevel.ManagerApproval,
            "Claim over 200 EUR should require manager approval");
    }

    private static void TestFinanceApprovalThreshold()
    {
        var processor = new ExpenseClaimProcessor();
        var claim = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            1500,
            ClaimCategory.Equipment,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true);

        var result = processor.ProcessClaim(claim);

        Assert(result.IsValid && result.RequiredApproval == ApprovalLevel.FinanceApproval,
            "Claim over 1000 EUR should require finance approval");
    }

    private static void TestMonthlyCategoryLimitEnforcement()
    {
        var processor = new ExpenseClaimProcessor();

        var claim1 = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            300,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true);
        var result1 = processor.ProcessClaim(claim1);
        Assert(result1.IsValid, "First claim within limit");

        var claim2 = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            150,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 20),
            ReceiptAttached: true);
        var result2 = processor.ProcessClaim(claim2);
        Assert(result2.IsValid, "Second claim within limit (450 total)");

        var claim3 = new ExpenseClaim(
            "EMP_001",
            "John Doe",
            100,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 25),
            ReceiptAttached: true);
        var result3 = processor.ProcessClaim(claim3);
        Assert(!result3.IsValid && result3.Errors.Any(e => e.Contains("limit exceeded")),
            "Third claim exceeding 500 EUR travel limit should be rejected");
    }

    private static void TestReimbursementReportGeneration()
    {
        var processor = new ExpenseClaimProcessor();

        processor.ProcessClaim(new ExpenseClaim(
            "EMP_001",
            "John Doe",
            100,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true));

        processor.ProcessClaim(new ExpenseClaim(
            "EMP_001",
            "John Doe",
            50,
            ClaimCategory.Meals,
            new DateTime(2026, 08, 20),
            ReceiptAttached: true));

        var report = processor.GenerateMonthlyReport("EMP_001", 2026, 8);

        Assert(report.TotalAmount == 150, "Total should be 150 EUR");
        Assert(report.AmountByCategory[ClaimCategory.Travel] == 100, "Travel should be 100 EUR");
        Assert(report.AmountByCategory[ClaimCategory.Meals] == 50, "Meals should be 50 EUR");
        Assert(report.Claims.Count == 2, "Report should contain 2 claims");
    }

    private static void TestMultipleEmployees()
    {
        var processor = new ExpenseClaimProcessor();

        processor.ProcessClaim(new ExpenseClaim(
            "EMP_001",
            "John Doe",
            100,
            ClaimCategory.Travel,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true));

        processor.ProcessClaim(new ExpenseClaim(
            "EMP_002",
            "Jane Smith",
            75,
            ClaimCategory.Meals,
            new DateTime(2026, 08, 15),
            ReceiptAttached: true));

        var report1 = processor.GenerateMonthlyReport("EMP_001", 2026, 8);
        var report2 = processor.GenerateMonthlyReport("EMP_002", 2026, 8);

        Assert(report1.TotalAmount == 100 && report1.Claims.Count == 1,
            "Employee 1 report should show only their claim");
        Assert(report2.TotalAmount == 75 && report2.Claims.Count == 1,
            "Employee 2 report should show only their claim");
    }

    private static void Assert(bool condition, string message)
    {
        if (!condition)
            throw new Exception($"Test failed: {message}");
    }
}

public class Program
{
    public static void Main()
    {
        try
        {
            ExpenseClaimProcessorTests.RunAll();
            Console.WriteLine("All tests passed!");
            
            Console.WriteLine("\n--- Sample Report ---");
            var processor = new ExpenseClaimProcessor();
            processor.ProcessClaim(new ExpenseClaim(
                "EMP_001",
                "John Doe",
                350,
                ClaimCategory.Travel,
                new DateTime(2026, 08, 10),
                ReceiptAttached: true,
                "Client meeting in Brussels"));
            
            processor.ProcessClaim(new ExpenseClaim(
                "EMP_001",
                "John Doe",
                45,
                ClaimCategory.Meals,
                new DateTime(2026, 08, 10),
                ReceiptAttached: true,
                "Team lunch"));

            Console.WriteLine(processor.GenerateMonthlyReport("EMP_001", 2026, 8));
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error: {ex.Message}");
        }
    }
}
