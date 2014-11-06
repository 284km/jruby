#!/bin/bash

# This script combines the public snapshots of JRuby and Graal to produce a
# tarball that will use the included Graal VM instead of your local Java. This
# means you can run jruby and get Graal without having to do any other
# configuration.

# Takes no arguments. Produces:
#    * jruby-dist-9.0.0.0+graal-linux-x86_64.dev-bin.tar.gz
#    * jruby-dist-9.0.0.0+graal-macosx-x86_64.dev-bin.tar.gz
# which are the artifacts to be published.

# Run in the root directory. Run -Pdist first.

tar -zxf maven/jruby-dist/target/jruby-dist-9.0.0.0.dev-bin.tar.gz || exit $?
mv jruby-9.0.0.0.dev-SNAPSHOT jruby-9.0.0.0.dev || exit $?

# Remove files we aren't going to patch so people don't use them by mistake

rm jruby-9.0.0.0.dev/bin/*.bat jruby-9.0.0.0.dev/bin/*.sh jruby-9.0.0.0.dev/bin/*.bash jruby-9.0.0.0.dev/bin/*.exe jruby-9.0.0.0.dev/bin/*.dll

# Patch the jruby bash script to set JAVACMD and JRUBY_OPTS

sed -i.backup 's|if \[ -z "\$JAVACMD" \] ; then|# Modifications for distribution with Graal\
JAVACMD=\"\$JRUBY_HOME/graalvm-jdk1.8.0/bin/java\"\
JRUBY_OPTS=\"-J-server -J-d64 \$JRUBY_OPTS\"\
\
if [ -z "$JAVACMD" ] ; then|' jruby-9.0.0.0.dev/bin/jruby

if diff jruby-9.0.0.0.dev/bin/jruby jruby-9.0.0.0.dev/bin/jruby.backup >/dev/null ; then
  echo "patch didn't work"
  exit 1
fi

rm jruby-9.0.0.0.dev/bin/jruby.backup

chmod +x jruby-9.0.0.0.dev/bin/jruby || exit $?

function pack {
    # $1 ... platform (linux, ...)
    # $2 ... jdk-release (b122, ...)
    # $3 ... graal-release (0.1, ...)

    buildname=openjdk-8-graalvm-$2-$1-x86_64-$3.tar.gz
    if [ ! -e $buildname ]
    then
      wget http://lafo.ssw.uni-linz.ac.at/graalvm/$buildname || exit $?
    fi

    tar -zxf $buildname || exit $?
    chmod -R +w graalvm-jdk1.8.0
    cp -r graalvm-jdk1.8.0 jruby-9.0.0.0.dev || exit $?
    rm -rf jruby-9.0.0.0.dev/graalvm-jdk1.8.0/src.zip jruby-9.0.0.0.dev/graalvm-jdk1.8.0/demo jruby-9.0.0.0.dev/graalvm-jdk1.8.0/include jruby-9.0.0.0.dev/graalvm-jdk1.8.0/sample
    targetname=jruby-dist-9.0.0.0+graal-$1-x86_64.dev-bin.tar.gz
    tar -zcf $targetname jruby-9.0.0.0.dev || exit $?
    shasum -a 1 $targetname > $targetname.sha1
}

pack "linux" "b132" "0.4"
pack "macosx" "b132" "0.4"
