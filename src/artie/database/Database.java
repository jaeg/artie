package artie.database;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
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
	private LinkedList<String> lastKeywordsGiven;
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
			Logger.log("Failed to build database.");
		}
	}

	public String getResponse(String[] keywords)
	{
		lastResponse = currentResponse;
		lastKeywordsGiven = new LinkedList<String>(
				Arrays.asList(keywords));
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
		Element responseElement = (Element) currentResponse;
		if (responseElement != null)
		{
			NodeList messages = responseElement.getElementsByTagName("Message");
			Random random = new Random();
			int randomNumber = random.nextInt(messages.getLength());
			String message = messages.item(randomNumber).getTextContent();
			return message;
		}
		else
			return null;
	}

	public String getSecondBestResponseMessage()
	{
		Logger.log("Get second best response.");
		Element responseElement = (Element) secondBestResponse;
		if (responseElement != null)
		{
			NodeList messages = responseElement.getElementsByTagName("Message");
			Random random = new Random();
			int randomNumber = random.nextInt(messages.getLength());
			String message = messages.item(randomNumber).getTextContent();
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

	public LinkedList<String> getLastKeywordsGiven()
	{
		return lastKeywordsGiven;
	}
	
	public double getResponseWeight()
	{
		return responseWeight;
	}

	public double getSecondBestResponseWeight()
	{
		return secondBestWeight;
	}


	public void writeResponse(LinkedList<String> keywords, String message)
			throws Exception
	{
		Node root = doc.getFirstChild();

		Node responseNode = doc.createElement("Response");
		Node keywordsNode = doc.createElement("Keywords");
		Node messagesNode = doc.createElement("Messages");
		for (String keyword : keywords)
		{
			Node keywordNode = doc.createElement("Keyword");
			keywordNode.setTextContent(keyword);
			Attr weight = doc.createAttribute("weight");
			Random generator = new Random();
			double weightValue = 1/((double)keywords.size()+1) + (0.2) * generator.nextDouble();
			weight.setValue(Double.toString(weightValue));
			keywordNode.getAttributes().setNamedItem(weight);
			keywordsNode.appendChild(keywordNode);
		}

		Node messageNode = doc.createElement("Message");
		messageNode.setTextContent(message);
		messagesNode.appendChild(messageNode);

		responseNode.appendChild(keywordsNode);
		responseNode.appendChild(messagesNode);

		root.appendChild(responseNode);

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		// initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		String xmlString = result.getWriter().toString();

		// File file = new File(path);
		PrintWriter out = new PrintWriter(database);
		out.println(xmlString);
		out.close();

	}

	// TODO set a particular keyword's weight
	public void setResponseKeywordWeight(Node keyword, double weight)
	{

	}

	// TODO Add code to add a message to a particular response
	public void addResponseMessage(Node response, String message)
	{

	}

	
	private void getResponseWithHeighestWeight(String[] keywords)
	{

		LinkedList<Node> responses = new LinkedList<Node>();
		responses = getResponsesWithSimilarKeywords(keywords);
		getBestResponseNode(responses, keywords);
	}

	private LinkedList<Node> getResponsesWithSimilarKeywords(String[] keywords)
	{
		NodeList keywordsInDatabase = doc.getElementsByTagName("Keyword");
		LinkedList<Node> responses = new LinkedList<Node>();
		LinkedList<String> keywordsList = new LinkedList<String>(
				Arrays.asList(keywords));

		for (int i = 0; i < keywordsInDatabase.getLength(); i++)
		{
			Node currentKeyword = keywordsInDatabase.item(i);
			if (keywordsList.contains(currentKeyword.getTextContent()))
			{
				Node responseNode = currentKeyword.getParentNode()
						.getParentNode();
				//if (!responses.contains(responseNode))
					responses.add(responseNode);
			}
		}

		return responses;
	}

	private void getBestResponseNode(LinkedList<Node> responses,
			String[] keywords)
	{
		double best = 0.0;
		double current = 0.0;
		Node bestResponse = null;
		LinkedList<String> keywordsList = new LinkedList<String>(
				Arrays.asList(keywords));
		for (Node response : responses)
		{
			Element responseElement = (Element) response;
			NodeList responseKeywordNodes = responseElement
					.getElementsByTagName("Keyword");
			for (int i = 0; i < responseKeywordNodes.getLength(); i++)
			{
				Node currentKeyword = responseKeywordNodes.item(i);
				if (keywordsList.contains(currentKeyword.getTextContent()))
				{
					NamedNodeMap attributes = currentKeyword.getAttributes();
					double weight = Double.parseDouble(attributes.getNamedItem(
							"weight").getNodeValue());
					current += weight;
				}
			}

			if (current >= best)
			{
				secondBestResponse = bestResponse;
				secondBestWeight = best;
				
				best = current;
				
				
				bestResponse = response;
				currentResponse = response;
				responseWeight = best;
			}
			current = 0;

		}
	}
}
