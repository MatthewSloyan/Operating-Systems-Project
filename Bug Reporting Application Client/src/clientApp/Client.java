package clientApp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client side bug reporting application
 * 
 * @author Matthew Sloyan
 */
public class Client {
	
	private Socket connection;
	private String message;
	private Scanner stdin;
	private String ipaddress;
	private int portaddress;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	/**
	 * Initial Method for user to connect to server
	 */
	public Client()
	{
		stdin = new Scanner(System.in);
		
		System.out.println("Enter the IP Address of the server");
		ipaddress = stdin.nextLine();
		
		System.out.println("Enter the TCP Port");
		portaddress  = stdin.nextInt();
	}
	
	/**
	 * main method to run program
	 * 
	 * @see Client
	 */
	public static void main(String[] args) 
	{
		new Client().clientapp();
	}
	
	/**
	 * Method to send message to the server
	 */
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	/**
	 * Base of client side application
	 * Handles all user options for program and sends/receives all messages from the server
	 * The user can either login or signup, and once logged in they are presented with multiple options
	 * 1) - Add a bug, 2) - Assign a bug, 3) - View all unasigned bugs, 4) - View all bugs, 5) - Update a bug
	 * The takeInDisplayMessage() method is used when client input is required to cut down duplicate code.
	 */
	public void clientapp()
	{
		try 
		{
			//Set up connection to server
			connection = new Socket(ipaddress,portaddress);
		
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			System.out.println("Client Side ready to communicate");
		
		    /// Client App.
			//3: Communicating with the server
			try
			{	
				//outer loop for signup/login selection
				do {
					//read in menu, and take in user option
					message = (String)in.readObject();
					System.out.println(message);
					message = stdin.next();
					sendMessage(message);
					
					//LOG IN
					//=============================
					if (message.equalsIgnoreCase("1")) {
						
						//Employee ID - takeInDisplayMessage() method is used throughout when client input is required (cuts down duplicate code)
						takeInDisplayMessage(message); 
						//email
						takeInDisplayMessage(message);
						
						//check if user is valid
						message = (String)in.readObject();
						if (message.equals("Valid")) {
							
							message = (String)in.readObject(); //success message
							System.out.println(message);
							
							//inner do
							do {
								//read in menu
								message = (String)in.readObject();
								System.out.println(message);
								message = stdin.next();
								sendMessage(message);
								
								//ADD A BUG
								if (message.equalsIgnoreCase("1")) { 
									message = (String)in.readObject();
									System.out.println(message);
									
									//Application Name
									message = (String)in.readObject();
									System.out.println(message);
									stdin.nextLine();
									message = stdin.nextLine();
									sendMessage(message);
									
									//Platform
									takeInDisplayMessage(message);
									
									//Problem description
									message = (String)in.readObject();
									System.out.println(message);
									stdin.nextLine();
									message = stdin.nextLine();
									sendMessage(message);
								}
								//ASSIGN A BUG
								else if (message.equalsIgnoreCase("2")) {
									//header
									message = (String)in.readObject();
									System.out.println(message);
									
									//search for bug
									takeInDisplayMessage(message);
									
									message = (String)in.readObject();
									if (message.equals("Valid")) { 
										//Person you would like to assign to (must be a registered user)
										takeInDisplayMessage(message);
										
										message = (String)in.readObject();
										if (message.equals("Valid")) { 
										}
										else {
											//error message
											message = (String)in.readObject();
											System.out.println(message);
										}
									}
									else {
										//error message
										message = (String)in.readObject();
										System.out.println(message);
									}
								}
								//VIEW ALL UNASSIGNED BUGS
								else if (message.equalsIgnoreCase("3")) {
									message = (String)in.readObject();
									System.out.println(message);
									
									//read in length of list, otherwise display error message if no unnasigned bugs
									message = (String)in.readObject();
									if (Integer.parseInt(message) > 0) {
										
										//read in full string of all bugs
										message = (String)in.readObject();
										System.out.println(message);
									}
									else {
										//error message
										message = (String)in.readObject();
										System.out.println(message);
									}
								}
								//VIEW ALL BUGS
								else if (message.equalsIgnoreCase("4")) {
									message = (String)in.readObject();
									System.out.println(message);
									
									//read in length of list, otherwise display error message if no bugs
									message = (String)in.readObject();
									if (Integer.parseInt(message) > 0) {
										
										//read in full string of all bugs
										message = (String)in.readObject();
										System.out.println(message);
									}
									else {
										//error message
										message = (String)in.readObject();
										System.out.println(message);
									}
								}
								//UPDATE BUG
								else if (message.equalsIgnoreCase("5")) {
									
									//menu selection
									message = (String)in.readObject();
									System.out.println(message);
									message = stdin.next();
									sendMessage(message);
									
									//Update status
									if (message.equalsIgnoreCase("1")) { 
										message = (String)in.readObject();
										System.out.println(message);
										
										takeInDisplayMessage(message);
										
										message = (String)in.readObject();
										if (message.equals("Valid")) { 
											takeInDisplayMessage(message);
										}
										else {
											//error message
											message = (String)in.readObject();
											System.out.println(message);
										}
									}
									//Append to description
									else if (message.equalsIgnoreCase("2")) {
										message = (String)in.readObject();
										System.out.println(message);
										
										//search by bug ID
										takeInDisplayMessage(message);
										
										message = (String)in.readObject();
										if (message.equals("Valid")) { 
											message = (String)in.readObject();
											System.out.println(message);
											stdin.nextLine();
											message = stdin.nextLine();
											sendMessage(message);
										}
										else {
											//error message
											message = (String)in.readObject();
											System.out.println(message);
										}
									}
									//CHANGE ENGINEER
									else if (message.equalsIgnoreCase("3")) {
										message = (String)in.readObject();
										System.out.println(message);
										
										//search by bug ID
										takeInDisplayMessage(message);
										
										message = (String)in.readObject();
										if (message.equals("Valid")) { 
											takeInDisplayMessage(message);
											
											message = (String)in.readObject();
											if (message.equals("Valid")) { 
											}
											else {
												//error message
												message = (String)in.readObject();
												System.out.println(message);
											}
										}
										else {
											//error message
											message = (String)in.readObject();
											System.out.println(message);
										} //if valid
									}
									else {
										//if update bug menu input is invalid
										message = (String)in.readObject();
										System.out.println(message);
									}
								}
								else {
									message = (String)in.readObject();
									System.out.println(message);
								}
								
								//read in user option to log out to select another menu option
								message = (String)in.readObject();
								System.out.println(message);
								message = stdin.next();
								sendMessage(message);
							} while(message.equalsIgnoreCase("Y")); //inner do
						}
						else {
							//error message
							message = (String)in.readObject();
							System.out.println(message);
						}
					}
					
					//SIGN UP
					//==============
					else if (message.equalsIgnoreCase("2")) {
						//NAME
						takeInDisplayMessage(message);
						
						//EMP ID
						do {
							takeInDisplayMessage(message);
							
							message = (String)in.readObject();
						} while (message.equals("Not Valid"));
						
						//EMAIL
						do {
							takeInDisplayMessage(message);
							
							message = (String)in.readObject();
						} while (message.equals("Not Valid"));
						
						//DEPARTMENT
						takeInDisplayMessage(message);
					}
					
					//read in option for menu loop
					message = (String)in.readObject();
					System.out.println(message);
					message = stdin.next();
					sendMessage(message);
				}while(message.equalsIgnoreCase("Y"));
				
			}
			catch(ClassNotFoundException classNot)
			{
				System.err.println("data received in unknown format");
			}	
		} 
		
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				out.close();
				in.close();
				connection.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	/**
	 * Method is used when client input is required to cut down duplicate code.
	 * The message is read in from server, printed out and user input is taken and sent back.
	 * 
	 * @throws ClassNotFoundException if error occurs
	 * @throws IOException if error occurs
	 */
	private void takeInDisplayMessage(String message) throws ClassNotFoundException, IOException {
		message = (String)in.readObject();
		System.out.println(message);
		message = stdin.next();
		sendMessage(message);
	}
}
