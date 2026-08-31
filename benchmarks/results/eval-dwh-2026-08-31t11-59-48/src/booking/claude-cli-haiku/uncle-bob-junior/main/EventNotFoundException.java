class EventNotFoundException extends RuntimeException {
  EventNotFoundException(String eventId) { super("Event not found: " + eventId); }
}
