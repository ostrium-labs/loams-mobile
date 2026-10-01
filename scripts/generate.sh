#!/usr/bin/env bash
# Regenerates every client from proto/ with buf (remote plugins on buf.build):
#   android/proto/src/generated               protobuf-javalite + connect-kotlin
#   ios/Packages/LoamsProto/.../Generated     swift-protobuf + connect-swift
#   mock/gen                                  protobuf-go + connect-go
# The output is committed; CI reruns this and fails on any difference.
set -euo pipefail
cd "$(dirname "$0")/.."
buf lint
for t in kotlin swift go; do
  buf generate --template "buf.gen.$t.yaml"
done
echo "generated from proto/ (tree $(scripts/proto-hash.sh))"
