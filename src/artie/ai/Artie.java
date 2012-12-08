package artie.ai;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import artie.database.Database;
import artie.utilities.Logger;

public class Artie
{
	private String userInput; // Provided by user
	private String lastUserInput;
	private String myName; // Loaded from settings file initially
	private String userName; // Provided by user
	private String response;
	private String lastResponse;
	private Node lastResponseNode;
	private LearningStage learningStage;
	private SentenceType sentenceType;
	private double trainingImpact; // Loaded from settings file
	private Database statementDatabase;
	private Database questionDatabase;
	private String learningType;
	private String newMessage;

	public Artie()
	{
		statementDatabase = new Database("statements.xml");
		questionDatabase = new Database("questions.xml");
		userInput = "";
		lastUserInput = "";
		myName = "Artie";
		userName = "Human";
		response = "";
		lastResponse = "";
		learningStage = LearningStage.NOT_ENGAGED;
		learningType = "";
		sentenceType = SentenceType.UNDECIDED;
		trainingImpact = 0.1;
	}

	public String getResponse(String input)
	{
		userInput = input;
		if (userInput.toUpperCase().equals(lastUserInput.toUpperCase()))
		{
			response = handleRepeatedInput();
		}
		else
		{
			sentenceType = analyzeUserResponse();

			if (sentenceType == SentenceType.QUESTION)
			{
				response = handleInput(questionDatabase, sentenceType);
			}
			else if (sentenceType == SentenceType.STATEMENT)
			{
				response = handleInput(statementDatabase, sentenceType);
			}
			else
			{
				response = handleUndecided();
			}

		}
		lastResponse = response;
		lastUserInput = userInput;
		return response;
	}

	public String getName()
	{
		return myName;
	}

	public String getUserName()
	{
		return userName;
	}

	private SentenceType analyzeUserResponse()
	{
		if (userInput.length() < 1)
			return SentenceType.UNDECIDED;
		// Test punctuation
		if (userInput.contains("?"))
			return SentenceType.QUESTION;

		if (userInput.contains(".") || userInput.contains("!"))
			return SentenceType.STATEMENT;

		File questionStarters = new File("questionStarters.txt");

		try
		{
			Scanner questionScanner = new Scanner(questionStarters);
			while (questionScanner.hasNext())
			{
				String word = questionScanner.nextLine();
				if (userInput.toUpperCase().contains(word))
				{
					return SentenceType.QUESTION;
				}

			}
		}
		catch (Exception ex)
		{
			System.out.println(ex);
			return SentenceType.UNDECIDED;
		}

		return SentenceType.STATEMENT;
	}

	private String[] tokenizeSentence(String sentence)
	{
		String tokens[] = sentence.split("([.,!?:;\"-]|\\s)+");
		for (int i = 0; i < tokens.length; i++)
		{
			tokens[i] = tokens[i].toUpperCase();
		}
		return tokens;
	}

	private String handleInput(Database database, SentenceType sentenceType)
	{
		String possibleResponse = "";
		Node tempResponseNode;
		if (learningStage == LearningStage.NOT_ENGAGED)
		{
			String[] keywords = tokenizeSentence(userInput);
			possibleResponse = database.getResponse(keywords);
			tempResponseNode = database.getResponseNode();

			if (possibleResponse == null)
			{
				return learn();
			}

			if (possibleResponse.equals(lastResponse))
			{
				if (database.getSecondBestResponseWeight() >= 0.60
						&& database.getSecondBestResponseMessage() != null)
				{
					possibleResponse = database.getSecondBestResponseMessage();
					tempResponseNode = database.getSecondBestResponseNode();
				}

				else
					return learn();
			}
			else if (database.getResponseWeight() < 0.60)
			{
				return learn();
			}
		}
		else
		{
			return learn();
		}

		if (lastResponseNode != null)
		{
			if (possibleResponse.contains("[+]"))
			{
				train(trainingImpact, database);
				possibleResponse = possibleResponse.replace("[+]", "");
			}
			if (possibleResponse.contains("[-]"))
			{
				train(-trainingImpact, database);
				possibleResponse = possibleResponse.replace("[-]", "");
			}
		}
		lastResponseNode = tempResponseNode;
		return possibleResponse;
	}

	private String handleUndecided()
	{
		return "Undecided!";
	}

	private String handleRepeatedInput()
	{
		return "Stop repeating yourself.";
	}

	private void train(double amount, Database database)
	{
		try{
		Logger.log("Training engaged.  Effect on keywords should be: " + amount);
		String keywords[] = tokenizeSentence(lastUserInput);
		for (int i = 0; i < keywords.length; i++)
			database.applyResponseKeywordWeight(keywords[i], lastResponseNode,
					amount);
		}
		catch (Exception ex)
		{
			getStatementFromXML("Fail");
		}
	}

	private String learn()
	{
		if (learningStage != LearningStage.NOT_ENGAGED)
		{
			if (learningType.equals("Statement"))
				return learnSomethingNew(statementDatabase, "Statements");
			else
				return learnSomethingNew(questionDatabase, "Questions");
		}

		if (sentenceType == SentenceType.STATEMENT)
		{
			learningType = "Statement";
			return learnSomethingNew(statementDatabase, "Statements");
		}
		else
		{
			learningType = "Question";
			return learnSomethingNew(questionDatabase, "Questions");
		}
	}

	private String learnSomethingNew(Database database, String questionType)
	{
		LinkedList<String> keywords = new LinkedList<String>(
				Arrays.asList(tokenizeSentence(userInput)));

		if (learningStage == LearningStage.NOT_ENGAGED)
		{
			learningStage = LearningStage.ASK_ANSWER;
			return getStatementFromXML(questionType);
		}

		else if (learningStage == LearningStage.ASK_ANSWER)
		{
			newMessage = userInput;
			learningStage = LearningStage.ASK_IF_TRUE;
			return getStatementFromXML("Confirm");
		}

		else if (learningStage == LearningStage.ASK_IF_TRUE)
		{
			try
			{
				Scanner scanner = new Scanner(new File("confirm.txt"));
				boolean confirmed = false;
				while (scanner.hasNext())
				{
					if (keywords.contains(scanner.nextLine()))
					{
						confirmed = true;
					}
				}
				scanner.close();
				if (confirmed == true)
				{
					database.writeResponse(database.getLastKeywordsGiven(),
							newMessage);
				}

				learningStage = LearningStage.NOT_ENGAGED;
				return getStatementFromXML("Thanks");
			}
			catch (Exception e)
			{
				return getStatementFromXML("Fail");
			}
		}
		return getStatementFromXML("Rejection");
	}

	private String getStatementFromXML(String baseElement)
	{
		String message = null;
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new File("learningResponses.xml"));
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName(baseElement);
			if (nodes.getLength() > 0)
			{
				Element element = (Element) nodes.item(0);
				NodeList messageNodes = element.getElementsByTagName("Message");
				Random random = new Random();
				int randomNumber = random.nextInt(messageNodes.getLength());
				message = messageNodes.item(randomNumber).getTextContent();
			}
			else
			{
				Logger.log("Bad base element in 'getStatementFromXML()'");
				message = null;
			}
		}
		catch (Exception ex)
		{
			Logger.log("Failed to access learningResponse.xml");
			message = null;

		}

		return message;
	}
}
