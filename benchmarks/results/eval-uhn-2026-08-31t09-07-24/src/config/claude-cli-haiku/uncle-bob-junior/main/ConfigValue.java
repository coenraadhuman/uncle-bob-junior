// Assumptions:
// - Duration units: s, m, h (seconds, minutes, hours)
// - Boolean values: true/false (case-insensitive)
// - Comments: # to end of line
// - Section and key names are case-sensitive
// - Each key can appear once per section; duplicates overwrite silently

sealed interface ConfigValue permits IntValue, BooleanValue, DurationValue {}
