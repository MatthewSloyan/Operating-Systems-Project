package serverApp;

public class Bug {
	
	private String bugID;
	private String appName;
	private String date;
	private String platform;
	private String description;
	private String status;
	private String assignedTo;

	public Bug(String bugID, String appName, String date, String platform, String description, String status, String assignedTo) {
		super();
		this.bugID = bugID;
		this.appName = appName;
		this.date = date;
		this.platform = platform;
		this.description = description;
		this.status = status;
		this.assignedTo = assignedTo;
	}
	
	public Bug() {
		super();
	}

	public String getBugID() {
		return bugID;
	}

	public void setBugID(String bugID) {
		this.bugID = bugID;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}
}
