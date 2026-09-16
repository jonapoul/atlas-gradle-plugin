FROM gradle:9.7.1-jdk21@sha256:52016096dc2fa801f949ae2715daf42296228ec6112882bf714f2f0d9e98935f

# apt-get update shares a layer with the installs on purpose: cached on its own, the package
# index goes stale and later fetches start 404ing on superseded versions.
RUN apt-get update && apt-get install -y \
  curl \
  graphviz \
  libgvplugin-neato-layout8 \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

# https://d2lang.com/tour/install/
# Pinned because d2's SVG output changes between releases, and the committed samples are diffed in CI.
# renovate: datasource=github-releases depName=d2lang/d2
ENV D2_VERSION=v0.9.0
RUN curl -fsSL https://d2lang.com/install.sh | sh -s -- --version $D2_VERSION

WORKDIR /atlas
CMD ["/bin/bash"]
