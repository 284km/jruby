#!/bin/bash

# Truffle may eventually become part of OpenJDK, and when that happens we won't
# want to package com.oracle.truffle in complete. This script removes those
# packages from a build of complete.

# You will want to run mvn -Pcomplete first.

# Takes no arguments. Modifies a copy of
# maven/jruby-complete/target/jruby-complete-9.0.0.0.dev.jar to create
# maven/jruby-complete/target/jruby-complete-no-truffle-9.0.0.0.dev.jar.

# Chris Seaton, 5 Aug 14

cp maven/jruby-complete/target/jruby-complete-9.0.0.0.dev-SNAPSHOT.jar maven/jruby-complete/target/jruby-complete-no-truffle-9.0.0.0.dev-SNAPSHOT.jar
zip -d maven/jruby-complete/target/jruby-complete-no-truffle-9.0.0.0.dev-SNAPSHOT.jar com/oracle/nfi/* com/oracle/truffle/*
