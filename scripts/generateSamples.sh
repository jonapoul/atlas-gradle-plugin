#!/bin/sh
set -eu

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit 1

# Kill gradle daemons - they cause the generation to stay at IDLE sometimes
./gradlew --stop

# The generated samples are committed and diffed in CI, and D2's output changes between
# releases, so generation runs inside the docker image where the D2 and Graphviz versions are
# pinned - not against whatever happens to be installed on this machine.
COMPOSE="docker compose -f docker/docker-compose.yml"

# Gradle runs as root inside the container, so everything it writes into the bind-mounted repo
# and gradle home comes back root-owned. Hand it back to whoever called the script, pass or fail.
# --from=0 keeps this to the files docker actually created, rather than rewriting a 30GB cache.
restore_ownership() {
  $COMPOSE run --rm --entrypoint sh samples \
    -c "chown -R --from=0 $(id -u):$(id -g) /atlas /home/gradle/.gradle"
}
trap restore_ownership EXIT

if [ -n "${ATLAS_IMAGE_TAG:-}" ]; then
  # The caller supplied an image, as CI does after building it in an earlier job.
  echo "Using prebuilt image tag '$ATLAS_IMAGE_TAG'"
else
  ATLAS_IMAGE_TAG=local
  export ATLAS_IMAGE_TAG
  # The d2 install calls the GitHub API, which rate limits unauthenticated calls
  if [ -z "${GITHUB_TOKEN:-}" ] && command -v gh >/dev/null 2>&1; then
    GITHUB_TOKEN="$(gh auth token 2>/dev/null || true)"
    export GITHUB_TOKEN
  fi
  $COMPOSE build samples
fi

$COMPOSE run --rm samples
