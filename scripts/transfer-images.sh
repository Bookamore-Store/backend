#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="frr4cflvuvlf"
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
WORKSPACE_ROOT="$(cd -- "$SCRIPT_DIR/../.." && pwd -P)"
SOURCE_DIR="$WORKSPACE_ROOT/gitops/data/uploads/img"
PROFILE="${OCI_CLI_PROFILE:-DEFAULT}"
OCI_CONFIG_FILE="${OCI_CLI_CONFIG_FILE:-$HOME/.oci/config}"
TARGET=""
DRY_RUN=false
OVERWRITE=false
AUTO_CONFIRM=false

usage() {
    cat <<'EOF'
Upload local Bookamore images to an OCI Object Storage bucket.

Usage:
  transfer-images.sh --target dev|prod [options]

Options:
  --source-dir DIR  Image directory containing book/ and offer/ (default: gitops/data/uploads/img)
  --dry-run         Validate and list uploads without contacting OCI
  --overwrite       Replace existing objects (default: keep existing objects)
  --yes             Skip the interactive bucket-name confirmation
  -h, --help        Show this help

OCI authentication uses OCI_CLI_CONFIG_FILE (default: ~/.oci/config)
and OCI_CLI_PROFILE (default: DEFAULT).
EOF
}

fail() {
    printf 'ERROR: %s\n' "$*" >&2
    exit 1
}

while (($#)); do
    case "$1" in
        --target)
            (($# >= 2)) || fail "--target requires dev or prod"
            TARGET="$2"
            shift 2
            ;;
        --source-dir)
            (($# >= 2)) || fail "--source-dir requires a directory"
            SOURCE_DIR="$2"
            shift 2
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --overwrite)
            OVERWRITE=true
            shift
            ;;
        --yes)
            AUTO_CONFIRM=true
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            fail "Unknown argument: $1 (see --help)"
            ;;
    esac
done

case "$TARGET" in
    dev)
        BUCKET="bookamore-dev"
        ;;
    prod)
        BUCKET="bookamore"
        ;;
    *)
        fail "--target dev or --target prod is required"
        ;;
esac

[[ -d "$SOURCE_DIR" ]] || fail "Image source directory does not exist: $SOURCE_DIR"
SOURCE_DIR="$(cd -- "$SOURCE_DIR" && pwd -P)"

for required_dir in book offer; do
    [[ -d "$SOURCE_DIR/$required_dir" ]] || fail "Required source directory is missing: $SOURCE_DIR/$required_dir"
done

while IFS= read -r -d '' path; do
    relative_path="${path#"$SOURCE_DIR"/}"
    case "$relative_path" in
        book/*|offer/*) ;;
        *) fail "Unexpected file outside book/ or offer/: $relative_path" ;;
    esac

    [[ "$relative_path" != */*/* ]] || fail "Nested image directories are not supported: $relative_path"
    case "${relative_path##*.}" in
        jpg|jpeg|png|webp|JPG|JPEG|PNG|WEBP) ;;
        *) [[ "${relative_path##*/}" == ".gitkeep" ]] || fail "Unsupported image file type: $relative_path" ;;
    esac
done < <(find "$SOURCE_DIR" -mindepth 1 -type f -print0)

if find "$SOURCE_DIR" -mindepth 1 -type l -print -quit | grep -q .; then
    fail "Symbolic links are not allowed in the image source directory"
fi

mapfile -d '' IMAGE_FILES < <(
    find "$SOURCE_DIR" -mindepth 2 -maxdepth 2 -type f ! -name '.gitkeep' -print0 | sort -z
)
((${#IMAGE_FILES[@]} > 0)) || fail "No image files found in $SOURCE_DIR"

for kind in book offer; do
    found=false
    for path in "${IMAGE_FILES[@]}"; do
        relative_path="${path#"$SOURCE_DIR"/}"
        if [[ "$relative_path" == "$kind/"* ]]; then
            found=true
            break
        fi
    done
    [[ "$found" == true ]] || fail "No image files found in $SOURCE_DIR/$kind"
done

PUBLIC_BUCKET_URL="https://frr4cflvuvlf.objectstorage.eu-frankfurt-1.oci.customer-oci.com/n/${NAMESPACE}/b/${BUCKET}/o"
printf 'Target bucket: %s\n' "$BUCKET"
printf 'Public URL:    %s\n' "$PUBLIC_BUCKET_URL"
printf 'Source:        %s\n' "$SOURCE_DIR"
printf 'Image files:   %s\n' "${#IMAGE_FILES[@]}"
printf 'Object keys will start with book/ or offer/ (not img/).\n'
if [[ "$OVERWRITE" == true ]]; then
    printf 'Existing objects: WILL be overwritten.\n'
else
    printf 'Existing objects: preserved (no overwrite).\n'
fi

if [[ "$DRY_RUN" == true ]]; then
    printf '\nPlanned object keys:\n'
    for path in "${IMAGE_FILES[@]}"; do
        printf '  %s\n' "${path#"$SOURCE_DIR"/}"
    done
    exit 0
fi

command -v oci >/dev/null 2>&1 || fail "OCI CLI is required (install/configure it with `oci setup config`)"
command -v curl >/dev/null 2>&1 || fail "curl is required for public bucket verification"
command -v jq >/dev/null 2>&1 || fail "jq is required for public bucket verification"
[[ -r "$OCI_CONFIG_FILE" ]] || fail "OCI CLI config is missing or unreadable: $OCI_CONFIG_FILE (run 'oci setup config' or set OCI_CLI_CONFIG_FILE)"

printf 'Checking OCI profile "%s"...\n' "$PROFILE"
if ! configured_namespace="$(oci --config-file "$OCI_CONFIG_FILE" --profile "$PROFILE" os ns get \
    --auth api_key --query data --raw-output 2>&1)"; then
    printf '%s\n' "$configured_namespace" >&2
    fail "OCI authentication preflight failed; no images were uploaded"
fi
[[ "$configured_namespace" == "$NAMESPACE" ]] ||
    fail "Configured OCI namespace '$configured_namespace' does not match expected namespace '$NAMESPACE'; no images were uploaded"

if [[ "$AUTO_CONFIRM" != true ]]; then
    printf '\nType the bucket name to confirm upload: '
    read -r confirmation
    [[ "$confirmation" == "$BUCKET" ]] || fail "Bucket name did not match; upload cancelled"
fi

for path in "${IMAGE_FILES[@]}"; do
    relative_path="${path#"$SOURCE_DIR"/}"
    extension="${relative_path##*.}"
    case "${extension,,}" in
        jpg|jpeg) content_type="image/jpeg" ;;
        png) content_type="image/png" ;;
        webp) content_type="image/webp" ;;
        *) fail "Unsupported image file type: $relative_path" ;;
    esac

    printf 'Uploading %s\n' "$relative_path"
    put_args=()
    if [[ "$OVERWRITE" == true ]]; then
        put_args+=(--force)
    else
        put_args+=(--no-overwrite)
    fi

    oci --config-file "$OCI_CONFIG_FILE" --profile "$PROFILE" os object put \
        --auth api_key \
        --namespace-name "$NAMESPACE" \
        --bucket-name "$BUCKET" \
        --name "$relative_path" \
        --file "$path" \
        --content-type "$content_type" \
        --verify-checksum \
        "${put_args[@]}" \
        --output table >/dev/null
done

printf '\nChecking public bucket listing...\n'
listing="$(curl --fail --silent --show-error "${PUBLIC_BUCKET_URL}?limit=5")"
if ! jq -e '.objects | length > 0' >/dev/null <<<"$listing"; then
    printf '%s\n' "$listing" >&2
    fail "Public bucket listing is empty or has an unexpected response"
fi
jq -r '.objects[:5][] | "  \(.name)"' <<<"$listing"

sample_relative_path="${IMAGE_FILES[0]#"$SOURCE_DIR"/}"
sample_url="${PUBLIC_BUCKET_URL}/${sample_relative_path}"
printf '\nChecking public image: %s\n' "$sample_relative_path"
response="$(curl --fail --silent --show-error --head --output /dev/null \
    --write-out '%{http_code} %{content_type}' "$sample_url")"
read -r http_code content_type <<<"$response"
[[ "$http_code" == "200" ]] || fail "Sample image returned HTTP $http_code: $sample_url"
case "$content_type" in
    image/*) ;;
    *) fail "Sample object has unexpected Content-Type '$content_type': $sample_url" ;;
esac
printf 'Verified: HTTP %s, %s\n' "$http_code" "$content_type"
