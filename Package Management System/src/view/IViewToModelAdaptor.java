package view;

import java.util.ArrayList;
import util.Package;
import util.Pair;
import util.Person;

/*
 * Interface outlining the operations that can be performed by the view (MainFrame)
 * on the model (model.database)
 */

public interface IViewToModelAdaptor {
	
	/*
	 * Pick up functions
	 */
	
	/**
	 * Returns true if the package is already checked out
	 * 
	 * @param pkgID				ID of the package to determine state of
	 * @return					Whether or not the package is checked out
	 */
	public boolean checkedOut(long pkgID);
	
	/**
	 * returns the owner for use in confirmation message
	 * 
	 * @param pkgID				ID of the package to determine the owner of
	 * @return 					Person object containing information about owner
	 */
	public Person getPackageOwner(long pkgID);
	
	/**
	 * Checks out the package after getting confirmation
	 * 
	 * @param pkgID				ID of the package to be checked out
	 */
	public void checkOutPackage(long pkgID);
	
	/*
	 * Check in functions
	 */
	
	/**
	 * Returns a list of person objects filtered by searchString
	 * 
	 * @param searchString		String representing search 
	 * @return					ArrayList of person objects matching searchString
	 */
	public ArrayList<Person> getPersonList(String searchString);
	
	/**
	 * Checks in a package into the database
	 * 
	 * @param personID			ID of the package owner
	 * @param comment			Comment input by the user
	 */
	public void checkInPackage(String personID, String comment);
	
	/**
	 * Send a package notification email alerting the person
	 * that a package has been checked in
	 * 
	 * @param personID			ID of the package owner
	 * @return					Success of sending email
	 */
	public boolean sendPackageNotification(String personID);

	/**
	 * Print a label for the package. 
	 * Can also be called by the admin functions.
	 * 
	 * @param pkgID				ID of the package 
	 * @return					Success of printing
	 */
	public boolean printLabel(long pkgID);
	
	/**
	 * Returns a list of all of the printers with associated drivers on
	 * the computer.
	 * 
	 * @param acceptingJobsOnly	If true, return only printers that are accepting jobs
	 * @return					Array of Strings of printer names
	 */
	public String[] getPrinterNames(boolean acceptingJobsOnly);
	
	/*
	 * Admin functions
	 */
	
	/**
	 * Authenticates the user to gain access to the admin functions
	 * 
	 * @param password			User input password
	 * @return					Success of authentication
	 */
	public boolean authenticate(String password);
	
	/**
	 * Changes the email information of the user and attempts to connect to the mail
	 * server with the new credentials. Returns whether or not this was successful.
	 * 
	 * @param newEmail			New email input by user
	 * @param newPassword		New password input by user
	 * @param newAlias			New email alias input by user
	 */
	public void changeEmail(String newEmail, String newPassword, String newAlias);
	
	/**
	 * Returns the email address stored
	 * @return 					String containing email address
	 */
	public String getEmailAddress();
	
	/**
	 * Returns the email alias stored
	 * @return 					String containing email alias
	 */
	public String getEmailAlias();
	
	/**
	 * Returns all of the packages with the appropriate filters applied
	 * 
	 * @param filter			String containing filtering information
	 * @param sort				String containing sorting information
	 * @return					ArrayList of (person, package) pairs after filter is applied
	 */
	public ArrayList<Pair<Person,Package>> getPackages(String filter, String sort);
	
	/**
	 * Reads a list of people from a csv file and adds the people to the database
	 * CSV format: LastName,FirstName,EmailAddress,PersonID 
	 * 
	 * @param fileName			Location of the csv
	 * @return					Success of importing the person list
	 */
	public void importPersonCSV(String fileName);
	
	/**
	 * Adds a person to the database
	 *
	 * @param personID			personID of the person - user input
	 * @param firstName			First Name of the person - user input
	 * @param lastName			Last Name of the person - user input
	 * @param emailAddress		Email Address of the person - user input
	 */
	public void addPerson(String personID, String firstName, String lastName, String emailAddress);
	
	/**
	 * Edits a person already in the database
	 * 
	 * @param personID			personID of the person - must already exist in the database
	 * @param firstName			First Name of the person - user input
	 * @param lastName			Last Name of the person - user input
	 * @param emailAddress		Email Address of the person - user input
	 */
	public void editPerson(String personID, String firstName, String lastName, String emailAddress);
	
	/**
	 * Deletes a person already in the database
	 * 
	 * @param personID			personID of the person - must already exist in the database
	 */
	public void deletePerson(String personID);
}
