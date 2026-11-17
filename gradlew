#!/usr/bin/env sh
GRADLE_BIN="C:/Users/cocro/.gradle/wrapper/dists/gradle-9.6.0-bin/42k10rwplmzkhuboz9kdazi7s/gradle-9.6.0/bin/gradle"
if [ -f "" ]; then
    exec "" "$@"
else
    exec gradle "$@"
fi
