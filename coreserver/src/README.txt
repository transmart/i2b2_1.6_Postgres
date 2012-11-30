README for Transmart, prostreSQL version.

Instructions for building and deploying the i2b2 modules that have been modified 
to use postgreSQL.

Load modules from: github/transmart/i2b2_1.6_Postgres/ 
In the instructions below BASE_DIR refers to the directory
i2b2_1.6_Postgres/coreserver/src/

NOTE: if you are building for windows the below steps can
be achieved by running i2b2Deploy.bat; see the source there for detail.

NOTE: the first two steps (configuration and building i2b2.common)
must proceed the build of any or all of the modules, but after that
you may build the module in any order, or choose which modules to build.

1. set up configuration files
  In each subdirectory for the modules below, if necessary, edit
  build.properties to change the location of tomcat, jboss, or
  the i2b2 war file/dir name.
  
2. deploy i2b2.common
  cd $BASE_DIR/edu.harvard.i2b2.common
  ant -f build.xml clean dist deploy jboss_pre_deployment_setup

 
3. deploy Pm module
  cd $BASE_DIR/edu.harvard.i2b2.pm
  ant -f master_build.xml clean build-all deploy

4. deploy Ontology module
  cd $BASE_DIR/edu.harvard.i2b2.ontology
  ant -f master_build.xml clean build-all deploy

5. deploy CRC module
  cd $BASE_DIR/edu.harvard.i2b2.crc.loader
  ant -f build.xml clean dist
  cd $BASE_DIR/edu.harvard.i2b2.crc
  ant -f master_build.xml clean build-all deploy

6. deploy Workplace module
  cd $BASE_DIR/edu.harvard.i2b2.workplace
  ant -f master_build.xml clean build-all deploy

7. deploy FR module
  cd $BASE_DIR/edu.harvard.i2b2.fr
  ant -f master_build.xml clean build-all deploy
