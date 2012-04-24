/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.pdo.output;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.harvard.i2b2.crc.datavo.pdo.query.OutputOptionType;


/**
 * Class to generate select, join, where clause
 * for patient dimenstion based on pdo's  OutputOptionType
 * $Id: PatientFactRelated.java,v 1.3 2007/08/31 14:43:33 rk903 Exp $
 * @author rkuttan
 */
public class PatientFactRelated extends FactRelated {
    public PatientFactRelated(OutputOptionType outputOptionType) {
        super(outputOptionType);
    }

    public String getSelectClause() {
        String selectClause = "";

        if (isSelected()) {
            selectClause = "  patient.patient_num patient_patient_num";

            if (isSelectDetail()) {
                selectClause += " ,patient.vital_status_cd patient_vital_status_cd, patient.birth_date patient_birth_date, patient.death_date patient_death_date, patient.sex_cd patient_sex_cd, patient.age_in_years_num patient_age_in_years_num, patient.language_cd patient_language_cd, patient.race_cd patient_race_cd, patient.marital_status_cd patient_marital_status_cd, patient.religion_cd patient_religion_cd, patient.zip_cd patient_zip_cd, patient.statecityzip_path patient_statecityzip_path";
            }

            if (isSelectBlob()) {
                selectClause += ", patient.patient_blob patient_patient_blob ";
            }

            if (isSelectStatus()) {
                selectClause += " , patient.update_date patient_update_date, patient.download_date patient_download_date, patient.import_date patient_import_date, patient.sourcesystem_cd patient_sourcesystem_cd, patient.upload_id patient_upload_id ";
            }
        }

        return selectClause;
    }

    public String joinClause() {
        if (isSelected()) {
            return " left join PATIENT_DIMENSION patient on (obs.patient_num = patient.patient_num) ";
        } else {
            return "";
        }
    }

    public int getPatientNumFromResultSet(ResultSet resultSet)
        throws SQLException {
        return resultSet.getInt("obs_patient_num");
    }
}
