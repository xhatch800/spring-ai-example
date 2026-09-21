# Ticket Triage Service

A Spring Boot REST application that uses Spring AI to call Anthropic's Claude
to triage trouble tickets: given a title, description, and impact statement,
it returns a severity, priority, and recommended next steps.

## Requirements

- Java 21
- An Anthropic API key

## Running

Set your Anthropic API key as an environment variable, then start the app:

```bash
export ANTHROPIC_API_KEY=sk-ant-...
./gradlew bootRun
```

## Example request

```bash
curl -s -X POST http://localhost:8080/ticket \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Checkout service returning 500s",
        "description": "All checkout requests have failed with HTTP 500 for the last 10 minutes after the latest deploy.",
        "impact": "Customers cannot complete purchases. Revenue-impacting, affects all regions."
      }'
```

Example response:

```json
{
  "severity": "CRITICAL",
  "priority": "P1",
  "nextSteps": [
    "Roll back the latest deploy",
    "Page the on-call engineer",
    "Open an incident channel"
  ]
}
```

## Testing

```bash
./gradlew test
```
