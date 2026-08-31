class WaitlistEntry {
  final String customerId;
  final int quantity;
  final TicketTier tier;
  
  WaitlistEntry(String customerId, int quantity, TicketTier tier) {
    this.customerId = customerId;
    this.quantity = quantity;
    this.tier = tier;
  }
}
