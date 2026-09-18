#!/usr/bin/env bash
set -euo pipefail

repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_dir"

scope="${2:-baseline}"
if [[ "${1:-}" != "TASK1" ]]; then
  echo 'Usage: ./scripts/classroom-test.sh TASK1 baseline|module|all|mysql' >&2
  exit 2
fi

case "$scope" in
  baseline)
    ./scripts/validate-ai-governance.sh baseline
    ./scripts/validate-v2-task1.sh baseline
    test_name='InventoryRegressionTest,WmsFlowIntegrationTest,T03TransferHappyPathIntegrationTest,T03TransferContractIntegrationTest,T03TransferAtomicityIntegrationTest,InventoryConcurrencyTest'
    ;;
  module)
    exec docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl training-server -am test
    ;;
  all)
    exec docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp clean verify
    ;;
  mysql)
    mysql_count_test='training-server/src/test/java/com/acme/training/InventoryCountContractIntegrationTest.java'
    if [[ ! -s "$mysql_count_test" ]]; then
      echo "[BLOCK] MySQL task-one verification test is missing: $mysql_count_test" >&2
      exit 1
    fi
    exec docker compose -f compose.classroom.yml exec -T classroom \
      /workspace/docker/classroom/training-wms-mvn -o -B -ntp -Pmysql-verification \
      -Dspring.profiles.active=mysql-verification -pl training-server -am \
      -Dtest='MysqlVerificationEnvironmentTest,T02InboundMysqlConcurrencyIntegrationTest,InventoryCountContractIntegrationTest' \
      -Dsurefire.failIfNoSpecifiedTests=false test
    ;;
  *)
    echo 'Usage: ./scripts/classroom-test.sh TASK1 baseline|module|all|mysql' >&2
    exit 2
    ;;
esac

docker compose -f compose.classroom.yml exec -T classroom \
  /workspace/docker/classroom/training-wms-mvn -o -B -ntp -pl training-server -am \
  -Dtest="$test_name" -Dsurefire.failIfNoSpecifiedTests=false test
