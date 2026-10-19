#!/bin/bash

# Fail on any error.
set -e

mkdir -p "${KOKORO_ARTIFACTS_DIR}/output_dir"
cd "${KOKORO_ARTIFACTS_DIR}/output_dir"

GCS_BUCKET="androidx-test-staging/release"
ATTESTATION_FILE="attestation.intoto.jsonl"
INPUT_ZIP="axt_m2repository_unsigned.zip"
OUTPUT_ZIP="axt_m2repository_unsigned_and_attested.zip"

# 1. Find the full GCS path of the zip file
ZIP_GCS_PATH=$(gcloud storage ls "gs://${GCS_BUCKET}/*/*/${CANDIDATE_NAME}/axt_m2repository_unsigned.zip" | head -n 1)

if [[ -z "${ZIP_GCS_PATH}" ]]; then
  echo "Error: Could not find the zip file for candidate ${CANDIDATE_NAME}."
  exit 1
fi

# 2. Extract the parent path: gs://[BUCKET_NAME]/[timestamp]/[build id]
# Removes '/axt_m2repository_unsigned.zip'
DIR_PATH="${ZIP_GCS_PATH%/*}"
# Removes '/[CANDIDATE_NAME]'
DIR_PATH="${DIR_PATH%/*}"

echo "Target build directory identified: ${DIR_PATH}"

# 3. Download and rename the ZIP file to current working directory
echo "Downloading zip file..."
gcloud storage cp "${ZIP_GCS_PATH}" "./${INPUT_ZIP}"

# 4. Download and rename the attestation file
if gcloud storage ls "${DIR_PATH}/${CANDIDATE_NAME}.intoto.jsonl" > /dev/null 2>&1; then
  echo "Downloading attestation..."
  gcloud storage cp "${DIR_PATH}/${CANDIDATE_NAME}.intoto.jsonl" "./${ATTESTATION_FILE}"
else
  echo "Error: Could not find an attestation file in ${DIR_PATH}"
  exit 1
fi

SCRATCH_DIR=$(mktemp -d -t axt_attest_XXXXXX)

echo "Unzipping $INPUT_ZIP to temporary directory..."
unzip -q "$INPUT_ZIP" -d "$SCRATCH_DIR"

echo "Scanning for .pom files..."
find "$SCRATCH_DIR" -type f -name "*.pom" | while IFS= read -r pom_path; do
  pom_dir=$(dirname "$pom_path")
  pom_filename=$(basename "$pom_path")

  # Remove .pom extension to get artifactid-version
  artifact_prefix="${pom_filename%.pom}"

  target_attestation="$pom_dir/$artifact_prefix.intoto.jsonl"

  cp "$ATTESTATION_FILE" "$target_attestation"
  echo "Added attestation: $target_attestation"
done

echo "Re-zipping content to $OUTPUT_ZIP..."
OUTPUT_ZIP_ABS="$(pwd)/$OUTPUT_ZIP"

# Delete output zip if it already exists to avoid appending
rm -f "$OUTPUT_ZIP_ABS"

(cd "$SCRATCH_DIR" && zip -r -q "$OUTPUT_ZIP_ABS" .)

echo "Success! Created $OUTPUT_ZIP"
