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

import artie.LanguageProcessor.LanguageProcessor;
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
	// Settings
	private double trainingImpact;
	private double learningThreshold;
	private String questionDatabasePath, statementDatabasePath, confirmPath,
			questionStartersPath, learningResponsesPath;

	private Database statementDatabase;
	private Database questionDatabase;

	private String learningType;
	private String newMessage;
	private String phraseToLearn;

	public Artie()
	{
		Logger.log("ARTIE online....\n");
		userInput = "";
		lastUserInput = "";
		myName = "Artie";
		userName = "Human";
		trainingImpact = 0.1;
		learningThreshold = 0.6;
		questionDatabasePath = "questions.xml";
		statementDatabasePath = "statements.xml";
		confirmPath = "confirm.txt";
		questionStartersPath = "questionStarters.txt";
		learningResponsesPath = "learningResponses";

		try
		{
			loadSettingsXML();
		}
		catch (Exception ex)
		{
			Logger.log("No settings.xml file found.  Using default settings.");
		}

		response = "";
		lastResponse = "";
		learningStage = LearningStage.NOT_ENGAGED;
		learningType = "";
		sentenceType = SentenceType.UNDECIDED;
		statementDatabase = new Database(statementDatabasePath);
		questionDatabase = new Database(questionDatabasePath);
		
		
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

		File questionStarters = new File(questionStartersPath);

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
				if (database.getSecondBestResponseWeight() >= learningThreshold
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
		try
		{
			Logger.log("Training engaged.  Effect on keywords should be: "
					+ amount+"\n");
			String keywords[] = tokenizeSentence(lastUserInput);
			for (int i = 0; i < keywords.length; i++)
				database.applyResponseKeywordWeight(keywords[i],
						lastResponseNode, amount);
			
			Logger.log("------\n");
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
			Logger.log("Learning Step 1 - Ask for information\n");
			learningStage = LearningStage.ASK_ANSWER;
			phraseToLearn = userInput;
			return getStatementFromXML(questionType);
		}

		else if (learningStage == LearningStage.ASK_ANSWER)
		{
			Logger.log("Learning Step 2 - Ask if correct\n");
			newMessage = userInput;
			learningStage = LearningStage.ASK_IF_TRUE;
			return getStatementFromXML("Confirm");
		}

		else if (learningStage == LearningStage.ASK_IF_TRUE)
		{
			Logger.log("Learning Step 3 - Proccess user confirmation\n");
			try
			{
				Scanner scanner = new Scanner(new File(confirmPath));
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
					Logger.log("   User input confirmed\n");
					Logger.log("Original Question: "+phraseToLearn+"\n");
					
					database.writeResponse(phraseToLearn,
							newMessage);
					learningStage = LearningStage.NOT_ENGAGED;
					return getStatementFromXML("Thanks");
				}
				learningStage = LearningStage.NOT_ENGAGED;
				return getStatementFromXML("Rejection");

			}
			catch (Exception e)
			{
				Logger.log("   Error with XML\n");
				return getStatementFromXML("Fail");
			}
		}
		Logger.log("   User input rejected\n");
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
			Document doc = dBuilder.parse(new File(learningResponsesPath));
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

	private void loadSettingsXML() throws Exception
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new File("settings.xml"));
		doc.getDocumentElement().normalize();

		NodeList settings = doc.getFirstChild().getChildNodes();

		for (int i = 0; i < settings.getLength(); i++)
		{
			Node setting = settings.item(i);
			String settingName = setting.getNodeName();
			String settingContent = setting.getTextContent();
			if (settingName.equals("MyName"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				myName = settingContent;
			}
			else if (settingName.equals("LastUserName"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				userName = settingContent;
			}
			else if (settingName.equals("TrainingImpact"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				trainingImpact = Double.parseDouble(settingContent);
			}
			else if (settingName.equals("LearningThreshold"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				learningThreshold = Double.parseDouble(settingContent);
			}
			else if (settingName.equals("QuestionDatabase"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				questionDatabasePath = settingContent;
			}
			else if (settingName.equals("StatementDatabase"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				statementDatabasePath = settingContent;
			}
			else if (settingName.equals("LearningDatabase"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				learningResponsesPath = settingContent;
			}
			else if (settingName.equals("QuestionStarters"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				questionStartersPath = settingContent;
			}
			else if (settingName.equals("Confirmers"))
			{
				Logger.log(settingName +" = "+settingContent+"\n");
				confirmPath = settingContent;
			}
		}
	}

	
}
