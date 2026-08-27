// Shared delivery contract for both arms, so neither gets a hidden advantage.
// Headless sessions sometimes act like agents: they try to write files or ask
// clarifying questions instead of answering, which leaves no code to score.
const DELIVERY_INSTRUCTION =
  'Reply with the complete solution as Java code in fenced ```java blocks in your message. ' +
  'Do not create or edit files. Do not ask clarifying questions; make reasonable assumptions and state them briefly.';

module.exports = { DELIVERY_INSTRUCTION };
