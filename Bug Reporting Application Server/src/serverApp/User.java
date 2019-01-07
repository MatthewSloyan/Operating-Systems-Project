package serverApp;

public class User {
	
	private String name;
	private String empID;
	private String email;
	private String department;
	
	public User(String name, String empID, String email, String department) {
		super();
		this.name = name;
		this.empID = empID;
		this.email = email;
		this.department = department;
	}

	public User() {
		super();
	}

	//Gets and sets
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmpID() {
		return empID;
	}

	public void setEmpID(String empID) {
		this.empID = empID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
