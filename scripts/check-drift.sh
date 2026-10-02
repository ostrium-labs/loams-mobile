#!/usr/bin/env bash
# CI: the vendored protos match conformance/proto-ref.lock, and the committed
# generated code matches what buf generates from them.
set -euo pipefail
cd "$(dirname "$0")/.."
want=$(sed -n 's/^tree_sha256=//p' conformance/proto-ref.lock)
have=$(scripts/proto-hash.sh)
if [[ "$want" != "$have" ]]; then
  echo "proto/ changed without updating conformance/proto-ref.lock (lock $want, tree $have)." >&2
  echo "Re-vendor with scripts/sync-protos.sh, or run scripts/update-lock.sh for placeholder edits." >&2
  exit 1
fi
scripts/generate.sh
paths=(android/proto/src/generated ios/Packages/LoamsProto/Sources/LoamsProto/Generated mock/gen)
if ! git diff --exit-code --stat -- "${paths[@]}" || [[ -n "$(git status --porcelain -- "${paths[@]}")" ]]; then
  git status --porcelain -- "${paths[@]}" >&2
  echo "Generated code is stale: run scripts/generate.sh and commit the result." >&2
  exit 1
fi
echo "protos and generated code are in sync"
