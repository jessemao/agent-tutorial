#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

docker image inspect training-wms-classroom:0.7 \
  --format 'Image: {{.Id}} | Architecture: {{.Architecture}} | Created: {{.Created}}'
docker compose -f compose.classroom.yml ps
docker compose -f compose.classroom.yml logs --tail=40 classroom frontend
