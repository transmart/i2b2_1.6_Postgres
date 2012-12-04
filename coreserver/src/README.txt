README for Transmart, prostreSQL version.

Instructions for building and deploying the i2b2 modules that have been modified 
to use postgreSQL.

Load modules from: github/transmart/i2b2_1.6_Postgres/ 

In the instructions below BASE_DIR refers to the directory
i2b2_1.6_Postgres/coreserver/src/
and YOUR_JBOSS_HOME refers to the location of 
your preconfigured i2b2 JBoss install.

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
  Edit etc/jboss/ont-ds.xml and configure your data sources
    you may have to modify: connection-url, user-name, and password
    changes may be necessary for: i2b2hive, i2b2metadata, and i2b2metadata2
  Edit etc/spring/ontology_application_directory.properties
    set edu.harvard.i2b2.ontology.applicationdir
    to YOUR_JBOSS_HOME_DIR/server/default/conf/ontologyapp
  ant -f master_build.xml clean build-all deploy

4. deploy CRC module
  cd $BASE_DIR/edu.harvard.i2b2.crc.loader
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit etc/spring/crc_loader_application_directory.properties
    set edu.harvard.i2b2.crc.loader.applicationdir to be
    YOUR_JBOSS_HOME_DIR/server/default/conf/crcapp
  ant -f build.xml clean dist

  cd $BASE_DIR/edu.harvard.i2b2.crcplugin.patientcount
  Edit the build.properties file
  	crcplugin.home=YOUR_JBOSS_HOME_DIR/server/default
  	jboss.home=YOUR_JBOSS_HOME_DIR
  Edit etc/spring/patientcount_application_directory.properties
    set edu.harvard.i2b2.crcplugin.pb.applicationdir
    to YOUR_JBOSS_HOME_DIR/server/default/conf/crcapp
  
  cd $BASE_DIR/edu.harvard.i2b2.crc
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit etc/spring/crc_application_directory.properties
    set edu.harvard.i2b2.crc.applicationdir to be
    YOUR_JBOSS_HOME_DIR/server/default/conf/crcloaderapp
  ant -f master_build.xml clean build-all deploy

5. deploy Workplace module
  cd $BASE_DIR/edu.harvard.i2b2.workplace
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit etc/spring/workplace_application_directory.properties
    set edu.harvard.i2b2.workplace.applicationdir
    to YOUR_JBOSS_HOME_DIR/server/default/conf/workplaceapp
  ant -f master_build.xml clean build-all deploy

6. deploy FR module
  cd $BASE_DIR/edu.harvard.i2b2.fr
  Edit the build.properties file
  	set jboss.home and axis2.war.name properties as approperate
  	(you can ignore the tomcat setting)
	jboss.home=YOUR_JBOSS_HOME_DIR
	axis2.war.name=i2b2.war
  Edit etc/spring/fr_application_directory.properties
    set edu.harvard.i2b2.fr.applicationdir
    to YOUR_JBOSS_HOME_DIR/server/default/conf/frapp
  ant -f master_build.xml clean build-all deploy


SUMMARY of all the files that need modification
(relative to BASE_DIR)
./edu.harvard.i2b2.common/build.properties

./edu.harvard.i2b2.pm/build.properties
./edu.harvard.i2b2.pm/etc/jboss/pm-ds.xml

./edu.harvard.i2b2.ontology/build.properties
./edu.harvard.i2b2.ontology/etc/jboss/ont-ds.xml
./edu.harvard.i2b2.ontology/etc/spring/ontology_application_directory.properties

./edu.harvard.i2b2.crc.loader/build.properties
./edu.harvard.i2b2.crc.loader/etc/spring/crc_loader_application_directory.properties

./edu.harvard.i2b2.crcplugin.patientcount/build.properties
./edu.harvard.i2b2.crcplugin.patientcount/etc/spring/patientcount_application_directory.properties

./edu.harvard.i2b2.crc/build.properties
./edu.harvard.i2b2.crc/etc/spring/crc_application_directory.properties

./edu.harvard.i2b2.workplace/build.properties
./edu.harvard.i2b2.workplace/etc/spring/workplace_application_directory.properties

./edu.harvard.i2b2.fr/build.properties
./edu.harvard.i2b2.fr/etc/spring/fr_application_directory.properties
