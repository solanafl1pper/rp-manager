#!/bin/bash
TOKEN="${1:-mrp_6e4sKErfsc4Tj3nUliTkXpLciZyYpnhHEJ2s1HItyTr3HnBuPqjwx3sxSwdU}"
PROJECT_ID="${2:-resource-pack-presets}"
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Modrinth Publisher for RP Manager v0.0.35"
echo "Target Project: $PROJECT_ID"

upload_version() {
  local name="$1"
  local ver="$2"
  local jar="$3"
  local gv="$4"
  local loaders="$5"

  echo "Uploading $name ($ver)..."
  DATA=$(cat <<EOF
{
  "name": "$name",
  "version_number": "$ver",
  "changelog": "Resource Pack Manager v0.0.35 multi-version release",
  "dependencies": [],
  "game_versions": $gv,
  "version_type": "release",
  "loaders": $loaders,
  "featured": true,
  "status": "listed",
  "requested_status": "listed",
  "project_id": "$PROJECT_ID",
  "file_parts": ["file"]
}
EOF
)

  curl -s -w "\nHTTP: %{http_code}\n" -X POST "https://api.modrinth.com/v2/version" \
    -H "Authorization: $TOKEN" \
    -H "User-Agent: solana/rp-manager/0.0.35 (mikhailkazmerchuk@gmail.com)" \
    -F "data=$DATA;type=application/json" \
    -F "file=@$jar;type=application/java-archive"
}

upload_version "RP Manager v0.0.35 (1.21.x / 26.3)" "0.0.35-1.21.1" "$BASE_DIR/rp-manager-1.21.1/build/libs/rp-manager-fabric-1.21.1-0.0.35.jar" '["1.21","1.21.1","1.21.2","1.21.3","1.21.4","1.21.11","26.3"]' '["fabric","quilt","neoforge"]'
upload_version "RP Manager v0.0.35 (1.20.x)" "0.0.35-1.20.1" "$BASE_DIR/rp-manager-1.20.1/build/libs/rp-manager-fabric-1.20.1-0.0.35.jar" '["1.20","1.20.1","1.20.2","1.20.3","1.20.4","1.20.6"]' '["fabric","quilt","neoforge","forge"]'
upload_version "RP Manager v0.0.35 (1.19.x)" "0.0.35-1.19.2" "$BASE_DIR/rp-manager-1.19.2/build/libs/rp-manager-fabric-1.19.2-0.0.35.jar" '["1.19","1.19.1","1.19.2","1.19.3","1.19.4"]' '["fabric","quilt","forge"]'
upload_version "RP Manager v0.0.35 (1.18.x)" "0.0.35-1.18.2" "$BASE_DIR/rp-manager-1.18.2/build/libs/rp-manager-fabric-1.18.2-0.0.35.jar" '["1.18","1.18.1","1.18.2"]' '["fabric","quilt","forge"]'
upload_version "RP Manager v0.0.35 (1.16.x)" "0.0.35-1.16.5" "$BASE_DIR/rp-manager-1.16.5/build/libs/rp-manager-fabric-1.16.5-0.0.35.jar" '["1.16","1.16.1","1.16.2","1.16.3","1.16.4","1.16.5"]' '["fabric","forge"]'
