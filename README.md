## Bug Reporting System – Matthew Sloyan G00348036 

This project was for our Operating Systems module in semester 5 (year three). The task was to create a Multi-threaded TCP Bug Report Client/Server Application from a spec sheet. Some of the requirments included using a file to save user/bug data and making it synchronised for all users. Some of the decisions can be found below under their relevant headings.

## Overall:
The first thing I did was get the client and server running on the local machine with basic input. I then began to develop the sign-up system. To do this I wanted a simple and efficient way to be able to add so with research I found that using JSON to store my data would be a good fit, as it would allow me to easily parse and access the objects for modification, insertion and viewing. To achieve this, I have used Jackson which is a high-performance JSON processor for Java. It uses three jar files which are included in the “lib” file on the server application. This can parse maps, lists and many other data structures along with the ability to save them to text files. I decided to use a HashMap to store my data which allows for O(1) insertion, searching etc. Then with this map I add the new object and parse it to a text file on the server with Jackson. It also is handy for quickly parsing the file and searching it to check if the ID for example exists. 

Once this was in place, I started to flesh out the program both client and server side with the requirements of the project such as the login function, add bug function, edit bug etc. (More information below). I also have commented the code to JavaDoc standards, and all inputs have validation and loops till they are correct.

## Client Application:
This prints out and displays all the options to the user, all messages are sent back to the server for validation and to save data to the various files.
## Server Application:

### UI:
I wanted to keep it simple, clear, easy to use and understand. Initially the user once connected is given the option to login or signup.

### Sign Up:
If the user selected 2, then they enter a synchronized method which only allows one user to sign up at a time. I used a lock to achieve this which acts as a wait(). If another user enters the signup they must wait till the lock is released to avoid duplicate data, it will wait on input and proceed when unlocked.

The user is first given the option to enter their name, then their employee ID which must be unique, to check for this it first gets the map from the json file and searches for the ID. Next the user is asked to enter their email which must be unique and is checked the same way as ID. Lastly the user must enter their department.

### Login:
If the user selects 1 then they are given they are presented with the option to enter their user ID and email address. The ID is checked first as it’s the key in the map and then it checks the email with a loop, cutting down running time. If the credentials are correct the user is presented with 5 options (below)

#### 1) - Add a bug
If the user selected 1, then they enter a synchronized method which only allows one user to add a bug at a time. I used a lock to achieve this which acts as a wait(). If another user enters the addBug() method they must wait till the lock is released to avoid duplicate data.
The user is first given the option to enter the application name, platform and problem description. The description can be multiple words. The time/date is also initialized along with the status which is set to “Open”. Also, the bug ID is set by the server and is incremented by 1, so if the last bug ID is 5 the new one will be 6.

#### 2) - Assign a bug
If the user selected 1, then they enter a synchronized method which only allows one user to edit a bug at a time. The user is asked for a bug ID to search for which is compared against the bug file. If correct, the user must enter a user ID for a user in the system. If correct the bug will be assigned to that person and the status will be changed to assigned. To achieve this and cut down code I have one method that handles all the edits for option 5 below and this as they are all very similar. (1 =change status, 2 = append to description and 3= change assigned engineer)

#### 3) - View all unassigned bugs
If 3 is selected, then all unassigned bugs are formatted and sent to the client to be printed to the screen. To do this it checks if assigned to is “”. There is one method that handles both option 3 & 4 as they are similar. This can be displayed by any user at any time.

#### 4) - View all bugs
If 4 is selected, then all bugs are formatted and sent to the client to be printed to the screen. This can be displayed by any user at any time.

#### 5) - Update a bug
The user is given three options, which all use the same method and are synchronized to stop duplication.
* 1 = Change bug status
* 2 = Append to description
* 3 = Change assigned engineer – (uses same method as 2) – Assign a bug)


