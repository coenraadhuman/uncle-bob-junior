class HoldExpiredException extends RuntimeException {
  HoldExpiredException(String holdId) { super("Hold expired: " + holdId); }
}
