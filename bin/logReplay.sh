#!/bin/bash

#
# Wrapper script needs improvement!
#
# @author Andre van Hoorn

BINDIR=$(cd "$(dirname "$0")"; pwd)/

JAVAARGS="-Dkicker.common.logging.Log=JDK -Djava.util.logging.config.file=${BINDIR}/logging.properties"
MAINCLASSNAME=kicker.tools.logReplayer.FilesystemLogReplayerStarter

java ${JAVAARGS} -cp "${BINDIR}/../lib/*":"${BINDIR}/../build/libs/*":"${BINDIR}" ${MAINCLASSNAME} "$@"
