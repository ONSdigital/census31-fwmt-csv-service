# CSV Service - Profile Configuration

## Profiles

### Default (Production) - `application.yml`

**Usage:**
```bash
mvn spring-boot:run
```

**Configuration:**
- ✅ Real GCP credentials (Application Default Credentials or `GOOGLE_APPLICATION_CREDENTIALS`)
- ✅ Real GCS buckets (`gs://c31-sandbox-simon-diaz-dev-fwmt-csv-data/...`)
- ✅ Real PubSub service (or emulator if `PUBSUB_EMULATOR_HOST` env var is set)
- ✅ Requires valid GCP authentication

**Prerequisites (Pick One):**

**Option A: Use gcloud Application Default Credentials (Recommended)**
```bash
# Verify you have gcloud auth already configured
gcloud auth list

# ADC is automatically used by Spring Cloud GCP
# No env var needed!
mvn spring-boot:run
```

**Option B: Use explicit service account key**
```bash
# If you have a service account key JSON file
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/sa-key.json
mvn spring-boot:run
```

**Option C: Set up ADC if not already done**
```bash
gcloud auth application-default login
# Then just run:
mvn spring-boot:run
```

---

### Emulator Profile - `application-emulator.yml`

**Usage:**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=emulator"
```

**Configuration:**
- ✅ Disables GCP credentials (`spring.cloud.gcp.credentials.enabled=false`)
- ✅ Uses `NoCredentialsProvider` for PubSub emulator
- ✅ Local file paths (`file://~/acceptance/data/...`) instead of GCS buckets
- ✅ PubSub emulator on `localhost:8085`
- ✅ Test mode enabled (`app.testing=true`)
- ✅ Debug logging for GCP components

**Prerequisites:**
Ensure PubSub emulator is running:
```bash
# If using Docker
docker run -p 8085:8085 google/cloud-cli gcloud beta emulators pubsub start --host-port=0.0.0.0:8085

# Or set the env var if using gcloud emulator
export PUBSUB_EMULATOR_HOST=localhost:8085
```

---

## Debug Mode

Add debugging to either profile:

```bash
# Production
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

# Emulator
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=emulator" \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

Then attach your IDE debugger to `localhost:5005`.

---

## Technical Notes

### Why Two Profiles?

The `CsvServicePubSubEmulatorConfig` Java bean provides `NoCredentialsProvider` **only when GCP credentials are disabled**. This design prevents credential conflicts:

- **Production:** Credentials enabled → Spring Cloud GCP uses real ADC → `CsvServicePubSubEmulatorConfig` does NOT activate
- **Emulator:** Credentials disabled → `CsvServicePubSubEmulatorConfig` activates → `NoCredentialsProvider` provides dummy credentials for both PubSub and Storage emulators

### Configuration Precedence

1. `application.yml` (default)
2. `application-{profile}.yml` (merged/override)
3. Environment variables (highest precedence)

So with `--spring.profiles.active=emulator`, the `application-emulator.yml` overrides matching keys from `application.yml`.

---

## Health Check

```bash
curl -u user:password http://localhost:8060/health
```

Expected response (both profiles):
```json
{
  "status": "UP",
  "components": {
    "diskSpace": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "pubSub": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

## Quick Start Commands

**Production (uses gcloud ADC automatically)**
```bash
mvn spring-boot:run
```

**Production debug**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**Emulator (local dev)**
```bash
export PUBSUB_EMULATOR_HOST=localhost:8085
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=emulator"
```

**Emulator debug**
```bash
export PUBSUB_EMULATOR_HOST=localhost:8085
mvn spring-boot:run \
  -Dspring-boot.run.arguments="--spring.profiles.active=emulator" \
  -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

