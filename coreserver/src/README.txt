README for Transmart, prostreSQL version.

Instructions for building and deploying the i2b2 modules that have been modified 
to use postgreSQL.

Load modules from: github/transmart/i2b2_1.6_Postgres/ 

In the instructions below BASE_DIR refers to the directory
i2b2_1.6_Postgres/coreserver/src/
YOUR_JBOSS_HOME refers to the location of your preconfigured i2b2 JBoss install

NOTE: the first step (building i2b2.common) must proceed the build of 
any or all of the modules, but after that you may build the modules 
in any order, or choose which modules to build.
  
1. deploy i2b2.common
  cd $BASE_DIR/edu.harvard.i2b2.common
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  ant clean dist deploy jboss_pre_deployment_setup
 
2. deploy Pm module
  cd $BASE_DIR/edu.harvard.i2b2.pm
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit etc/jboss/pm-ds.xml to reflect your postgreSQL install
    you may have to modify: connection-url, user-name, and password
  ant -f master_build.xml clean build-all deploy

3. deploy Ontology module
  cd $BASE_DIR/edu.harvard.i2b2.ontology
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit the etc/jboss/ont-ds.xml and configure your data sources
    you may have to modify: connection-url, user-name, and password
    changes may be necessary for: i2b2hive, i2b2metadata, and i2b2metadata2
  ant -f master_build.xml clean build-all deploy

4. deploy CRC module
  cd $BASE_DIR/edu.harvard.i2b2.crc.loader
  ant -f build.xml clean dist
  cd $BASE_DIR/edu.harvard.i2b2.crc
  ant -f master_build.xml clean build-all deploy

5. deploy Workplace module
  cd $BASE_DIR/edu.harvard.i2b2.workplace
  ant -f master_build.xml clean build-all deploy

6. deploy FR module
  cd $BASE_DIR/edu.harvard.i2b2.fr
  ant -f master_build.xml clean build-all deploy
