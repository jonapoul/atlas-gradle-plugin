# Contributing

## Issues

Before opening an issue, check whether it's already been reported. For bugs, a link to a project that shows the problem helps a lot. Failing that, include your Gradle setup and your Atlas config.

## Pull requests

For anything bigger than a small fix, please open an issue first so we can agree on the approach.

You'll need Java 21 or newer. Then:

```bash
./gradlew build           # build and test
./gradlew check           # detekt, licensee and the ABI dump
./scripts/ktfmt.sh        # format your changes
```

If you change the public API, run `./gradlew updateKotlinAbi` and commit the updated dump, otherwise `check` will fail.

New behaviour should come with tests. Most tests build a sample multi-module project from `src/test/kotlin/atlas/test/scenarios/` and run Gradle against it with TestKit.

For anything that changes output charts, make sure to also run `./scripts/generateSamples.sh`.

## Code of conduct

Please follow the [code of conduct](CODE_OF_CONDUCT.md).
