package serverApp;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Date;
import java.util.HashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Server side bug reporting application
 * 
 * @author Matthew Sloyan
 */
public class Server {
	
	/**
	 * main method to run program
	 * 
	 * @see Connecthandler
	 */
	public static void main(String[] args) {
		ServerSocket listener;
		int clientid = 0;
		
		try 
		{
			 listener = new ServerSocket(2005,10);
			 
			 while(true)
			 {
				System.out.println("Main thread listening for incoming new connections");
				Socket newconnection = listener.accept();
				
				System.out.println("New connection received and spanning a thread");
				
				//start new thread for each user
				new Thread(new Connecthandler(newconnection, clientid)).start();
				clientid++;
			 }
		} 
		
		catch (IOException e) 
		{
			System.out.println("Socket not opened");
			e.printStackTrace();
		}
	}
}

/**
 * Base class of server side application
 */
class Connecthandler implements Runnable {
	private Socket individualconnection;
	private int socketid;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private String message;
	
	private String name, empID, email, department; //signup
	private String appName, platform, description, status; //add bug
	private String bugID, assignedTo, newDescription; //edit bug
	private boolean isValidUser = false, isValidBug = false; //validation
	
	private static Lock userLock = new ReentrantLock();
	private static Lock bugLock = new ReentrantLock();
	
	private final File userFile = new File("user.json"); //user file
	private final File bugFile = new File("bug.json"); //bug file
	
	ObjectMapper mapper = new ObjectMapper();
	
	public Connecthandler(Socket s, int i)
	{
		individualconnection = s;
		socketid = i;
	}
	
	/**
	 * Method to send message to the client
	 */
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("client> " + msg);
		}
		catch(IOException e){
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Runnable thread that handles all options for program and receives and sends all messages to the client
	 * The user can either login or signup on the client and the server will check the data against the saved files.
	 * 
	 * @see loginFunction
	 * @see signUpFunction
	 */
	public void run()
	{
		try 
		{
			out = new ObjectOutputStream(individualconnection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(individualconnection.getInputStream());
			System.out.println("Connection"+ socketid+" from IP address "+individualconnection.getInetAddress());
		
			//Commence the conversation with the client......
			
			do {
				sendMessage("Press 1 to Login or 2 to register a new user.");
				message = (String)in.readObject();
				
				//LOG IN
				//=============================
				if (message.equalsIgnoreCase("1")) {
					loginFunction();
				}
				
				//SIGN UP
				//=============================
				else if (message.equalsIgnoreCase("2")) {
					userLock.lock();
					try {
						signUpFunction();
					}
					finally{
						userLock.unlock();
					}
				} //else if
				
				sendMessage("Press Y to login/Sign Up or N to terminate");
				message = (String)in.readObject();
				
			}while(message.equalsIgnoreCase("Y"));	
		}
		catch (IOException | ClassNotFoundException e) 
		{
			System.out.println("Error: " + e.getMessage());
		}
		
		finally
		{
			try 
			{
				//close connections
				out.close();
				in.close();
				individualconnection.close();
			}
			catch (IOException e) 
			{
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	/**
	 * Allows the user to enter a employee ID and email address to login
	 * If valid the user is presented with 5 menu options which allow the user to add a bug,
	 * assign a bug, view all bugs and edit bugs.
	 * addBug and findEditBug methods are synchronized to allow only one user to edit at one time.
	 * 
	 * @see addBug 
	 * @see findEditBug
	 * @see viewUnassignedAndAllBugs
	 */
	private void loginFunction() {
		try {
			sendMessage("Please enter your Employee ID.");
			empID = (String)in.readObject();
			
			sendMessage("Please enter email address.");
			email = (String)in.readObject();
		
			//get map of users to check if user exists
			Map<String, User> userMap = mapper.readValue(userFile, new TypeReference<Map<String,User>>(){});
			
			isValidUser = false;
			
			//check ID first to avoid unnecessary loop over users 
			if (userMap.containsKey(empID)) {
				User users = userMap.get(empID);
				
				//then check email against input ID, if email is wrong then display error
				//this ensures that the user can't access the account even if they know the id
				if (users.getEmpID().equals(empID) && users.getEmail().equals(email)) {
					isValidUser = true;
					
					sendMessage("Valid");
					sendMessage("Login Sucessful, hello " + users.getName() + "!\n");
					
					//Logged in USER
					do {
						sendMessage("Please enter your selection\n 1) - Add a bug\n 2) - Assign a bug"
								+ "\n 3) - View all unassigned bugs \n 4) - View all bugs \n 5) - Update a bug");
						message = (String)in.readObject();
						
						if (message.equalsIgnoreCase("1")) { 
							//add a bug (synchronized)
							bugLock.lock();
							try {
								addBug();
							}
							finally{
								bugLock.unlock();
							}
						}
						else if (message.equalsIgnoreCase("2")) {
							sendMessage("\nAssign a Bug\n=========");
							
							//call method to handle all editing bugs (3 = edit assigned to)
							bugLock.lock();
							try {
								findEditBug(3);
							}
							finally{
								bugLock.unlock();
							}
						}
						else if (message.equalsIgnoreCase("3")) {
							sendMessage("\nView all unassigned Bugs\n====================");
							
							//view all unassigned bugs
							viewUnassignedAndAllBugs(1);
						}
						else if (message.equalsIgnoreCase("4")) {
							sendMessage("\nView all Bugs\n====================");
							
							//view all bugs
							viewUnassignedAndAllBugs(2);
						}
						else if (message.equalsIgnoreCase("5")) {
							
							sendMessage("\nUpdate a Bug\n=========\n\nPlease enter your selection\n 1) - Update Status\n 2) - Append to Problem"
									+ "\n 3) - Assign new Engineer \n");
							message = (String)in.readObject();
							
							if (message.equalsIgnoreCase("1")) { 
								sendMessage("\nChange Status\n=========");
								
								//call method which handles all bug updates (1 = Change status)
								bugLock.lock();
								try {
									findEditBug(1);
								}
								finally{
									bugLock.unlock();
								}
							}
							else if (message.equalsIgnoreCase("2")) {
								sendMessage("\nAppend to Description\n=========");
								
								bugLock.lock();
								try {
									findEditBug(2);
								}
								finally{
									bugLock.unlock();
								}
							}
							else if (message.equalsIgnoreCase("3")) {
								sendMessage("\nAssign a Bug\n=========");
								
								bugLock.lock();
								try {
									findEditBug(3);
								}
								finally{
									bugLock.unlock();
								}
							}
							else {
								sendMessage("Invalid entry, please try again");
							}
						}
						else {
							sendMessage("Invalid entry, please try again");
						}
						
						sendMessage("Press Y to select another option or N to logout");
						message = (String)in.readObject();
					}while(message.equalsIgnoreCase("Y"));	
				}
	        } //outer if (first login check)
			
			if (isValidUser == false) {
				sendMessage("Not Valid");
				sendMessage("Loggin attempt failed, please try again");
			}
		}	
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Allows the user add a bug to the system (synchronized)
	 * 
	 * @see saveToBugFile
	 */
	private synchronized void addBug() {
		try {
			sendMessage("\nAdd a Bug\n=========");
			
			//get map of bugs to find the largest bugID and add one to it below
			Map<String, Bug> bugMap = mapper.readValue(bugFile, new TypeReference<Map<String,Bug>>(){});
			int largestBugID = 0;
			
			for (String s: bugMap.keySet()) {
				if (Integer.parseInt(s) > largestBugID) {
					largestBugID = Integer.parseInt(s);
				}
			}
			
			sendMessage("Please enter the application Name.");
			appName = (String)in.readObject();
			
			sendMessage("Please enter the platform (Windows, Mac, Unix)");
			platform = (String)in.readObject();
			
			sendMessage("Please enter the problem description");
			description = (String)in.readObject();
			
			// Instantiate a Date object
		    Date date = new Date();
			
			Bug bug = new Bug(Integer.toString(++largestBugID), appName, date.toString(), platform, description, "Open", "");
			
			saveToBugFile(bug);
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Allows the user to view either all unassigned bugs or all bugs in the system
	 * option 1 = all unassigned bugs
	 * option 2 = all bugs
	 */
	private void viewUnassignedAndAllBugs(int option) {
		try {
			//get map of bugs
			Map<String, Bug> bugMap = mapper.readValue(bugFile, new TypeReference<Map<String,Bug>>(){});
			
			//send size to client
			sendMessage(Integer.toString(bugMap.size()));
			
			if (bugMap.size() > 0) {
				//build up a string of all the unnassigned bugs in the system and send to client
				StringBuilder sb = new StringBuilder();
				
				sb.append("Bug ID   App Name     Date                                Platform        Description                         Status        Assigned To\n");
				
				if (option == 1) {
					for (Bug bugDetails : bugMap.values()) {
					    if (bugDetails.getAssignedTo().equals("")) {	
					    	sb.append(String.format("%-8s %-12s %-35s %-15s %-35s %-13s %-10s %n", bugDetails.getBugID(), bugDetails.getAppName(), 
					    		bugDetails.getDate(), bugDetails.getPlatform(), bugDetails.getDescription(),
					    			bugDetails.getStatus(), bugDetails.getAssignedTo()));
					    }
					}//for
				}
				else {
					for (Bug bugDetails : bugMap.values()) {
				    	sb.append(String.format("%-8s %-12s %-35s %-15s %-35s %-13s %-10s %n", bugDetails.getBugID(), bugDetails.getAppName(), 
				    		bugDetails.getDate(), bugDetails.getPlatform(), bugDetails.getDescription(),
				    			bugDetails.getStatus(), bugDetails.getAssignedTo()));
					}
				}
				
				//send full string to client to be printed out
				sendMessage(sb.toString());
			}
			else {
				sendMessage("There are no bugs in the system\n");
			}
		}	
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Allows the user to either edit the assigned user, edit the status or append to the description
	 * option 1 = edit status
	 * option 2 = append to description
	 * option 3 = edit assigned user
	 */
	private synchronized void findEditBug(int option) {
		try {
			sendMessage("Please enter bug ID to search for");
			bugID = (String)in.readObject();
	
			//get map of bugs to check if bug id exists
			Map<String, Bug> map = mapper.readValue(bugFile, new TypeReference<Map<String,Bug>>(){});
			
			isValidBug = false;
			
			if (map.containsKey(bugID)) {
				
				isValidBug = true;
				Bug bug = map.get(bugID);
				
				sendMessage("Valid");
				
				if (option == 1) {
					sendMessage("Please enter the new status of Bug: " + bugID + " (Open, Assigned or Closed)");
					status = (String)in.readObject();
				
					//set new status
					bug.setStatus(status);
				}
				else if (option == 2) {
					sendMessage("Current Description is: " + bug.getDescription() + "\nPlease enter additional comments to add.");
					description = (String)in.readObject();
					
					//append input to old description and set
					newDescription = bug.getDescription() + " " + description;
					bug.setDescription(newDescription);
				}
				else {
					sendMessage("Please enter the user ID of the person you would like to assign Bug: " + bugID + " to.");
					assignedTo = (String)in.readObject();
					
					//get map of users to check if user exists
					Map<String, User> userMap = mapper.readValue(userFile, new TypeReference<Map<String,User>>(){});
					if(userMap.containsKey(assignedTo)) {
						sendMessage("Valid");
						
						//change assigned to value and set status to assigned
						User user = userMap.get(assignedTo);
						bug.setAssignedTo(user.getName());
						bug.setStatus("Assigned");
					}
					else {
						sendMessage("Not Valid");
						sendMessage("User ID not found, please try again");
					}
				}
				
				//put updated object back in map and rewrite to file
				map.put(bugID, bug);
				mapper.writeValue(bugFile, map);
			}
			
			if (isValidBug == false) {
				sendMessage("Not Valid");
				sendMessage("Bug No not found, please try again");
			}
			
		}
		catch (IOException | ClassNotFoundException e) 
		{
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Allows the user to sign up and enter in their details to be saved to the user file.
	 * This method is synchronized to allow only one user to signup at a time.
	 * 
	 * @see saveToUserFile
	 */
	private synchronized void signUpFunction() {
		try {
			//NAME
			sendMessage("Please enter your Name");
			name = (String)in.readObject();
			
			//ID
			do {
				sendMessage("Please enter Employee ID. (Must be unique)");
				empID = (String)in.readObject();
				
				//get map of users to check if ID is unique
				Map<String, User> map = mapper.readValue(userFile, new TypeReference<Map<String,User>>(){});
				
				isValidUser = true;
				
				if (map.containsKey(empID)){
			    	isValidUser = false;
			    	sendMessage("Not Valid");
				}
				else {
					sendMessage("Valid");
				}
			} while (isValidUser == false);
			
			//EMAIL
			do {
				sendMessage("Please enter your Email (Must be unique)");
				email = (String)in.readObject();
				
				//get map of users to check if email is unique
				Map<String, User> map = mapper.readValue(userFile, new TypeReference<Map<String,User>>(){});
				
				isValidUser = true;
					
				for (User userDetails : map.values()) {
				    if (userDetails.getEmail().equals(email)){
				    	isValidUser = false;
				    	sendMessage("Not Valid");
				    }
				}
				
				if (isValidUser == true) {
					sendMessage("Valid");
				}
			} while (isValidUser == false);
			
			//DEPARTMENT
			sendMessage("Please enter your Department");
			department = (String)in.readObject();
			
			User user = new User(name, empID, email, department);
			
			//save data to user file
			saveToUserFile(user);
		}	
		catch (IOException | ClassNotFoundException e) 
		{
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * Saves the full user map to the user file
	 */
	private void saveToUserFile(User user) {
		Map<String, User> map;
		
		//check if file exists, if so then get existing data and add new object
		try {
			if(userFile.exists()) { 
				map = mapper.readValue(userFile, new TypeReference<Map<String,User>>(){});
			}
			else {
				map = new HashMap<String, User>();
			}

			map.put(user.getEmpID(), user);
			mapper.writeValue(userFile, map);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	/**
	 * Saves the full bug map to the bug file
	 */
	private void saveToBugFile(Bug bug) {
		Map<String, Bug> bugMap;
		
		//check if file exists, if so then get existing data and add new object
		try {
			if(bugFile.exists()) { 
				bugMap = mapper.readValue(bugFile, new TypeReference<Map<String,Bug>>(){});
			}
			else {
				bugMap = new HashMap<String, Bug>();
			}
			
			bugMap.put(bug.getBugID(), bug);
			mapper.writeValue(bugFile, bugMap);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
