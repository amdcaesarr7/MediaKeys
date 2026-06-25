#!/bin/sh
# Gradle wrapper script for Unix
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

die() {
    echo
    echo "ERROR: $*"
    echo
    exit 1
}

warn() {
    echo "WARNING: $*"
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
  NONSTOP* ) nonstop=true ;;
esac

DIRNAME=`dirname "$0"`
cd "$DIRNAME" || exit
APP_HOME=`pwd -P`

JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_EXE=java

exec "$JAVA_EXE" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$JAR" \
  org.gradle.wrapper.GradleWrapperMain "$@"
