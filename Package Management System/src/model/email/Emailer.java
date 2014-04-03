package model.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;
import java.util.logging.Logger;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import model.IModelToViewAdaptor;
import util.Package;
import util.Pair;
import util.Person;
import util.PropertyHandler;

/*
 * Class that handles sending notification and reminder emails to students through
 * SMTP to the Gmail mail server.
 */

//TODO Timeout
//TODO If notification sending fails, add to a list of emails to send
//TODO Remove test tag

public class Emailer {
	
	private PropertyHandler propHandler;
	private String senderAddress;
	private String senderPassword;
	private String senderAlias;
	
	private String host;
	private Session session;
	private Transport transport;
	
	private Logger logger;
	private IModelToViewAdaptor viewAdaptor;
	
	public Emailer(IModelToViewAdaptor viewAdaptor) {
		// get PropertyHandler and logger instance
		this.propHandler = PropertyHandler.getInstance();
		this.logger = Logger.getLogger(Emailer.class.getName());
		
		this.viewAdaptor = viewAdaptor;
		
        this.host = "smtp.gmail.com";

	}
	
	public void start() {
		// get properties from PropertyHandler
		this.senderAddress = propHandler.getProperty("email.email_address");
		this.senderPassword = propHandler.getProperty("email.password");
		this.senderAlias = propHandler.getProperty("email.alias");
		 
		// warn the user if the email properties were not loaded
		while(this.senderAddress == null || this.senderPassword == null || this.senderAlias == null) {
			logger.warning("Failed to load email properties.");
			viewAdaptor.displayMessage("Email information was not loaded from file.\n"
					+ "Please change email information in the next window", 
					"Email Not Loaded");			
			changeEmail();
		}
		
		// attempt to connect to the mail server and alert user if it fails
		attemptConnection();
	}
	
	/**
	 * Function changes email, password, and alias to passed values
	 * @param newAddress		New Email address
	 * @param newPassword		New Password to email address
	 * @param newAlias			New Alias for sender
	 */
	public void setEmailProperties(String newAddress, String newPassword, String newAlias) {

		propHandler.setProperty("email.email_address",newAddress);
		propHandler.setProperty("email.password",newPassword);
		propHandler.setProperty("email.alias",newAlias);
		
		this.senderAddress = newAddress;
		this.senderPassword = newPassword;
		this.senderAlias = newAlias;
		
	}


	/**
	 * Function that sends all reminder emails
	 * @param allEntriesSortedByPerson	All active entries - MUST be sorted by person
	 * @return							Success of sending all reminders
	 */
	public boolean sendAllReminders(ArrayList<Pair<Person,Package>> allEntriesSortedByPerson) {
		
		//TODO Check if the last reminder was within the past day from calling function
//		propHandler.getProperty("email.last_reminder");
//		Calendar calendar = new GregorianCalendar(); 
		
		//collect ArrayList of pairs of person,ArrayList<Package>
		ArrayList<Pair<Person,ArrayList<Package>>> remindList = collectPairs(allEntriesSortedByPerson);		
		try {
			connect();
			// iterate through ArrayList, sending emails if the person has packages
			for (Pair<Person,ArrayList<Package>> ppPair : remindList) {
				sendPackageReminder(ppPair.first,ppPair.second);
			}
			closeConnection();
			
			// add property with the current time as the last sent date
			propHandler.setProperty("email.last_reminder", Long.valueOf(new Date().getTime()).toString());
			return true;
		} catch(NoSuchProviderException e) {
			logger.warning(e.getMessage());
			return false;
		} catch(MessagingException e) {
			logger.warning(e.getMessage());
			return false;
		} catch (UnsupportedEncodingException e) {
			logger.severe(e.getMessage());
			return false;
		}
	}

	/*
	 * Sends a notification email to the recipient informing them that they 
	 * have a new package 
	 */
	public boolean sendPackageNotification(Person recipient, Package pkg) {
		
		String subject = "[Test][Package Notification] New Package for " + recipient.getFullName();
		String body = new String();
		
		String comment = pkg.getComment();
		if(comment != null && !comment.isEmpty()) {
			body += "Comment: " + comment + "\n";
		}
		
		body += "Checked in on " + pkg.getCheckInDate().toString() + "\n\n";
		
		body += "Jones Mail Room";
		try {
			connect();
			sendEmail(recipient.getEmailAddress(), recipient.getFullName(), subject, body);
			closeConnection();
			return true;
		} catch (UnsupportedEncodingException e) {
			logger.severe("UnsupportedEncodingException for Person (ID: " + recipient.getPersonID() +
					") and Package (ID: " + pkg.getPackageID() + ")");
			return false;
		} catch (MessagingException e) {
			logger.warning(e.getMessage());
			return false;
		}
		
	}
	
	public String getSenderAddress() {
		return senderAddress;
	}

	public String getSenderAlias() {
		return senderAlias;
	}
	
	// connect to the mail server
	private void connect() 
			throws NoSuchProviderException, MessagingException {
		
		Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", senderAddress);
        props.put("mail.smtp.password", senderPassword);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        session = Session.getDefaultInstance(props);
		transport = session.getTransport("smtp");
		transport.connect(host, senderAddress, senderPassword);
		
	}
	
	// send an email through the mail server
	private void sendEmail(String recipientEmail, String recipientAlias, String subject,
			String body) throws UnsupportedEncodingException, MessagingException {
		
        MimeMessage message = new MimeMessage(session);
        
        message.setFrom(new InternetAddress(senderAddress, senderAlias));
        message.addRecipient(Message.RecipientType.TO, 
        		new InternetAddress(recipientEmail, recipientAlias));
        
        message.setSubject(subject);
        message.setText(body);
        transport.sendMessage(message, message.getAllRecipients());
	}

	// close the connection to the mail server
	private void closeConnection() throws MessagingException {
        transport.close();
	}
	
	/*
	 * Sends a reminder email to the recipient reminding them of each package that
	 * is returned by recipient.getPackageList()
	 */
	private void sendPackageReminder(Person recipient, ArrayList<Package> packages) 
			throws UnsupportedEncodingException, MessagingException {
		
		String subject = "[Test][Package Reminder] You have " + packages.size() + " package";
		if (packages.size() != 1) {
			subject += 's';
		}
		subject += " in the mail room";
		String body = "Hello " + recipient.getFirstName() + ",\n\nYour packages include: ";
		
		for (int i=0; i<packages.size(); i++) {
			Package pkg = packages.get(i);
			body += "\n\tPackage " + (i+1) + ":";
			body += "\n\t\tChecked in on " + pkg.getCheckInDate().toString();
			if(pkg.getComment() != "") {
				body += "\n\t\tComment: " + pkg.getComment();
			}
		}
		body += "\n\nPlease retrieve your packages as soon as possible.\n\n" + 
				"Jones Mail Room";
		sendEmail(recipient.getEmailAddress(), recipient.getFullName(), subject, body);
	}
	
	/**
	 * Function requests the view to get user input for new email information
	 */
	private void changeEmail() {
		String[] newEmail = viewAdaptor.changeEmail(senderAddress,senderPassword,senderAlias);
		if(newEmail != null) {
			setEmailProperties(newEmail[0],newEmail[1],newEmail[2]);
		}
	}
	
	/**
	 * Attempts to connect to the Gmail server with stored credentials
	 * Sends error messages to the view if an authentication error or a 
	 * general messaging error occurs.
	 */
	private void attemptConnection() {
		
		boolean retry = true;
		while(retry) {
			try {
				connect();
				closeConnection();
				retry = false;
			} catch (AuthenticationFailedException e){ 
					viewAdaptor.displayMessage("Incorrect username or password.\n","");
					changeEmail();
			} catch (MessagingException e) {
				logger.warning("Failed to connect to the mail server.");
				String[] options = {"Retry", "Cancel"};
				retry = viewAdaptor.getBooleanInput("Program failed to connect to the Gmail server.\n"
						+ "Please check your internet connection and try again.", 
						"Failed Connection",options);
				if(!retry) {
					viewAdaptor.displayMessage("Emails will not be sent until a connection is established.",
							"");
				}
			}
		}
		
	}
	
	/**
	 * Function that collects all of the pairs for the send all reminders function
	 * @param allEntriesSortedByPerson		DB entries sorted by person
	 * @return								ArrayList of pairs of person and owned packages
	 */
	private ArrayList<Pair<Person,ArrayList<Package>>> collectPairs(
			ArrayList<Pair<Person,Package>> allEntriesSortedByPerson) {
		
		// create a container for the result
		ArrayList<Pair<Person,ArrayList<Package>>> result = new ArrayList<Pair<Person,ArrayList<Package>>>();

		
		// initialize holders for the last person and a package list
		Person lastPerson = null;
		ArrayList<Package> pkgList = new ArrayList<Package>();
		for(Pair<Person,Package> entry : allEntriesSortedByPerson) {
			if (entry.first != lastPerson) {
				// if the person is new, add old person entry with packages
				if (lastPerson != null) {
					result.add(new Pair<Person,ArrayList<Package>>(lastPerson,pkgList));
				}
				
				// set the last person and give them a new package list
				lastPerson = entry.first;			// set the lastPerson
				pkgList = new ArrayList<Package>(); // new reference for new person
			}
			
			pkgList.add(entry.second);
		}
		
		// add the last person
		if (lastPerson != null && pkgList.size() > 0) {
			result.add(new Pair<Person,ArrayList<Package>>(lastPerson,pkgList));
		}
		
		return result;
	}
	
	public static void main(String[] args) {
		Emailer emailer = new Emailer(null);
		
		emailer.senderAddress = "JonesCollegeMailRoom@gmail.com";
		emailer.senderPassword = "jonesmailroomisbadass";
		emailer.senderAlias = "Jones Mail Room";
		
		Person navin = new Person("Pathak", "Navin", "np8@rice.edu","np8");
		Person nathan = new Person("Patrick", "Nathan", "Navin.Pathak@rice.edu","nathan");
		Person gavin = new Person("Pathak", "Gavin", "Pathak@rice.edu", "gavin");
		//Person chris = new Person("Henderson", "Chris", "cwh1@rice.edu");
		//Person michelle = new Person("Bennack", "Michelle", "mrb4@rice.edu");
		//Person ambi = new Person("Bobmanuel","Ambila","ajb6@rice.edu");
		
		Date now = new Date();
		Package p1 = new Package(123+now.getTime(),"",now);
		Package p2 = new Package(234+now.getTime(),"It's huge. Get it out now.",new Date(now.getTime()-100000000));
		Package p3 = new Package(309435+now.getTime(),"",new Date(now.getTime()-200000000));
		Package p4 = new Package(1+now.getTime(),"",new Date(now.getTime()-300000000));
		Package p5 = new Package(2+now.getTime(),"",new Date(now.getTime()-400000000));
		
		ArrayList<Pair<Person,Package>> entries = new ArrayList<Pair<Person,Package>>();
		entries.add(new Pair<Person,Package>(navin,p1));
		entries.add(new Pair<Person,Package>(navin,p2));
		entries.add(new Pair<Person,Package>(nathan,p3));
		entries.add(new Pair<Person,Package>(nathan,p4));
		entries.add(new Pair<Person,Package>(gavin,p5));
		
		emailer.sendPackageNotification(navin,p2);
		emailer.sendPackageNotification(navin,p1);
		
		emailer.sendAllReminders(entries);
	}

}
