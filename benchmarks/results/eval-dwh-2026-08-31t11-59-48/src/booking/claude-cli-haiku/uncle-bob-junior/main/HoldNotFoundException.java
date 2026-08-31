class HoldNotFoundException extends RuntimeException {
  HoldNotFoundException(String holdId) { super("Hold not found: " + holdId); }
}
