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

SCC_LAUNCHER=org.ebayopensource.scc.Launcher
SCC_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


. "$SCC_HOME/setenv.sh"

SCC_LIB_PATH="$SCC_HOME/$SCC_LIB_NAME"

echo Starting Service COLD Cache...
echo "Using SCC_HOME:   $SCC_HOME"
echo "Using SCC_LIB_PATH:   $SCC_LIB_PATH"
echo "Executing: java -classpath $SCC_LIB_PATH $SCC_LAUNCHER $*"

"$JAVA_HOME/bin/java" -Xmx1536m -XX:MaxPermSize=512m -Dorg.ebayopensource.ssc.initiator=CMD -classpath "$SCC_LIB_PATH" $SCC_LAUNCHER $*
