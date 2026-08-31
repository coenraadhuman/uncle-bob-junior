using Xunit;

public class ClaimValidatorTests
{
    private readonly ClaimValidator validator = new();

    [Fact]
    public void ValidatesKnownCategory()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(50m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Valid");

        Assert.True(validator.Validate(claim).IsValid);
    }

    [Fact]
    public void RequiresReceiptOver25Eur()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(30m),
            ExpenseCategory.Travel,
            DateTime.Now,
            false,
            "No receipt");

        var result = validator.Validate(claim);
        Assert.False(result.IsValid);
        Assert.Contains("Receipt required", result.Errors.First());
    }

    [Fact]
    public void AllowsUnder25EurWithoutReceipt()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(20m),
            ExpenseCategory.Meals,
            DateTime.Now,
            false,
            "No receipt");

        Assert.True(validator.Validate(claim).IsValid);
    }
}

public class MonthlyCapsEnforcerTests
{
    private readonly MonthlyCapsEnforcer enforcer = new();

    [Theory]
    [InlineData(ExpenseCategory.Travel, 500m)]
    [InlineData(ExpenseCategory.Meals, 150m)]
    [InlineData(ExpenseCategory.Equipment, 1000m)]
    public void EnforcesCategoryCapLimit(ExpenseCategory category, decimal cap)
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(cap + 1),
            category,
            DateTime.Now,
            true,
            "Over cap");

        var result = enforcer.EnforceCaps("EMP001", claim, new List<ExpenseClaim>());
        Assert.False(result.IsValid);
        Assert.Contains("Exceeds", result.Errors.First());
    }

    [Fact]
    public void AllowsClaimWithinRemainingBudget()
    {
        var approved = new ExpenseClaim(
            "EMP001",
            new Money(100m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "First");

        var next = new ExpenseClaim(
            "EMP001",
            new Money(300m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Second");

        var result = enforcer.EnforceCaps("EMP001", next, new() { approved });
        Assert.True(result.IsValid);
    }
}

public class ApprovalRouterTests
{
    private readonly ApprovalRouter router = new();

    [Fact]
    public void RoutesUnder200ToAutoApprove()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(150m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Small");

        Assert.Equal(ApprovalRoute.AutoApproved, router.Route(claim));
    }

    [Fact]
    public void RoutesOver200ToManager()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(250m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Medium");

        Assert.Equal(ApprovalRoute.Manager, router.Route(claim));
    }

    [Fact]
    public void RoutesOver1000ToFinance()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(1500m),
            ExpenseCategory.Equipment,
            DateTime.Now,
            true,
            "Large");

        Assert.Equal(ApprovalRoute.Finance, router.Route(claim));
    }
}

public class ExpenseClaimProcessorTests
{
    private readonly ExpenseClaimProcessor processor = new();

    [Fact]
    public void ProcessorAutoApprovesSmallValidClaim()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(100m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Small claim");

        var result = processor.ProcessClaim(claim);
        Assert.Equal(ProcessingStatus.AutoApproved, result.Status);
        Assert.Equal(ClaimStatus.ApprovedByFinance, claim.Status);
    }

    [Fact]
    public void ProcessorRejectsMissingReceipt()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(50m),
            ExpenseCategory.Meals,
            DateTime.Now,
            false,
            "No receipt");

        var result = processor.ProcessClaim(claim);
        Assert.Equal(ProcessingStatus.Rejected, result.Status);
        Assert.NotEmpty(result.RejectionReasons);
    }

    [Fact]
    public void ProcessorRoutesManagerApproval()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(250m),
            ExpenseCategory.Travel,
            DateTime.Now,
            true,
            "Manager approval");

        var result = processor.ProcessClaim(claim);
        Assert.Equal(ProcessingStatus.AwaitingManagerApproval, result.Status);
    }

    [Fact]
    public void ProcessorRoutesFinanceApproval()
    {
        var claim = new ExpenseClaim(
            "EMP001",
            new Money(1500m),
            ExpenseCategory.Equipment,
            DateTime.Now,
            true,
            "Finance approval");

        var result = processor.ProcessClaim(claim);
        Assert.Equal(ProcessingStatus.AwaitingFinanceApproval, result.Status);
    }

    [Fact]
    public void ProcessorEnforcesMonthlyCaps()
    {
        var first = new ExpenseClaim(
            "EMP002",
            new Money(100m),
            ExpenseCategory.Meals,
            new DateTime(2026, 8, 5),
            true,
            "Meal 1");

        var second = new ExpenseClaim(
            "EMP002",
            new Money(60m),
            ExpenseCategory.Meals,
            new DateTime(2026, 8, 15),
            true,
            "Meal 2");

        processor.ProcessClaim(first);
        var result = processor.ProcessClaim(second);

        Assert.Equal(ProcessingStatus.Rejected, result.Status);
        Assert.Contains("Exceeds", result.RejectionReasons.First());
    }
}

public class ReimbursementReporterTests
{
    [Fact]
    public void GeneratesPerEmployeeMonthlyReport()
    {
        var claims = new List<ExpenseClaim>
        {
            CreateFinanceApproved("EMP001", 100m, ExpenseCategory.Travel, 2026, 8, 5),
            CreateFinanceApproved("EMP001", 50m, ExpenseCategory.Meals, 2026, 8, 10),
            CreateFinanceApproved("EMP002", 200m, ExpenseCategory.Equipment, 2026, 8, 15)
        };

        var reporter = new ReimbursementReporter();
        var report = reporter.GenerateMonthlyReport(claims, 2026, 8);

        Assert.Equal(2, report.Count);
        Assert.Equal("EMP001", report[0].EmployeeId);
        Assert.Equal(150m, report[0].TotalApproved.Amount);
        Assert.Equal(2, report[0].ApprovedByCategory.Count);
    }

    [Fact]
    public void ExcludesNonFinanceApprovedClaims()
    {
        var approved = CreateFinanceApproved("EMP001", 100m, ExpenseCategory.Travel, 2026, 8, 5);
        var pending = new ExpenseClaim("EMP001", new Money(50m), ExpenseCategory.Meals, 
            new DateTime(2026, 8, 10), true, "Pending");

        var reporter = new ReimbursementReporter();
        var report = reporter.GenerateMonthlyReport(new() { approved, pending }, 2026, 8);

        Assert.Single(report);
        Assert.Equal(100m, report[0].TotalApproved.Amount);
    }

    private static ExpenseClaim CreateFinanceApproved(
        string employeeId,
        decimal amount,
        ExpenseCategory category,
        int year,
        int month,
        int day)
    {
        var claim = new ExpenseClaim(
            employeeId,
            new Money(amount),
            category,
            new DateTime(year, month, day),
            true,
            "Test");
        claim.ApproveByFinance();
        return claim;
    }
}
