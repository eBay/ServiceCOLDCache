#*******************************************************************************
# Copyright (c) 2016 eBay Software Foundation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************
#!/bin/bash
SCC_LAUNCHER=org.ebayopensource.io.netty.http.snoop.HttpSnoopClient
SCC_HOME=$PWD

arg="$1.xx"
if [ ".xx" != $arg -a \( "$1" = "enableCache" -o "$1" = "disableCache" -o "$1" = "cleanupCache" \) ] ; then
  ## valid arguments passed
  EXEC_CMD=$1
else
  ## No valid arguments passed
  echo "Usage: $0 <action>(enableCache/disableCache/cleanupCache)"
  exit 0
fi

. "$SCC_HOME/setenv.sh"

echo EXEC_CMD is $EXEC_CMD

CMD_LINE_ARGS=$*
echo "CMD_LINE_ARGS:$CMD_LINE_ARGS"

  if [ ! -f $SCC_LIB_NAME ] ; then
    echo "ERROR: the library $SCC_LIB_NAME is not found."
    exit -1
  fi


SCC_LIB_PATH=$SCC_HOME/$SCC_LIB_NAME

echo "Using SCC_HOME:   $SCC_HOME"
echo "Using SCC_LIB_PATH:   $SCC_LIB_PATH"

java -classpath "$SCC_LIB_PATH" $SCC_LAUNCHER $CMD_LINE_ARGS
