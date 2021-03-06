#!/bin/bash

# Can't export `cygpath`ed JAVA_HOME on Cygwin, since that will corrupt it for Windows-y accesses
# Namely, the SBT build tries to configure its JAVA_HOME, and it would get Cygwin gunk
# Other things might rely on exported JAVA_HOMEs, though, so the solution here is to introduce
# an intermediary JH variable --JAB (3/22/13)
if [[ `uname -s` == *CYGWIN* ]] ; then
  CURR_DIR="$( cd "$( dirname "$0" )" && pwd )"
  JH=`cygpath -up "C:\Program Files\Java\jdk1.6.0_45"`
else
  CURR_DIR=`dirname $0`
  if [ `uname -s` = Linux ] ; then
    if [ -a /usr/lib/jvm/java-6-sun ] ; then
      export JAVA_HOME=/usr/lib/jvm/java-6-sun
    elif [ -a /usr/lib/jvm/jdk1.6.0_45 ] ; then
      export JAVA_HOME=/usr/lib/jvm/jdk1.6.0_45
    elif ! $JAVA_HOME/bin/java -version 2>&1 | head -n 1 | grep "1\.6" >> /dev/null ; then
      echo "Please set JAVA_HOME to version 1.6"
      exit
    fi
  else
    if [ `uname -s` = Darwin ] ; then
      export JAVA_HOME=`/usr/libexec/java_home -F -v1.6*`
    else
      export JAVA_HOME=/usr
    fi
  fi
  JH=$JAVA_HOME
fi


export PATH=$JH/bin:$PATH
JAVA=$JH/bin/java
if [[ `uname -s` == *CYGWIN* ]] ; then
  JAVA=$JH/bin/java.exe
fi

# Most of these settings are fine for everyone
XSS=-Xss10m
XMX=-Xmx1900m
XX=-XX:MaxPermSize=256m
ENCODING=-Dfile.encoding=UTF-8
HEADLESS=-Djava.awt.headless=true
USE_QUARTZ=-Dapple.awt.graphics.UseQuartz=false
BOOT=xsbt.boot.Boot
LAF=-Dnetlogo.quaqua.laf=ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel
GOGO_JAVA=-Dnetlogo.extensions.gogo.javaexecutable=$JAVA


SBT_LAUNCH=$HOME/.sbt/sbt-launch-0.13.9.jar
URL='http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/0.13.9/sbt-launch.jar'

if [ ! -f $SBT_LAUNCH ] ; then
  echo "downloading" $URL
  mkdir -p $HOME/.sbt
  curl -s -S -L -f $URL -o $SBT_LAUNCH || exit
fi

# Windows/Cygwin users need these settings
if [[ `uname -s` == *CYGWIN* ]] ; then

  # While you might want the max heap size lower, you'll run out
  # of heap space from running the tests if you don't crank it up
  # (namely, from TestChecksums)
  XMX=-Xmx1350m
  SBT_LAUNCH=`cygpath -w $SBT_LAUNCH`

  # This gets SBT working properly in my heavily-modded version of Cygwin --JAB (2/7/2012)
  if [ "$TERM" = "xterm" ] ; then
    TERMINAL=-Djline.terminal=jline.UnixTerminal
  fi

fi

# UseQuartz=false so that we get pixel for pixel identical drawings between OS's, so TestChecksums works - ST 6/9/10
"$JAVA" \
    $XSS $XMX $XX \
    $ENCODING \
    $JAVA_OPTS \
    $HEADLESS \
    $TERMINAL \
    $USE_QUARTZ \
    $LAF \
    -classpath $SBT_LAUNCH \
    $BOOT "$@"
