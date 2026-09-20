#!/bin/sh
set -eu

# Runs the sample generation itself. Intended to be run inside the docker image (see
# docker/docker-compose.yml), where D2 and Graphviz are pinned to the versions the committed
# samples were generated with. Use scripts/generateSamples.sh to drive it from the host.

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit 1

GRADLE="${GRADLE_CMD:-gradle -Dorg.gradle.java.home=/opt/java/openjdk/}"

for sample in sample-basic sample-d2 sample-graphviz sample-mermaid; do
  # shellcheck disable=SC2086 # GRADLE deliberately carries arguments
  $GRADLE atlasGenerate -p "samples/$sample"
done

# The layout engine comparison in docs/docs/usage-d2.md is one chart drawn by each engine.
# sample-basic configures no layout engine, so its chart source is engine-agnostic and d2 can
# render an image per engine straight from it. It lives in the build directory because the sample
# leaves intermediateFilesInBuildDir at its default.
CHART="samples/sample-basic/android/app/build/atlas/chart-d2.d2"
for engine in dagre elk tala; do
  d2 "$CHART" "docs/docs/img/d2-layoutEngine-$engine.svg" --layout="$engine"
done
