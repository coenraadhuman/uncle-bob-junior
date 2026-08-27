# Uncle Bob Junior for Hermes installed

Enable it if you did not install with `--enable`:

```bash
hermes plugins enable uncle-bob-junior
```

Restart Hermes or the gateway after enabling.

In shared gateways, restrict `/uncle-bob-junior` to trusted users with Hermes slash-command access controls; runtime mode is process-local.

Commands:

- `/uncle-bob-junior [lite|full|ultra|off]`
- `/uncle-bob-junior-review [target]`
- `/uncle-bob-junior-audit [target]`
- `/uncle-bob-junior-debt`
- `/uncle-bob-junior-gain`
- `/uncle-bob-junior-help`

Bundled skills are available as `uncle-bob-junior:uncle-bob-junior`, `uncle-bob-junior:uncle-bob-junior-review`, `uncle-bob-junior:uncle-bob-junior-audit`, `uncle-bob-junior:uncle-bob-junior-debt`, `uncle-bob-junior:uncle-bob-junior-gain`, and `uncle-bob-junior:uncle-bob-junior-help`.
