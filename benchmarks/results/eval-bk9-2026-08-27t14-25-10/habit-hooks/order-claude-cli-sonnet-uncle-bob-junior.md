── incomplete-run (1 issue) ──

⚠️ Habit Hooks: this run did not complete — a tool broke, so a clean result cannot be trusted.

habit-sensors: sensor 'pmd' failed: ${python} ${dir}/pmd_sensor.py ${args} -- ${files}
InvalidLineItemException.java: ParseException: Parse exception in file 'InvalidLineItemException.java' at line 9, column 1: Encountered "import".
Was expecting one of:
    <EOF> 
    ";" ...
LineItem.java: ParseException: Parse exception in file 'LineItem.java' at line 33, column 1: Encountered "import".
Was expecting one of:
    <EOF> 
    ";" ...
OrderCalculator.java: ParseException: Parse exception in file 'OrderCalculator.java' at line 44, column 1: Encountered "import".
Was expecting one of:
    <EOF> 
    ";" ...
OrderTotals.java: ParseException: Parse exception in file 'OrderTotals.java' at line 5, column 1: Encountered "import".
Was expecting one of:
    <EOF> 
    ";" ...
ReceiptFormatter.java: ParseException: Parse exception in file 'ReceiptFormatter.java' at line 45, column 1: Encountered "import".
Was expecting one of:
    <EOF> 
    ";" ...
Fix the broken tool and re-run; do not treat this change as checked.
