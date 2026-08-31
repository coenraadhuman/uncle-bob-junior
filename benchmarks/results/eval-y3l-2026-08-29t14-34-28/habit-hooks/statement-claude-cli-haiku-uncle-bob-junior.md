── unused-import (17 issues) ──

An import nothing in the module uses is noise the reader has to disprove: it makes the module look like it depends on something it does not, and it hides real signals — a leftover from code you deleted, a symbol you meant to call and forgot, or a re-export that belongs somewhere explicit.

Delete it. Don't comment it out or alias it to silence the warning — that keeps the lie. Two cases need more than deletion: if the name was imported purely for a side effect at import time, that side-effect-on-import is the smell — make the effect an explicit call. If the module is a package's public surface deliberately re-exporting the name, say so where the language makes re-exports explicit (an `__all__` entry, a barrel export) rather than leaning on an unused import to hold it.

Done right, the import list names exactly what the code below it uses — nothing to prove, nothing to explain.

BankStatementAnalyzer.java:4
BankStatementAnalyzerTest.java:1
BankStatementAnalyzerTest.java:2
BankStatementAnalyzerTest.java:3
BankStatementAnalyzerTest.java:4
Categorizer.java:1
Categorizer.java:2
Categorizer.java:4
CurrencyConverter.java:1
CurrencyConverter.java:2
CurrencyConverter.java:3
CurrencyConverter.java:4
MonthlyReport.java:1
MonthlyReport.java:4
Transaction.java:1
Transaction.java:3
Transaction.java:4
