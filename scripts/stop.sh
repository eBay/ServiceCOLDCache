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

arg="$1.xx"
if [ ".xx" = $arg ]; then
  ## No arguments passed so use default dir
  SCC_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
else
  ## Argument passed in so use dir of what is passed in instead
  SCC_HOME=$1
fi

# . "$SCC_HOME/setenv.sh"

_pid=`ps -ef|grep "$SCC_LAUNCHER"|grep -v grep |awk '{print $2}'`

if [[ -n "$_pid" ]]; then
  kill -9 $_pid
fi

echo Service COLD Cache stopped...!
