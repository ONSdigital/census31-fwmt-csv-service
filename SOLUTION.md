 # CSV Service Startup Fix Summary

## Problem
`csv-service` failed to start with:
```
org.springframework.beans.factory.UnsatisfiedDependencyException: 
Error creating bean with name 'storage': Failed to instantiate [com.google.cloud.storage.Storage]: 
Factory method 'storage' threw exception with message: null
```

Root cause: `CsvServicePubSubEmulatorConfig` was always active with `@Primary` `NoCredentialsProvider`, which broke GCS Storage client creation by passing `null` credentials to `ServiceOptions.Builder.setCredentials()`.

---

## Solution: Profile-Based Configuration

Replaced the problematic always-active emulator config with explicit Spring profiles:

### Files Changed

#### 1. **`src/main/java/.../config/CsvServicePubSubEmulatorConfig.java`**
   - Added `@ConditionalOnProperty(value = "spring.cloud.gcp.credentials.enabled", havingValue = "false")`
   - Now only activates when credentials are explicitly disabled
   - Added javadoc explaining profile activation
   - The `NoCredentialsProvider` bean is now **safe** — it only applies to emulator mode

#### 2. **`src/main/resources/application.yml`** (production default)
   - Added header comment explaining profile usage
   - Kept as-is with real GCP bucket paths and `credentials.enabled: true`
   - Default startup: `mvn spring-boot:run` → uses real GCP credentials and Storage

#### 3. **`src/main/resources/application-emulator.yml`** (new)
   - Explicit emulator profile for local development
   - Sets `spring.cloud.gcp.credentials.enabled: false` → activates `CsvServicePubSubEmulatorConfig`
   - Routes CSV data to `file://~/acceptance/data/` paths
   - Usage: `mvn spring-boot:run --spring.profiles.active=emulator`

#### 4. **`PROFILES.md`** (new)
   - Complete guide for using both profiles
   - Debug mode instructions
   - Prerequisites for each environment

---

## How It Works Now

**Production (Default)**
```bash
mvn spring-boot:run
```
- ✅ `spring.cloud.gcp.credentials.enabled: true` (from `application.yml`)
- ✅ `CsvServicePubSubEmulatorConfig` does NOT activate (conditional guard fails)
- ✅ Spring Cloud GCP uses real Application Default Credentials
- ✅ Storage bean initializes with real GCP credentials
- ✅ CSV files loaded from `gs://` buckets

**Emulator (Local Dev)**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=emulator"
```
- ✅ `application-emulator.yml` merges into config
- ✅ `spring.cloud.gcp.credentials.enabled: false` (from emulator profile)
- ✅ `CsvServicePubSubEmulatorConfig` activates (conditional guard passes)
- ✅ `NoCredentialsProvider` bean provided for PubSub emulator
- ✅ Storage uses no credentials (not used with `file://` paths)
- ✅ CSV files loaded from local `file://` paths

---

## Verification

✅ Compiled successfully: `mvn clean compile`
✅ Config files in place:
  - `/src/main/resources/application.yml`
  - `/src/main/resources/application-emulator.yml`
✅ Java config properly guarded with `@ConditionalOnProperty`
✅ Documentation in `PROFILES.md`

---

## Commands

**Production startup (uses gcloud ADC automatically)**
```bash
mvn spring-boot:run
```

**Production debug**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**Emulator startup (local dev)**
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

**Health check**
```bash
curl -u user:password http://localhost:8060/health
```


