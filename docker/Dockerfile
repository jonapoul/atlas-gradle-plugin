FROM gradle:9.8.1-jdk21@sha256:5ec5f726f14851a11a1bac6e6a5720e41062c33357053e7717eb0d189a6f1db9

# apt-get update shares a layer with the installs on purpose: cached on its own, the package
# index goes stale and later fetches start 404ing on superseded versions.
RUN apt-get update && apt-get install -y \
  curl \
  graphviz \
  libgvplugin-neato-layout8 \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# https://d2lang.com/tour/install/
# Pinned in config/d2.version, since d2's SVG output changes between releases and the committed
# samples are diffed in CI. The same file sets the version Atlas downloads by default.
# install.sh calls the GitHub API, so a token lifts it off the unauthenticated rate limit
COPY config/d2.version /tmp/d2.version
RUN --mount=type=secret,id=github_token,env=GITHUB_TOKEN \
  D2_VERSION="$(tr -d '[:space:]' < /tmp/d2.version)" \
  && test -n "$D2_VERSION" \
  && curl -fsSL https://d2lang.com/install.sh | sh -s -- --version "v$D2_VERSION" \
  && rm /tmp/d2.version

WORKDIR /atlas
CMD ["/bin/bash"]
