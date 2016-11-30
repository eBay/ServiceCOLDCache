@rem ***************************************************************************
@rem Copyright (c) 2016 eBay Software Foundation.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem  http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem ***************************************************************************
@ECHO off
REM setlocal EnableDelayedExpansion

SET "WINDOWS_TITLE=Service Cold Cache"
SET "SCC_LIB_NAME=scc.jar"
SET "SCC_HOME=%CD%"

REM check java if valid
REM check version 
FOR /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    SET _javaVer=%%g
)
SET _javaVer=%_javaVer:"=%

FOR /f "tokens=2 delims=." %%g in ('ECHO %_javaVer%') do (
	SET _javaMinorVersion=%%g
)

rem check if java in path
IF [%_javaMinorVersion%] EQU [] (
	ECHO No Java in PATH
	EXIT /b 1
)
rem check java version less than 1.7
IF %_javaMinorVersion% EQU +%_javaMinorVersion% (
	IF !_javaMinorVersion! LSS 7 (
		ECHO The version of Java in PATH is less than JDK 7
		EXIT /b 1
	)
)
