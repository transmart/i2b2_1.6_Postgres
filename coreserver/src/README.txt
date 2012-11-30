@echo off
REM i2b2Deploy.bat
REM -------------------------------------------------------------------
REM       I2B2 DEPLOY SCRIPT
REM -------------------------------------------------------------------

set CURRENT_DIR= %CD%
set I2B2_DIR= %3%

REM -------------------------------------------------------------------
REM DEPLOY_MODULE=0 Indicates deployment of individual modules
REM DEPLOY_MODULE=1 Indicates deployment of all modules
REM -------------------------------------------------------------------
set DEPLOY_MODULE=0

if /I "%2" == "all"  (      
	set DEPLOY_MODULE=1
	goto deployAll
)
if /I "%2" == "common"     goto deployCommon
if /I "%2" == "pm"         goto deployPm
if /I "%2" == "ontology"   goto deployOntology
if /I "%2" == "crc"        goto deployCrcloader
if /I "%2" == "workplace"  goto deployWorkplace
if /I "%2" == "fr"         goto deployFr
echo Usage: deploy all^|common^|pm^|ontology^|crc^|workplace^|fr directory_name
goto cmdEnd

REM %~dp1

:deployAll
goto deployCommon

:deployCommon
call CD%I2B2_DIR%\edu.harvard.i2b2.common
call ant -f build.xml clean dist deploy jboss_pre_deployment_setup
echo Deployed module common
if not %DEPLOY_MODULE% == 1 goto cmdEnd
goto deployPm
 
:deployPm
call CD%I2B2_DIR%\edu.harvard.i2b2.pm
call ant -f master_build.xml clean build-all deploy
echo Deployed Module PM
if not %DEPLOY_MODULE% == 1 goto cmdEnd
goto deployOntology

:deployOntology
call CD%I2B2_DIR%\edu.harvard.i2b2.ontology
call ant -f master_build.xml clean build-all deploy
echo Deployed Module Ontology
if not %DEPLOY_MODULE% == 1 goto cmdEnd
goto deployCrcloader

:deployCrcloader
call CD%I2B2_DIR%\edu.harvard.i2b2.crc.loader
call ant -f build.xml clean dist
echo Deployed Crcloader
goto deployCrc

:deployCrc
call CD%I2B2_DIR%\edu.harvard.i2b2.crc
call ant -f master_build.xml clean build-all deploy
echo Deployed Module Crc
if not %DEPLOY_MODULE% == 1 goto cmdEnd
goto deployWorkplace

:deployWorkplace
call CD%I2B2_DIR%\edu.harvard.i2b2.workplace
call ant -f master_build.xml clean build-all deploy
echo Deployed Module Workplace
if not %DEPLOY_MODULE% == 1 goto cmdEnd
goto deployFr

:deployFr
call CD%I2B2_DIR%\edu.harvard.i2b2.fr
call ant -f master_build.xml clean build-all deploy
echo Deployed module Fr
goto cmdEnd

:cmdEnd
call CD%CURRENT_DIR%  
