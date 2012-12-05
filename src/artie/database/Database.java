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
	
	public Node getResponse(LinkedList<String> keywords)
	{
		return currentResponse;
	}
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
	
	public void writeResponse(String[] keywords,String[] message)
	{
		
	}
	
	public void setResponseKeywordWeight(Node keyword, double weight)
	{
		
	}
	
	public void addResponseMessage(Node response, String message)
	{
		
	}

	private Node getResponseWithHeighestWeight()
	{
		return null;
	}
	
}
