package main.java.util;

/*
 * Implementation of a pair from StackOverflow
 */

public class Pair<FirstType, SecondType> { 
	  public final FirstType first; 
	  public final SecondType second; 
	  public Pair(FirstType first, SecondType second) { 
	    this.first = first; 
	    this.second = second; 
	  } 
	}