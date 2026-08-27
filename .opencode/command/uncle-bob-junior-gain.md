---
description: Show the measured with/without benchmark scoreboard
---

Show the uncle-bob-junior gain scoreboard. One shot, change nothing: do not switch mode, write flag files, or persist anything. Results come from the promptfoo benchmark: find the newest eval with `npx promptfoo@latest list evals -n 1`, export it with `npx promptfoo@latest export eval <id> -o -`, and render a per-arm scoreboard: gates passed (functions under 20 lines, flat control flow, ships tests, correct), smell penalty scores (magic numbers, mutable fields, setters), and the weighted score per arm. If no eval exists, say so and point at `npx promptfoo@latest eval -c benchmarks/promptfooconfig.yaml` (needs a logged-in claude CLI, no API key). NEVER invent a figure or print a per-repo savings number: for real per-repo figures point to /uncle-bob-junior-debt (counted ledger) and /uncle-bob-junior-audit (counted smells). Report only.
