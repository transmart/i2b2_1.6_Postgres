package edu.harvard.i2b2.pm.services;

import java.util.Date;


///import edu.harvard.i2b2.pm.datavo.pm.ParamType;

public class UserData {
    // every persistent object needs an identifier
    
	//private String oid = null;
    private String oid = new String();
    private String name = new String();
    private String password = new String();
    private String email = new String();
    private String status = new String();
    private Date changeDate = new Date();
    
	public Date getChangeDate() {
		return changeDate;
	}
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}

}
