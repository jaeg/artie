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

import artie.LanguageProcessor.LanguageProcessor;
import artie.utilities.Logger;


public class Database
{
	private Document doc;
	private File database;
	// private Node lastResponse;
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
		// lastResponse = currentResponse;
		currentResponse = null;
		lastKeywordsGiven = new LinkedList<String>(Arrays.asList(keywords));
		getResponseWithHeighestWeight(keywords);
		return getResponseMessage();
	}

	public Node getResponseNode()
	{
		return currentResponse;
	}

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

	public Node getSecondBestResponseNode()
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

	public void writeResponse(String phraseToLearn, String message)
			throws Exception
	{
		Logger.log("Writing a new response...\n ");
		Node root = doc.getFirstChild();

		Node responseNode = doc.createElement("Response");
		Node keywordsNode = doc.createElement("Keywords");
		Node messagesNode = doc.createElement("Messages");
		Logger.log("New keywords: ");
		
		int numberOfKeywords = 0;
		LinkedList<Node> nodesToAdd = new LinkedList<Node>();
		//phraseToLearn = phraseToLearn.toUpperCase();
		//TODO condense into a single function
		//Nouns
		String nounKeywords[] = LanguageProcessor.getNouns(phraseToLearn);
		numberOfKeywords += nounKeywords.length;
		for (String keyword : nounKeywords)
		{
			Node keywordNode = doc.createElement("Keyword");
			keywordNode.setTextContent(keyword.toUpperCase());
			Attr weight = doc.createAttribute("weight");
			Random generator = new Random();
			double weightValue = 0.3 + (.2) * generator.nextDouble();
			weight.setValue(Double.toString(weightValue));
			keywordNode.getAttributes().setNamedItem(weight);
			nodesToAdd.add(keywordNode);

			Logger.log(keyword + "-" + weight + ",");
		}
		//Verbs
		String verbKeywords[] = LanguageProcessor.getVerbs(phraseToLearn);
		numberOfKeywords += verbKeywords.length;
		for (String keyword : verbKeywords)
		{
			Node keywordNode = doc.createElement("Keyword");
			keywordNode.setTextContent(keyword.toUpperCase());
			Attr weight = doc.createAttribute("weight");
			Random generator = new Random();
			double weightValue = 0.2 + (.2) * generator.nextDouble();
			weight.setValue(Double.toString(weightValue));
			keywordNode.getAttributes().setNamedItem(weight);
			nodesToAdd.add(keywordNode);

			Logger.log(keyword + "-" + weight + ",");
		}
		

		
		//Adjectives
		String adjectiveKeywords[] = LanguageProcessor.getAdjectives(phraseToLearn);
		numberOfKeywords += adjectiveKeywords.length;
		for (String keyword : adjectiveKeywords)
		{
			Node keywordNode = doc.createElement("Keyword");
			keywordNode.setTextContent(keyword.toUpperCase());
			Attr weight = doc.createAttribute("weight");
			Random generator = new Random();
			double weightValue = 0.4 + (.2) * generator.nextDouble();
			weight.setValue(Double.toString(weightValue));
			keywordNode.getAttributes().setNamedItem(weight);
			nodesToAdd.add(keywordNode);

			Logger.log(keyword + "-" + weight + ",");
		}
		
		//Adverbs
		String adverbKeywords[] = LanguageProcessor.getAdverbs(phraseToLearn);
		numberOfKeywords += adverbKeywords.length;
		for (String keyword : adverbKeywords)
		{
			Node keywordNode = doc.createElement("Keyword");
			keywordNode.setTextContent(keyword.toUpperCase());
			Attr weight = doc.createAttribute("weight");
			Random generator = new Random();
			double weightValue = 0.4 + (.2) * generator.nextDouble();
			weight.setValue(Double.toString(weightValue));
			keywordNode.getAttributes().setNamedItem(weight);
			nodesToAdd.add(keywordNode);

			Logger.log(keyword + "-" + weight + ",");
		}
		
		if (numberOfKeywords < 2 )
		{
			String keywords[] = phraseToLearn.split("([.,!?:;\"-]|\\s)+");
			for (String keyword : keywords)
			{
				Node keywordNode = doc.createElement("Keyword");
				keywordNode.setTextContent(keyword.toUpperCase());
				Attr weight = doc.createAttribute("weight");
				Random generator = new Random();
				double weightValue = 0.5 + (.2) * generator.nextDouble();
				weight.setValue(Double.toString(weightValue));
				keywordNode.getAttributes().setNamedItem(weight);
				keywordsNode.appendChild(keywordNode);

				Logger.log(keyword + "-" + weight + ",");
			}
		}
		else
		{
			for (Node keywordNode: nodesToAdd)
			{
				keywordsNode.appendChild(keywordNode);
			}
		}
		
		Logger.log("\nMessage: " + message + "\n");
		Node messageNode = doc.createElement("Message");
		messageNode.setTextContent(message);
		messagesNode.appendChild(messageNode);

		responseNode.appendChild(keywordsNode);
		responseNode.appendChild(messagesNode);

		root.appendChild(responseNode);

		saveDom();
	}

	public void applyResponseKeywordWeight(String keyword, Node response,
			double amountToChangeWeight) throws Exception
	{
		Element responseElement = (Element) response;
		NodeList keywordNodes = responseElement.getElementsByTagName("Keyword");

		for (int i = 0; i < keywordNodes.getLength(); i++)
		{
			Node keywordNode = keywordNodes.item(i);
			if (keywordNode.getTextContent().equals(keyword))
			{
				Logger.log("Adjusting the keyword: " + keyword + " by "
						+ amountToChangeWeight + "\n");
				Node weightNode = keywordNode.getAttributes().getNamedItem(
						"weight");
				Double weight = Double.parseDouble(weightNode.getNodeValue())
						+ amountToChangeWeight;
				weightNode.setNodeValue(weight.toString());
			}
		}
		saveDom();
	}

	// TODO Add code to add a message to a particular response
	public void addResponseMessage(Node response, String message)
	{

	}

	private void saveDom() throws Exception
	{
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

	private void getResponseWithHeighestWeight(String[] keywords)
	{
		Logger.log("*********\nAsking for response from database...\n");
		LinkedList<Node> responses = new LinkedList<Node>();
		responses = getResponsesWithSimilarKeywords(keywords);
		getBestResponseNode(responses, keywords);
		Logger.log("--------\n");
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
				if (!responses.contains(responseNode))
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
			Logger.log("--------\n");
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
					Logger.log(currentKeyword.getTextContent() + "-" + weight
							+ "\n");
				}
			}

			if (current >= best)
			{
				Logger.log("Better response found.\n");
				secondBestResponse = bestResponse;
				secondBestWeight = best;

				best = current;

				bestResponse = response;
				currentResponse = response;
				responseWeight = best;
			}

			Logger.log("Weight Total = " + current + "\n");
			current = 0;
		}
	}
	
	
	private static void outputArray(String[] array)
	{
		for (int i=0; i<array.length;i++)
		{
			System.out.println(array[i]);
		}
	}
}
