package artie.database;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import artie.utilities.Logger;

public class Database
{
	private Document doc;
	private File database;
	private Node lastResponse;
	private Node currentResponse;
	private Node secondBestResponse; // If there is a repeat use this one
										// instead.
	private LinkedList<Node> lastKeywords;
	private double responseWeight;
	private double secondBestWeight;

	public Database(String fileName)
	{
		database = new File(fileName);
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(database);
			doc.getDocumentElement().normalize();
		}
		catch (Exception ex)
		{
			Logger.Log("Failed to build database.");
		}
	}

	public String getResponse(String[] keywords)
	{
		lastResponse = currentResponse;
		getResponseWithHeighestWeight(keywords);
		return getResponseMessage();
	}

	public Node getResponseNode()
	{
		return currentResponse;
	}

	// TODO Get a random message from a response
	public String getResponseMessage()
	{
		Element responseElement = (Element)currentResponse;
		if (responseElement!=null)
		{
			NodeList messages = responseElement.getElementsByTagName("Message");
			Random random = new Random();

			String message = messages.item(0).getTextContent();
			return message;
		}
		else
			return null;
	}

	public String getSecondBestResponseMessage()
	{
		Logger.Log("Get second best response.");
		Element responseElement = (Element)secondBestResponse;
		if (responseElement!=null)
		{
			NodeList messages = responseElement.getElementsByTagName("Message");
			Random random = new Random();

			String message = messages.item(0).getTextContent();
			return message;
		}
		else
			return null;
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
		return secondBestWeight;
	}

	// TODO create a new response from scratch
	public void writeResponse(String[] keywords, String[] message)
	{

	}

	// TODO set a particular keyword's weight
	public void setResponseKeywordWeight(Node keyword, double weight)
	{

	}

	// TODO Add code to add a message to a particular response
	public void addResponseMessage(Node response, String message)
	{

	}

	// TODO Add code to get the best and second best response from database.
	private void getResponseWithHeighestWeight(String[] keywords)
	{
		
		LinkedList<Node> responses = new LinkedList<Node>();
		responses = getResponsesWithSimilarKeywords(keywords);
		getBestResponseNode(responses,keywords);
	}
	
	private LinkedList<Node> getResponsesWithSimilarKeywords(String[] keywords)
	{
		NodeList keywordsInDatabase = doc.getElementsByTagName("Keyword");
		LinkedList<Node> responses = new LinkedList<Node>();
		LinkedList<String> keywordsList = new LinkedList<String>(Arrays.asList(keywords));
		
		for (int i=0; i<keywordsInDatabase.getLength();i++)
		{
			Node currentKeyword = keywordsInDatabase.item(i);
			if (keywordsList.contains(currentKeyword.getTextContent()))
			{
				Node responseNode = currentKeyword.getParentNode().getParentNode();
				if (!responses.contains(responseNode))
					responses.add(responseNode);
			}			
		}
		
		return responses;
	}

	private void getBestResponseNode(LinkedList<Node> responses,String[] keywords)
	{
		double best = 0.0;
		double current = 0.0;
		Node bestResponse = null;
		LinkedList<String> keywordsList = new LinkedList<String>(Arrays.asList(keywords));
		for (Node response : responses)
		{
			Element responseElement = (Element)response;
			NodeList responseKeywordNodes = responseElement.getElementsByTagName("Keyword");
			for (int i=0; i<responseKeywordNodes.getLength();i++)
			{
				Node currentKeyword = responseKeywordNodes.item(i);
				if (keywordsList.contains(currentKeyword.getTextContent()))
				{
					NamedNodeMap attributes = currentKeyword.getAttributes();
					double weight = Double.parseDouble(attributes.getNamedItem("weight").getNodeValue());
					current += weight;
				}
			}
			
			if (current>best)
			{
				secondBestResponse = bestResponse;
				secondBestWeight = best;
				best = current;
				current = 0;
				bestResponse = response;
				responseWeight = best;
				currentResponse = response;
			}
			
		}
	}
}
