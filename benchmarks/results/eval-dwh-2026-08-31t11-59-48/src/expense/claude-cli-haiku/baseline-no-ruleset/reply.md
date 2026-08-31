I'll create a complete C# expense claims management system with validation, approval routing, and monthly reporting.

**Assumptions:**
- Claims are validated and routed sequentially
- Monthly caps are checked cumulatively within a month
- Approval routing is based on amount thresholds
- Reports aggregate by employee, month, and category

```csharp
using System;
using System.Collections.Generic;
using System.Linq;

public enum ExpenseCategory
{
    Travel,
    Meals,
    Equipment
}

public enum ApprovalStatus
{
    Pending,
    ApprovedByManager,
    ApprovedByFinance,
    Rejected
}

public class ExpenseClaim
{
    public string ClaimId { get; set; }
    public string EmployeeId { get; set; }
    public string EmployeeName { get; set; }
    public decimal Amount { get; set; }
    public ExpenseCategory Category { get; set; }
    public DateTime ClaimDate { get; set; }
    public bool ReceiptAttached { get; set; }
    public string Description { get; set; }
    public ApprovalStatus Status { get; set; }
    public string RejectionReason { get; set; }
    public string ApprovedBy { get; set; }
}

public class ClaimValidator
{
    private const decimal RECEIPT_THRESHOLD = 25m;

    public (bool isValid, string errorMessage) Validate(ExpenseClaim claim)
    {
        if (claim.Amount <= 0)
            return (false, "Amount must be positive");

        if (!Enum.IsDefined(typeof(ExpenseCategory), claim.Category))
            return (false, "Unknown expense category");

        if (claim.Amount > RECEIPT_THRESHOLD && !claim.ReceiptAttached)
            return (false, $"Receipt required for amounts over {RECEIPT_THRESHOLD} euros");

        return (true, "");
    }
}

public class ExpenseProcessor
{
    private static readonly Dictionary<ExpenseCategory, decimal> MonthlyCaps = new()
    {
        { ExpenseCategory.Travel, 500m },
        { ExpenseCategory.Meals, 150m },
        { ExpenseCategory.Equipment, 1000m }
    };

    private const decimal MANAGER_APPROVAL_THRESHOLD = 200m;
    private const decimal FINANCE_APPROVAL_THRESHOLD = 1000m;

    private List<ExpenseClaim> allClaims = new();
    private ClaimValidator validator = new();

    public void ProcessClaim(ExpenseClaim claim)
    {
        var (isValid, error) = validator.Validate(claim);

        if (!isValid)
        {
            claim.Status = ApprovalStatus.Rejected;
            claim.RejectionReason = error;
            allClaims.Add(claim);
            return;
        }

        var monthlyUsed = GetMonthlyUsageForCategory(claim.EmployeeId, claim.Category, claim.ClaimDate);
        var cap = MonthlyCaps[claim.Category];

        if (monthlyUsed + claim.Amount > cap)
        {
            claim.Status = ApprovalStatus.Rejected;
            claim.RejectionReason = $"Monthly {claim.Category} limit of {cap} euros exceeded";
            allClaims.Add(claim);
            return;
        }

        RouteForApproval(claim);
        allClaims.Add(claim);
    }

    private decimal GetMonthlyUsageForCategory(string employeeId, ExpenseCategory category, DateTime claimDate)
    {
        return allClaims
            .Where(c => c.EmployeeId == employeeId 
                && c.Category == category 
                && c.ClaimDate.Year == claimDate.Year 
                && c.ClaimDate.Month == claimDate.Month
                && c.Status != ApprovalStatus.Rejected)
            .Sum(c => c.Amount);
    }

    private void RouteForApproval(ExpenseClaim claim)
    {
        if (claim.Amount > FINANCE_APPROVAL_THRESHOLD)
        {
            claim.Status = ApprovalStatus.Pending;
            claim.ApprovedBy = "Finance Team";
        }
        else if (claim.Amount > MANAGER_APPROVAL_THRESHOLD)
        {
            claim.Status = ApprovalStatus.Pending;
            claim.ApprovedBy = "Manager";
        }
        else
        {
            claim.Status = ApprovalStatus.ApprovedByManager;
            claim.ApprovedBy = "Auto-approved";
        }
    }

    public void ApproveClaimByManager(string claimId)
    {
        var claim = allClaims.FirstOrDefault(c => c.ClaimId == claimId);
        if (claim != null && claim.Amount <= FINANCE_APPROVAL_THRESHOLD)
        {
            claim.Status = ApprovalStatus.ApprovedByManager;
        }
    }

    public void ApproveClaimByFinance(string claimId)
    {
        var claim = allClaims.FirstOrDefault(c => c.ClaimId == claimId);
        if (claim != null)
        {
            claim.Status = ApprovalStatus.ApprovedByFinance;
        }
    }

    public void RejectClaim(string claimId, string reason)
    {
        var claim = allClaims.FirstOrDefault(c => c.ClaimId == claimId);
        if (claim != null)
        {
            claim.Status = ApprovalStatus.Rejected;
            claim.RejectionReason = reason;
        }
    }

    public void GenerateMonthlyReport(int year, int month)
    {
        var monthClaims = allClaims
            .Where(c => c.ClaimDate.Year == year && c.ClaimDate.Month == month)
            .GroupBy(c => c.EmployeeId)
            .OrderBy(g => g.Key);

        Console.WriteLine($"\n{'='} MONTHLY EXPENSE REPORT - {year:D4}-{month:D2} {'='}\n");

        foreach (var employeeGroup in monthClaims)
        {
            var employeeName = employeeGroup.First().EmployeeName;
            Console.WriteLine($"Employee: {employeeName} (ID: {employeeGroup.Key})");
            Console.WriteLine(new string('-', 80));

            var categoryBreakdown = employeeGroup
                .GroupBy(c => c.Category)
                .OrderBy(g => g.Key);

            decimal employeeTotal = 0;
            foreach (var categoryGroup in categoryBreakdown)
            {
                decimal categoryTotal = 0;
                Console.WriteLine($"  {categoryGroup.Key}:");

                foreach (var claim in categoryGroup)
                {
                    var statusStr = claim.Status == ApprovalStatus.Rejected 
                        ? $"REJECTED ({claim.RejectionReason})" 
                        : claim.Status.ToString();
                    
                    Console.WriteLine($"    {claim.ClaimId}: {claim.Amount:C} - {statusStr}");
                    
                    if (claim.Status != ApprovalStatus.Rejected)
                        categoryTotal += claim.Amount;
                }

                Console.WriteLine($"    Subtotal: {categoryTotal:C}");
                employeeTotal += categoryTotal;
            }

            Console.WriteLine($"  TOTAL REIMBURSEMENT: {employeeTotal:C}\n");
        }

        Console.WriteLine($"{'='} END OF REPORT {'='}\n");
    }
}

class Program
{
    static void Main()
    {
        var processor = new ExpenseProcessor();

        var claim1 = new ExpenseClaim
        {
            ClaimId = "CLM001",
            EmployeeId = "EMP001",
            EmployeeName = "John Smith",
            Amount = 150m,
            Category = ExpenseCategory.Travel,
            ClaimDate = new DateTime(2026, 8, 15),
            ReceiptAttached = true,
            Description = "Flight to Berlin"
        };
        processor.ProcessClaim(claim1);

        var claim2 = new ExpenseClaim
        {
            ClaimId = "CLM002",
            EmployeeId = "EMP001",
            EmployeeName = "John Smith",
            Amount = 45m,
            Category = ExpenseCategory.Meals,
            ClaimDate = new DateTime(2026, 8, 16),
            ReceiptAttached = false,
            Description = "Team lunch"
        };
        processor.ProcessClaim(claim2);

        var claim3 = new ExpenseClaim
        {
            ClaimId = "CLM003",
            EmployeeId = "EMP002",
            EmployeeName = "Sarah Johnson",
            Amount = 1500m,
            Category = ExpenseCategory.Equipment,
            ClaimDate = new DateTime(2026, 8, 20),
            ReceiptAttached = true,
            Description = "Monitor and keyboard"
        };
        processor.ProcessClaim(claim3);

        var claim4 = new ExpenseClaim
        {
            ClaimId = "CLM004",
            EmployeeId = "EMP001",
            EmployeeName = "John Smith",
            Amount = 350m,
            Category = ExpenseCategory.Travel,
            ClaimDate = new DateTime(2026, 8, 25),
            ReceiptAttached = true,
            Description = "Hotel accommodation"
        };
        processor.ProcessClaim(claim4);

        var claim5 = new ExpenseClaim
        {
            ClaimId = "CLM005",
            EmployeeId = "EMP001",
            EmployeeName = "John Smith",
            Amount = -50m,
            Category = ExpenseCategory.Meals,
            ClaimDate = new DateTime(2026, 8, 26),
            ReceiptAttached = true,
            Description = "Invalid claim"
        };
        processor.ProcessClaim(claim5);

        processor.ApproveClaimByManager("CLM001");
        processor.ApproveClaimByFinance("CLM003");

        processor.GenerateMonthlyReport(2026, 8);
    }
}
```

**Key features:**
- **Validation**: Checks positive amount, valid category, and receipt requirement
- **Monthly caps**: Tracks cumulative spending per employee per category
- **Approval routing**: Auto-approves under 200 euros, routes 200-1000 to manager, above 1000 to finance
- **Report generation**: Summarizes claims by employee and category with approval status
- **Complete workflow**: Demonstrates processing, approval, and reporting