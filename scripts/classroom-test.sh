#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

task="${1:-}"
scope="${2:-target}"

case "$task:$scope" in
  T01:target)
    test_name='WmsFlowIntegrationTest#cancellingReservedShipmentReleasesInventory'
    ;;
  T02:baseline)
    test_name='T02InboundBaselineIntegrationTest'
    ;;
  T02:target)
    for test_file in \
      training-server/src/test/java/com/acme/training/T02InboundAcceptanceIntegrationTest.java \
      training-server/src/test/java/com/acme/training/T02InboundRollbackIntegrationTest.java; do
      if [[ ! -f "$test_file" ]]; then
        echo "Missing T02 acceptance test: $test_file" >&2
        exit 1
      fi
    done
    test_name='T02InboundAcceptanceIntegrationTest,T02InboundRollbackIntegrationTest'
    ;;
  T01:module|T02:module)
    exec docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl training-server -am test
    ;;
  T01:all|T02:all)
    exec docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify
    ;;
  *)
    echo 'Usage: ./scripts/classroom-test.sh T01|T02 target|baseline|module|all' >&2
    exit 2
    ;;
esac

docker compose -f compose.classroom.yml exec -T classroom \
  /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl training-server -am \
  -Dtest="$test_name" -Dsurefire.failIfNoSpecifiedTests=false test
