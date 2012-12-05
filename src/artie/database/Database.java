package artie.database;

import java.io.File;
import java.util.LinkedList;

import org.w3c.dom.Node;

public class Database
{
	//private DOM doc
	private File database;
	private Node lastResponse;
	private Node currentResponse;
	private Node secondBestResponse; //If there is a repeat use this one instead.
	private LinkedList<Node> lastKeywords;
	private double responseWeight;
	private double secondBestResponseWeight;
	
	public Database(String fileName)
	{
		database = new File(fileName);
	}
	public String getResponse(String[] keywords)
	{
		lastResponse = currentResponse;
		currentResponse = getResponseWithHeighestWeight(); 
		return getResponseMessage();
	}
	
	public Node getResponseNode()
	{
		return currentResponse;
	}
	//TODO Get a random message from a response
	public String getResponseMessage()
	{
		return "Response message";
	}
	
	public String getSecondBestResponseMessage()
	{
		return "Second best response message";
	}
	
	public Node getSecondBestResponse()
	{
		return secondBestResponse;
	}
	
	public LinkedList<Node> getResponseKeywords()
	{
		return lastKeywords;
	}
	
	public double getResponseWeight()
	{
		return responseWeight;
	}
	
	public double getSecondBestResponseWeight()
	{
		return secondBestResponseWeight;
	}
	//TODO create a new response from scratch
	public void writeResponse(String[] keywords,String[] message)
	{
		
	}
	//TODO set a particular keyword's weight
	public void setResponseKeywordWeight(Node keyword, double weight)
	{
		
	}
	
	//TODO Add code to add a message to a particular response
	public void addResponseMessage(Node response, String message)
	{
		
	}
	
	//TODO Add code to get the best and second best response from database. 
	private Node getResponseWithHeighestWeight()
	{
		return null;
	}
	
}
