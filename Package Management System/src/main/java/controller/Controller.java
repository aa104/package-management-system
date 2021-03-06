package main.java.controller;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import main.java.model.IModelToViewAdapter;
import main.java.model.PackageManager;
import main.java.util.FileIO;
import main.java.util.LogHandler;
import main.java.util.Package;
import main.java.util.Pair;
import main.java.util.Person;
import main.java.util.PropertyHandler;
import main.java.view.IViewToModelAdaptor;
import main.java.view.MainFrame;

/**
 * Controller controlling the view and the underlying model,
 * instantiating them both as well as their communications and adaptors.
 */
public class Controller{
	
	private MainFrame viewFrame;
	private PackageManager modelPM;

	private LogHandler logHandler;
	private PropertyHandler propHandler;

	private static Logger logger = Logger.getLogger(Controller.class.getName());
	private String progDirName;

	/**
	 * Function which initializes and starts the controller
	 */
	public static void main(String[] args) {
		System.out.println("mvc started..!!!!");
		EventQueue.invokeLater(() -> {
			try {
				Controller control = new Controller();
				control.start();
				logger.info("Control started...");

			} catch (Exception e) {
				logger.info("Control NOT started...");
				logger.severe(e.getMessage());
			}
		});
	}
	
	/**
	 * Class which acts as the go between for the model and the view
	 */
	private Controller() {

		this.propHandler = PropertyHandler.getInstance();
		this.logHandler = new LogHandler();
		init();
		
		/* Initializes the view */
		viewFrame = new MainFrame(new IViewToModelAdaptor() {

			public Package getPackage(long pkgID) {
				return modelPM.getPackage(pkgID);
			}

			public Person getPackageOwner(long pkgID) {
				return modelPM.getOwner(pkgID);
			}
			
			public boolean checkOutPackage(long pkgID) {
				return modelPM.checkOutPackage(pkgID);
			}
			
			public ArrayList<Person> getPersonList(String searchString) {
				return modelPM.getPersonList(searchString);
			}
			
			public long checkInPackage(String personID, String comment) {
				return modelPM.checkInPackage(personID, comment);
			}
			
			public boolean sendPackageNotification(String personID, long pkgID) {
				return modelPM.sendPackageNotification(personID,pkgID);
			}
			
			public boolean sendPackageReminders() {
				return modelPM.sendPackageReminders();
			}
			
			public boolean printLabel(long pkgID) {
				return modelPM.printLabel(pkgID);
			}
			
			public String[] getPrinterNames() {
				return modelPM.getPrinterNames();
			}
			
			public void setPrinter(String PrinterName) {
				modelPM.setPrinter(PrinterName);
			}
			
			public ArrayList<Pair<Person,Package>> getPackages(String filter, String sort) {
				return modelPM.getPackages(filter,sort);
			}
			
			public boolean authenticate(String password) {
				return modelPM.checkAdminPassword(password);
			}
			
			public void changeEmail(String newEmail, String newPassword, String newAlias) {
				modelPM.changeEmail(newEmail, newPassword, newAlias);
			}
			
			public String getEmailAddress() {
				return modelPM.getEmailAddress();
			}
			
			public String getEmailAlias() {
				return modelPM.getEmailAlias();
			}
			
			public void importPersonCSV(String fileName) {
				modelPM.importPersonCSV(fileName);
			}

			public boolean addPerson(String personID, String firstName,
					String lastName, String emailAddress) {
				Person person = new Person(lastName,firstName,emailAddress,personID);
				return modelPM.addPerson(person);
				
			}

			public boolean editPerson(String personID, String firstName,
					String lastName, String emailAddress) {
				Person person = new Person(lastName,firstName,emailAddress,personID);
				return modelPM.editPerson(person);
				
			}

			public boolean deletePerson(String personID) {
				return modelPM.deletePerson(personID);
			}

			public String getRawEmailTemplate() {
				return modelPM.getRawEmailTemplate();
			}

			public Map<String,String> getEmailTemplates(boolean convert, boolean comments) {
				return modelPM.getEmailTemplates(convert, comments);
			}

			public void changeEmailTemplate(Map<String, String> newTemplate) {
				modelPM.changeEmailTemplate(newTemplate);
			}
			
		});
		
		/* Initialize model */
		modelPM = new PackageManager(new IModelToViewAdapter() {
			public void displayMessage(String message, String title) {
				viewFrame.displayMessage(message, title);

			}

			public void displayError(String error, String title) {
				viewFrame.displayError(error, title);

			}

			public void displayWarning(String warning, String title) {
				viewFrame.displayWarning(warning, title);

			}

			public String getChoiceFromList(String message, String title,
											String[] choices) {
				return viewFrame.getChoiceFromList(message, title, choices);
			}

			public String[] changeEmail(String senderAddress,
										String senderPassword, String senderAlias) {
				return viewFrame.changeEmail(senderAddress, senderPassword, senderAlias);
			}

			public boolean getBooleanInput(String message, String title,
										   String[] options) {
				int response = viewFrame.getButtonInput(message,title,options);
				return response == 0;
			}

			public String getPrinterNames(String[] printerNames) {
				return viewFrame.getPrinterName(printerNames);
			}
		});
	}
	
	/**
	 * Function initializes helper classes
	 */
	public void init() {
		// set and create root directory, if it doesn't exist
		String home = System.getProperty("user.home");
		this.progDirName = home + "/Documents/Package Management System";
//		this.progDirName = "C:\\Users\\Ayo\\Desktop\\Rice\\Personal Projects\\" +
//				"Package-Management-System";


		FileIO.init(progDirName);
		FileIO.makeDirs(progDirName);

		propHandler.init(progDirName);
		logHandler.init(progDirName);
		logHandler.cleanLogs();
	}

	private void start() {
		// Start view and model
		viewFrame.start();
		modelPM.start();
	}

}
