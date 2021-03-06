package main.java.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import main.java.model.database.Database;
import main.java.model.email.Emailer;
import main.java.model.email.TemplateHandler;
import main.java.model.print.LabelPrinter;
import main.java.util.Package;
import main.java.util.Pair;
import main.java.util.Person;

/**
 * Package Manager class functions as a large model to string together the
 * database, emailer, and printer
 * @author Navin
 *
 */
public class PackageManager {

	private IModelToViewAdapter viewAdapter;
	
	private Database db;
	private Emailer mailer;
	private LabelPrinter printer;

	private static Logger logger = Logger.getLogger(PackageManager.class.getName());


	public PackageManager(IModelToViewAdapter adpt) {
		
		viewAdapter = adpt;
		
		// initialize the database
		db = new Database(viewAdapter);
		mailer = new Emailer(viewAdapter);
		printer = new LabelPrinter(viewAdapter);
	}
	
	public void start() {
		logger.info("[Model.start()] Starting model...");
		// Start the database, mailer, and printer
		db.start();
		mailer.start(db.getEntries("checked_in=TRUE", "person_ID=ASCENDING"));
		printer.start();
	}
	
	//TODO Functions that run on a schedule - mainly send reminders
	
	/*
	 * Emailer functions
	 */
	public boolean sendPackageNotification(String personID, long pkgID) {

		Person person = db.getPerson(personID);
		Package pkg = db.getPackage(pkgID);
		if(mailer.sendPackageNotification(person, pkg)) {
			pkg.setNotificationSent(true);
			db.editPackage(pkg);
			return true;
		}
		return false;
	}
	
	public boolean sendPackageReminders() {
		ArrayList<Pair<Person,Package>> entriesSortedByPerson = 
				db.getEntries("checked_in=true", "person_ID=ASCENDING");
		return mailer.sendAllReminders(entriesSortedByPerson);
	}
	
	public boolean checkAdminPassword(String password) {
		return false;
	}

	public void changeEmail(String newEmail, String newPassword, String newAlias) {
		mailer.setEmailProperties(newEmail, newPassword, newAlias);
	}
	
	public String getEmailAddress() {
		return mailer.getSenderAddress();
	}

	public String getEmailAlias() {
		return mailer.getSenderAlias();
	}
	
	public String getRawEmailTemplate() {
		return TemplateHandler.getRawFile();
	}
	
	public Map<String,String> getEmailTemplates(boolean convert, boolean comments) {
		return TemplateHandler.getTemplates(convert, comments);
	}
	
	public void changeEmailTemplate(Map<String,String> newTemplate) {
		TemplateHandler.writeNewTemplates(newTemplate);
	}
	
	/*
	 * Printer functions
	 */
	public boolean printLabel(long pkgID) {
		String personName = db.getOwner(pkgID).getLastFirstName();
		return printer.printLabel(Long.valueOf(pkgID).toString(), personName);
	}
	
	public String[] getPrinterNames() {
		return printer.getPrinterNames();
	}
	
	public void setPrinter(String printerName) {
		printer.setPrinter(printerName);
	}
	
	/*
	 * Database functions
	 */
	
	public long checkInPackage(String personID, String comment) {
		// create a packageID
		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmss");
		long pkgID = Long.valueOf(ft.format(now));
		
		Package pkg = new Package(pkgID, comment, now);
		db.checkInPackage(personID, pkg);
		return pkg.getPackageID();
	}
	
	public boolean checkOutPackage(long pkgID) {
		return db.checkOutPackage(pkgID);		
	}
	
	public Package getPackage(long pkgID) {
		return db.getPackage(pkgID);
	}
	
	public Person getOwner(long pkgID) {
		return db.getOwner(pkgID);
	}

	public ArrayList<Person> getPersonList(String searchString) {
		return db.getPersonList(searchString);
	}
	

	public ArrayList<Pair<Person, Package>> getPackages(String filter, String sort) {
		return db.getEntries(filter,sort);
	}

	public void importPersonCSV(String fileName) {
		db.importPersonsFromCSV(fileName);		
	}

	public boolean addPerson(Person person) {
		return db.addPerson(person);
	}

	public boolean editPerson(Person person) {
		return db.editPerson(person);		
	}

	public boolean deletePerson(String personID) {
		return db.deletePerson(personID);
	}
	
}
