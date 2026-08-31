// Shared delivery contract for both arms, so neither gets a hidden advantage.
// Headless sessions sometimes act like agents: they try to write files or ask
// clarifying questions instead of answering, which leaves no code to score.
// Language-neutral on purpose: the task names the language, and hardcoding
// one here once made models write Java for the Python and C# tasks.
const DELIVERY_INSTRUCTION =
  'Reply with the complete solution as code in fenced blocks in your message, written in the language the task asks for ' +
  'and tagged with that language (```java, ```python, ```csharp). ' +
  'Do not create or edit files. Do not ask clarifying questions; make reasonable assumptions and state them briefly.';

module.exports = { DELIVERY_INSTRUCTION };
