# Testing Cloud SQL Autorotation Locally

This is a step-by-step guide for exercising the Cloud SQL managed-rotation
samples --
[`CreateRegionalSecretWithCloudSqlCredentials.java`](src/main/java/secretmanager/regionalsamples/CreateRegionalSecretWithCloudSqlCredentials.java),
[`EnableRegionalSecretManagedRotation.java`](src/main/java/secretmanager/regionalsamples/EnableRegionalSecretManagedRotation.java),
[`RotateRegionalSecret.java`](src/main/java/secretmanager/regionalsamples/RotateRegionalSecret.java),
[`UpdateRegionalSecretWithManagedRotationSchedule.java`](src/main/java/secretmanager/regionalsamples/UpdateRegionalSecretWithManagedRotationSchedule.java),
and
[`GetRegionalSecretType.java`](src/main/java/secretmanager/regionalsamples/GetRegionalSecretType.java)
-- plus the related, non-regional
[`CreateSecretWithType.java`](src/main/java/secretmanager/CreateSecretWithType.java)
and
[`GetSecretType.java`](src/main/java/secretmanager/GetSecretType.java)
-- against a real project, using a mix of `gcloud`, the Cloud Console, and the
samples themselves. It mirrors the Python port's equivalent walkthrough
([`python-docs-samples/secretmanager/Testing.md`](https://github.com/GoogleCloudPlatform/python-docs-samples/blob/main/secretmanager/Testing.md))
step for step where Python has an equivalent, including which steps run the
actual sample code versus `gcloud` -- with one deliberate divergence
matching Go's port: `UpdateRegionalSecretWithManagedRotationSchedule.java`
covers scheduled rotation (scenario 5) with real sample code, a scenario
Python's guide still only covers via `gcloud`.

It covers two passes: first a **sample run**, calling the regional files'
methods directly against a secret that stays alive across the whole flow so
its state is inspectable between steps, then a **test run**, using the
automated tests already written for them in
[`SnippetsIT.java`](src/test/java/secretmanager/regionalsamples/SnippetsIT.java).
The test run's fixtures grant and revoke the Cloud SQL IAM permission for
their own secrets automatically, so they don't depend on the sample run's IAM
grant -- but nothing automates creating the Cloud SQL instance itself, so do
the sample run first anyway: it's the step that actually proves a real
instance exists and is reachable, with state you can inspect between steps.

**Costs money**: this spins up a real Cloud SQL instance. Use a
throwaway/test project, and tear it down with the [Cleanup](#cleanup) step
when you're done.

## Prerequisites

- A GCP project with billing enabled, with the Secret Manager and Cloud SQL
  Admin APIs enabled (step 1 below).
- `GOOGLE_CLOUD_PROJECT` set to that project -- required by
  [`SnippetsIT.java`](src/test/java/secretmanager/regionalsamples/SnippetsIT.java)
  for the test run (see the top-level
  [secretmanager/README.md](README.md#set-environment-variables)).
- Application Default Credentials configured for a principal with Secret
  Manager Admin (`roles/secretmanager.admin`) on the project -- see
  [secretmanager/README.md](README.md#grant-permissions). Run
  `gcloud auth application-default login` if you haven't already.
- A real Cloud SQL instance in the same region you'll use for the regional
  secret, with a database user already created on it. Standing up a Cloud SQL
  instance per run is expensive, so this is meant to be a pre-provisioned,
  long-lived instance -- see [step 2](#2-create-a-cloud-sql-instance-and-database-user)
  below for the exact commands if you don't already have one.

  If you skip this and try to enable rotation anyway,
  `enableRegionalSecretManagedRotation` fails with `PERMISSION_DENIED:
  Permission denied on the Cloud SQL user or instance, or the resource may
  not exist.` -- this single error covers two distinct causes (confirmed by
  reproducing it directly against the API): the instance doesn't exist, or it
  exists but the IAM grant below hasn't been done (or was done for a
  different secret).
- The secret's built-in identity granted Cloud SQL IAM permissions ([step
  4](#4-grant-the-secrets-identity-cloud-sql-permissions-scenario-2) below) --
  this has no SDK snippet, since it's done via gcloud/Resource Manager, not
  the Secret Manager client library.
- Environment variables needed for the **test run** (the sample run uses
  plain shell variables instead, set in step 0 below):
  - `CLOUD_SQL_INSTANCE`: the bare Cloud SQL instance ID (e.g. `my-instance`)
    -- not a connection name. Don't include the project or region: neither
    `PROJECT_ID:INSTANCE_ID` nor `PROJECT_ID:LOCATION_ID:INSTANCE_ID` work,
    since the service already derives the project from the secret's own path
    and would double-prefix a qualified value.
  - `CLOUD_SQL_USER`: the username of the database user on that instance.
- The identity running the **test run** additionally needs
  `resourcemanager.projects.getIamPolicy`/`setIamPolicy` on the project (e.g.
  via `roles/resourcemanager.projectIamAdmin`, or a custom role with just
  those two permissions). `SnippetsIT.java`'s fixtures grant and revoke
  `roles/cloudsql.admin` to each Cloud SQL DB credentials secret's own
  built-in identity, which needs these permissions -- see [Test
  run](#test-run) below for why.

## Sample run

### 0. Set shared variables and build a classpath

```bash
export PROJECT_ID="migrationsource-392805"
export LOCATION_ID="us-east5"          # regional secret + Cloud SQL must match
export INSTANCE_ID="autorotation-test"
export DB_USERNAME="rotation-user"
export SECRET_ID="cloudsql-autorotation-test"

gcloud config set project "$PROJECT_ID"
```

These map onto the test run's environment variables (further down) as:
`GOOGLE_CLOUD_PROJECT=$PROJECT_ID`, `CLOUD_SQL_INSTANCE=$INSTANCE_ID`,
`CLOUD_SQL_USER=$DB_USERNAME`.

`CreateRegionalSecretWithCloudSqlCredentials.java`,
`EnableRegionalSecretManagedRotation.java`, and `RotateRegionalSecret.java`
each have a runnable `main` method, but -- unlike Python's `argparse`-driven
scripts -- it doesn't take command-line arguments; it hardcodes
`TODO(developer)` placeholder values instead. To run one directly, edit its
placeholders and put the compiled classes plus their dependencies on the
classpath:

```bash
cd secretmanager

# Build a classpath file once; reuse it for every step below.
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/sm-classpath.txt

mvn -q compile
```

For each step below, edit the named file's placeholders with `sed`, compile,
run it, then revert with `git checkout --` so the working tree is clean for
the next step (and for the test run).

### 1. Enable the required APIs

```bash
gcloud services enable \
    secretmanager.googleapis.com \
    sqladmin.googleapis.com \
    --project="$PROJECT_ID"
```

No code sample for this -- it's a one-time project setup step.

### 2. Create a Cloud SQL instance and database user

Skip this if you already have a PostgreSQL or SQL Server instance in
`LOCATION_ID` to test against -- this is meant to be a one-time, long-lived
setup, not something you recreate per run. Check first with
`gcloud sql instances list --project="$PROJECT_ID"`.

```bash
gcloud sql instances create "$INSTANCE_ID" \
    --database-version=POSTGRES_15 \
    --region="$LOCATION_ID" \
    --cpu=2 --memory=4GB \
    --root-password="temporary-root-password" \
    --project="$PROJECT_ID"

# Any initial password works -- managed rotation will replace it.
gcloud sql users create "$DB_USERNAME" \
    --instance="$INSTANCE_ID" \
    --password="temporary-initial-password" \
    --project="$PROJECT_ID"
```

**Console check:** Cloud SQL > Instances > `autorotation-test` should show
status "Runnable", region matching `LOCATION_ID`, and a `rotation-user` user
under the "Users" tab.

### 3. Create the secret -- runs `CreateRegionalSecretWithCloudSqlCredentials.java` (scenario 1)

```bash
SAMPLE=src/main/java/secretmanager/regionalsamples/CreateRegionalSecretWithCloudSqlCredentials.java

sed -i \
    -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
    -e "s/String locationId = \"your-location-id\";/String locationId = \"$LOCATION_ID\";/" \
    -e "s/String secretId = \"your-secret-id\";/String secretId = \"$SECRET_ID\";/" \
    "$SAMPLE"

mvn -q compile
java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" \
    secretmanager.regionalsamples.CreateRegionalSecretWithCloudSqlCredentials

git checkout -- "$SAMPLE"
```

Copy the printed `iamPolicyUidPrincipal` value from the "Grant this identity
Cloud SQL IAM permissions..." line -- you'll need it next:

```bash
export SECRET_PRINCIPAL="principal://secretmanager.googleapis.com/projects/.../uid/locations/.../secrets/..."
```

**Console check:** Secret Manager > Regional secrets > `$SECRET_ID`. The
"Overview" tab should show secret type "Cloud SQL DB credentials", 0
versions, and rotation status "Disabled". The "IAM principal identifier"
field on this page is the same value the sample printed.

### 4. Grant the secret's identity Cloud SQL permissions (scenario 2)

```bash
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="$SECRET_PRINCIPAL" \
    --role="roles/cloudsql.admin" \
    --condition=None
```

No code sample for this -- it's an IAM binding via Resource Manager, not a
Secret Manager client library call.

`--condition=None` matters here: if your project already has *any*
conditional IAM bindings, `gcloud` will otherwise prompt you to attach this
new binding to one of them, or write a new one -- and if you accidentally
reuse an unrelated existing condition (e.g. one scoped to Parameter Manager
resources), the role grant silently becomes a no-op and
`enableRegionalSecretManagedRotation` fails with a `PERMISSION_DENIED` that
looks like a Cloud SQL problem but isn't. `--condition=None` skips the prompt
and guarantees this binding is unconditional.

This grant is per-secret, not per-project: `SECRET_PRINCIPAL` is derived from
the secret's own UID, so every new Cloud SQL DB credentials secret needs its
own binding -- a grant made here (for the persistent `$SECRET_ID`) does not
cover any other secret. The test run further down creates its own fresh,
randomly-named secrets and grants (and later revokes) this same role for
each secret's principal automatically, so you don't need to repeat this step
for it -- see [Test run](#test-run) for details.

**Console check:** IAM & Admin > IAM. Filter by principal and confirm the
`principal://secretmanager.googleapis.com/...` row has the Cloud SQL Admin
role. (Least-privilege alternative: a custom role with just
`cloudsql.users.list` and `cloudsql.users.update`.)

### 5. Enable managed rotation -- runs `EnableRegionalSecretManagedRotation.java` (scenario 3, creates version 1)

```bash
SAMPLE=src/main/java/secretmanager/regionalsamples/EnableRegionalSecretManagedRotation.java

sed -i \
    -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
    -e "s/String locationId = \"your-location-id\";/String locationId = \"$LOCATION_ID\";/" \
    -e "s/String secretId = \"your-secret-id\";/String secretId = \"$SECRET_ID\";/" \
    -e "s/String instanceId = \"your-cloud-sql-instance-id\";/String instanceId = \"$INSTANCE_ID\";/" \
    -e "s/String username = \"your-cloud-sql-username\";/String username = \"$DB_USERNAME\";/" \
    "$SAMPLE"

mvn -q compile
java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" \
    secretmanager.regionalsamples.EnableRegionalSecretManagedRotation

git checkout -- "$SAMPLE"
```

`instanceId` is the **bare** Cloud SQL instance ID -- just
`autorotation-test`, not `PROJECT_ID:INSTANCE_ID` and not the full
`PROJECT_ID:LOCATION_ID:INSTANCE_ID` connection name. Both of those fail: the
service already knows the project from the secret's own path and prepends it
internally, so a qualified value ends up double-prefixed (`INVALID_ARGUMENT:
Invalid full instance name`) or simply doesn't resolve (`PERMISSION_DENIED:
...or the resource may not exist`). This contradicts `gcloud secrets
enable-managed-rotation --help`'s own `--instance-id=my-project:my-instance`
example, which is misleading -- confirmed by testing all three forms against
a real instance.

**Console check:** the secret's "Versions" tab now shows version 1,
"Enabled". The Overview tab's rotation status flips to "Enabled".

**Verify the password actually changed:** access the version and try
connecting to Cloud SQL with it.

```bash
gcloud secrets versions access latest \
    --secret="$SECRET_ID" --location="$LOCATION_ID" --project="$PROJECT_ID"

# Use the value above as PGPASSWORD:
PGPASSWORD='<value from above>' psql \
    "host=$(gcloud sql instances describe "$INSTANCE_ID" --format='value(ipAddresses[0].ipAddress)') \
     dbname=postgres user=$DB_USERNAME sslmode=require"
```

`gcloud secrets versions access` isn't affected by the `gcloud`
regional-secrets bug noted in step 8 -- confirmed working directly. `psql`
must be installed to run the connection check.

### 6. Trigger an on-demand rotation -- runs `RotateRegionalSecret.java` (scenario 4)

```bash
SAMPLE=src/main/java/secretmanager/regionalsamples/RotateRegionalSecret.java

sed -i \
    -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
    -e "s/String locationId = \"your-location-id\";/String locationId = \"$LOCATION_ID\";/" \
    -e "s/String secretId = \"your-secret-id\";/String secretId = \"$SECRET_ID\";/" \
    "$SAMPLE"

mvn -q compile
java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" \
    secretmanager.regionalsamples.RotateRegionalSecret

git checkout -- "$SAMPLE"
```

**Console check:** "Versions" tab now shows version 2 as "Enabled" and
version 1 as "Disabled". Re-run the `psql` check above with the new latest
version's value to confirm the live password matches.

### 7. Configure a recurring schedule -- runs `UpdateRegionalSecretWithManagedRotationSchedule.java` (scenario 5)

Unlike Python (which still has no sample for this and uses `gcloud`
directly), this Java port -- like Go -- has a dedicated sample:
`updateRegionalSecretWithManagedRotationSchedule` sets
`rotation.next_rotation_time`/`rotation.rotation_period` via `updateSecret`
with a field mask covering just those two subfields.
`UpdateRegionalSecret.java` is a separate, pre-existing sample that only
demonstrates updating labels and doesn't touch rotation. Masking the whole
`rotation` submessage instead of just those two subfields fails with `Field
'rotation.managed_rotation_status' is immutable and cannot be updated`
(`managed_rotation_status` is output-only). This only works on a secret that
already has Cloud SQL managed rotation enabled (step 5) -- calling it before
that, or on a secret that isn't the `CLOUD_SQL_DB_CREDENTIALS` type, fails.

```bash
SAMPLE=src/main/java/secretmanager/regionalsamples/UpdateRegionalSecretWithManagedRotationSchedule.java

sed -i \
    -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
    -e "s/String locationId = \"your-location-id\";/String locationId = \"$LOCATION_ID\";/" \
    -e "s/String secretId = \"your-secret-id\";/String secretId = \"$SECRET_ID\";/" \
    "$SAMPLE"

mvn -q compile
java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" \
    secretmanager.regionalsamples.UpdateRegionalSecretWithManagedRotationSchedule

git checkout -- "$SAMPLE"
```

The file's default rotation period is 86400 seconds (24h) -- edit the
`rotationPeriodSeconds` literal directly (in addition to the `sed` above) if
you want a different period for a real test. The service requires it to be at
least 3600 (1 hour) and
the derived `next_rotation_time` to be at least 300s (5 minutes) in the
future -- both enforced server-side, not checked by the sample. For a real
test you're mainly confirming the schedule is accepted rather than waiting a
full period to elapse; pass a small period (e.g. `600` for 10 minutes) if you
want to actually observe a rotation fire and check for version 3.

This step runs the Java sample directly rather than `gcloud secrets update
--location=...`, so it isn't affected by the `gcloud`/regional-secrets bug
described in step 8.

**Console check:** Overview tab shows the configured rotation period and
next rotation time. After it fires, "Versions" gains a new entry and
`next_rotation_time` advances by one period.

### 8. Inspect state directly (scenario 6)

`GetRegionalSecretType.java` prints the secret's type directly (`Found
regional secret ... with secret type ...`) -- run it the same way as the
steps above:

```bash
SAMPLE=src/main/java/secretmanager/regionalsamples/GetRegionalSecretType.java

sed -i \
    -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
    -e "s/String locationId = \"your-location-id\";/String locationId = \"$LOCATION_ID\";/" \
    -e "s/String secretId = \"your-secret-id\";/String secretId = \"$SECRET_ID\";/" \
    "$SAMPLE"

mvn -q compile
java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" \
    secretmanager.regionalsamples.GetRegionalSecretType

git checkout -- "$SAMPLE"
```

`GetRegionalSecret.java` and `ListRegionalSecretVersions.java` can also fetch
the rest of this state (they return the full API response, including
`rotation` and `policyMember`/each version's `state` -- they just don't print
those fields, only the resource name), but this step uses `gcloud` directly
to get formatted output for those, matching Python's guide (which offers the
same choice between its own
`get_regional_secret.py`/`list_regional_secret_versions.py` scripts and
plain `gcloud`).

```bash
gcloud secrets describe "$SECRET_ID" --location="$LOCATION_ID" --project="$PROJECT_ID" \
    --format="yaml(secretType,rotation,policyMember)"
gcloud secrets versions list "$SECRET_ID" --location="$LOCATION_ID" --project="$PROJECT_ID"
```

**Known `gcloud` CLI issue with regional secrets:** on at least gcloud CLI
582.0.0, every regional secret command (`create`, `describe`,
`enable-managed-rotation`, `rotate-secret`, `versions list`, `update`,
`delete` -- anything with `--location=`) mis-builds the resource path as
`projects/P/locations/L/locations/L/...` (doubled) and fails with
`INVALID_ARGUMENT` or a raw 404. This is a client-side `gcloud` bug, not a
permissions or product issue -- the underlying REST API works correctly,
confirmed by hitting it directly with `curl`. If you hit this, try `gcloud
components update` first; if it persists, use direct REST calls instead,
e.g. for this step:

```bash
curl -s -H "Authorization: Bearer $(gcloud auth print-access-token)" \
    "https://secretmanager.$LOCATION_ID.rep.googleapis.com/v1/projects/$PROJECT_ID/locations/$LOCATION_ID/secrets/$SECRET_ID"
```

In practice this only affects the steps above that have no code sample
(inspecting state, the recurring schedule, cleanup) -- steps 3, 5, and 6 run
the actual ported sample code instead of `gcloud secrets`, which sidesteps
this bug entirely.

### 9. Pub/Sub rotation notifications (scenario 7, optional)

```bash
gcloud pubsub topics create cloudsql-rotation-notify --project="$PROJECT_ID"
gcloud pubsub subscriptions create cloudsql-rotation-notify-sub \
    --topic=cloudsql-rotation-notify --project="$PROJECT_ID"

gcloud secrets update "$SECRET_ID" --location="$LOCATION_ID" \
    --add-topics="projects/$PROJECT_ID/topics/cloudsql-rotation-notify"
```

After the next rotation (step 6 or 7), pull the subscription and confirm a
`SECRET_ROTATE` event arrives:

```bash
gcloud pubsub subscriptions pull cloudsql-rotation-notify-sub \
    --auto-ack --project="$PROJECT_ID"
```

`ConsumeEventNotification.java`
(`src/main/java/secretmanager/ConsumeEventNotification.java`) demonstrates
parsing this message's `eventType`/`secretId` attributes and payload; it's
not itself deployable from this guide (it's meant to back a Cloud
Functions/Cloud Run push endpoint), so this step just confirms the
notification arrives.

### Cleanup

```bash
gcloud secrets delete "$SECRET_ID" --location="$LOCATION_ID" --quiet
gcloud pubsub subscriptions delete cloudsql-rotation-notify-sub --quiet
gcloud pubsub topics delete cloudsql-rotation-notify --quiet
gcloud sql instances patch "$INSTANCE_ID" --no-deletion-protection --quiet
gcloud sql instances delete "$INSTANCE_ID" --quiet
```

## Test run

Once a Cloud SQL instance and database user exist (steps 0-2 above), run just
the new tests in
[`SnippetsIT.java`](src/test/java/secretmanager/regionalsamples/SnippetsIT.java):

```bash
cd secretmanager
export GOOGLE_CLOUD_PROJECT="$PROJECT_ID"
export CLOUD_SQL_INSTANCE="$INSTANCE_ID"
export CLOUD_SQL_USER="$DB_USERNAME"

mvn test -Dtest=secretmanager.regionalsamples.SnippetsIT#testCreateRegionalSecretWithCloudSqlCredentials+testEnableRegionalSecretManagedRotation+testRotateRegionalSecret+testUpdateRegionalSecretWithManagedRotationSchedule+testGetRegionalSecretType
```

The non-regional `secretmanager.SnippetsIT#testCreateSecretWithType` and
`#testGetSecretType` don't need a Cloud SQL instance -- they only exercise
`CreateSecretWithType.java`/`GetSecretType.java` against a plain secret, so
they can run with just `GOOGLE_CLOUD_PROJECT` set:

```bash
mvn test -Dtest=secretmanager.SnippetsIT#testCreateSecretWithType+testGetSecretType
```

The fully-qualified class name is required: `secretmanager` also has a
non-regional `SnippetsIT` (`src/test/java/secretmanager/SnippetsIT.java`)
with the same simple name, so a bare `-Dtest=SnippetsIT` is ambiguous.

(Drop the `-Dtest=...` filter -- but keep the fully-qualified
`-Dtest=secretmanager.regionalsamples.SnippetsIT` -- to run the full
regional `SnippetsIT` suite, including the pre-existing non-rotation tests.)

`testEnableRegionalSecretManagedRotation` and `testRotateRegionalSecret`
each need their own Cloud SQL DB credentials secret granted
`roles/cloudsql.admin` on its own built-in identity before they can pass --
the Cloud SQL IAM grant is per-secret with no wildcard/project-wide
mechanism that covers a secret created at test time, so a bare `@BeforeClass`
that creates a fresh secret and calls `enableManagedRotation` on it
immediately fails with `PermissionDenied`, confirmed empirically.
`SnippetsIT.beforeAll` fixes this the same way the Python and Go ports
already do: for each Cloud SQL DB credentials secret it creates for these two
tests, it reads the secret's `policyMember.iamPolicyUidPrincipal`, does a
`GetIamPolicy`/`SetIamPolicy` read-modify-write against the *project's* IAM
policy to add `roles/cloudsql.admin` for that principal (retrying on
`AbortedException`, since `SetIamPolicy` replaces the whole policy and can
race another writer's etag), waits 10 seconds for the grant to propagate,
then revokes it the same way in `afterAll`. This is why the test run's
prerequisites above call out `resourcemanager.projects.getIamPolicy`/
`setIamPolicy` specifically -- it's a permission this rotation test pattern
needs beyond ordinary Secret Manager/Cloud SQL access.

This fix mirrors `regional_secret_with_cloud_sql_credentials` in Python's
`snippets_test.py` and `testRegionalSecretWithCloudSQLCredentials` in Go's
`regional_secretmanager_test.go` -- if you improve this pattern further,
backport the improvement to all three.

After a real run, confirm teardown actually happened cleanly:

```bash
gcloud projects get-iam-policy "$PROJECT_ID" --format=json > /tmp/iam-after.json
# Should show no leftover roles/cloudsql.admin bindings for
# principal://secretmanager.googleapis.com/... members from this test run.
```

## Report / open gaps

- **Scheduled rotation** (scenario 5): covered by
  `UpdateRegionalSecretWithManagedRotationSchedule.java` (step 7 above),
  matching Go's port. `UpdateRegionalSecret.java` remains a separate,
  pre-existing sample that only demonstrates updating labels and doesn't
  touch rotation.
- **Get Secret Type** (regional and global): covered by
  `GetRegionalSecretType.java` (step 8 above) and the non-regional
  `GetSecretType.java`.
- **Create a secret with the Access Key, Certificate, Other DB Credential, or
  Other type** (global, non-Cloud-SQL initiative): covered by
  `CreateSecretWithType.java`, which takes a `Secret.SecretType` parameter
  (`ACCESS_KEY`, `CERTIFICATE`, `OTHER_DB_CREDENTIALS`, or `OTHER` --
  `CLOUD_SQL_DB_CREDENTIALS` is intentionally excluded from this sample's
  intended use, since that type additionally requires a regional secret and
  goes through `enableManagedRotation` instead). Unlike
  `CreateRegionalSecretWithCloudSqlCredentials.java`, these other types are
  plain metadata tags -- no additional credentials payload is required at
  creation time. This and `GetSecretType.java` aren't part of the Cloud SQL
  managed-rotation walkthrough above (they're a separate, non-Cloud-SQL
  "Secret Type" initiative tracked in the same sheet); exercise them
  directly, e.g.:

  ```bash
  cd secretmanager
  SAMPLE=src/main/java/secretmanager/CreateSecretWithType.java
  sed -i \
      -e "s/String projectId = \"your-project-id\";/String projectId = \"$PROJECT_ID\";/" \
      -e "s/String secretId = \"your-secret-id\";/String secretId = \"secret-type-test\";/" \
      "$SAMPLE"
  mvn -q compile
  java -cp "target/classes:$(cat /tmp/sm-classpath.txt)" secretmanager.CreateSecretWithType
  git checkout -- "$SAMPLE"

  gcloud secrets delete secret-type-test --quiet
  ```
- **Inspecting a secret's rotation config** (scenario 6): `GetRegionalSecret.java`
  returns the full `Secret` proto (including `getRotation()`/`getPolicyMember()`),
  and `ListRegionalSecretVersions.java` returns the full paged version list
  (including each version's `getState()`) -- both cover the relevant fields,
  though neither sample prints them.
- **Pub/Sub rotation notifications** (scenario 7): `ConsumeEventNotification.java`
  already exists and parses the same `eventType`/`secretId` attributes Python's
  `consume_event_notification.py` does -- this is a pre-existing, generic
  (non-regional-specific) sample, confirmed to cover this reuse case.
- **Granting the secret's built-in identity Cloud SQL IAM permissions**: no
  SDK snippet by design -- it's a `gcloud`/Resource Manager step only (step 4
  above), matching the tracking sheet's scope.
