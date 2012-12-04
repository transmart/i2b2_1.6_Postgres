#!/bin/bash

cd coreserver/src
cd edu.harvard.i2b2.common
ant
cd ../edu.harvard.i2b2.crc.loader
ant
cd ../edu.harvard.i2b2.crc
ant
cd ../edu.harvard.i2b2.pm
ant
cd ../edu.harvard.i2b2.ontology
ant
