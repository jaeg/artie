package artie.ai;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

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
	private LearningStage questionLearning;
	private LearningStage statementLearning;
	private SentenceType sentenceType;
	private double trainingImpact; // Loaded from settings file
	private Database statementDatabase;
	private Database questionDatabase;
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
		questionLearning = LearningStage.NOT_ENGAGED;
		statementLearning = LearningStage.NOT_ENGAGED;
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

	private String[] tokenizeSentence()
	{
		String tokens[] = userInput.split("([.,!?:;\"-]|\\s)+");
		for (int i = 0; i < tokens.length; i++)
		{
			tokens[i] = tokens[i].toUpperCase();
		}
		return tokens;
	}

	private String handleInput(Database database, SentenceType sentenceType)
	{
		String possibleResponse = "";
		if (questionLearning == LearningStage.NOT_ENGAGED
				&& statementLearning == LearningStage.NOT_ENGAGED)
		{
			String[] keywords = tokenizeSentence();
			possibleResponse = database.getResponse(keywords);

			if (possibleResponse == null)
			{
				return learn();
			}

			if (possibleResponse.equals(lastResponse))
			{
				if (database.getSecondBestResponseWeight() > 0.60
						&& database.getSecondBestResponseMessage() != null)
					possibleResponse = database.getSecondBestResponseMessage();
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

		if (possibleResponse.contains("[+]"))
		{
			train(trainingImpact);
			possibleResponse.replaceAll("[+]", "");
		}
		if (possibleResponse.contains("[-]"))
		{
			train(-trainingImpact);
			possibleResponse.replaceAll("[-]", "");
		}
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

	private void train(double amount)
	{
		Logger.Log("Training engaged.  Effect on keywords should be: " + amount);
	}

	private String learn()
	{
		if (statementLearning != LearningStage.NOT_ENGAGED)
			return learnStatement();
		if (questionLearning != LearningStage.NOT_ENGAGED)
			return learnQuestion();
		
		if (sentenceType == SentenceType.STATEMENT)
			return learnStatement();
		else
			return learnQuestion();
	}

	private String learnStatement()
	{
		LinkedList<String> keywords = new LinkedList<String>(
				Arrays.asList(tokenizeSentence()));
		if (statementLearning == LearningStage.NOT_ENGAGED)
		{
			statementLearning = LearningStage.ASK_ANSWER;
			return "I don't know much about that.  Can you tell me more?";
		}

		else if (statementLearning == LearningStage.ASK_ANSWER)
		{
			newMessage = userInput;
			statementLearning = LearningStage.ASK_IF_TRUE;
			return "Is that true?";
		}

		else if (statementLearning == LearningStage.ASK_IF_TRUE)
		{

			if (keywords.contains("YES") || keywords.contains("YAH")
					|| keywords.contains("YEP") || keywords.contains("SURE"))
			{
				try
				{
					statementDatabase.writeResponse(
							statementDatabase.getLastKeywordsGiven(),
							newMessage);
				}
				catch (Exception e)
				{
					return "I failed to remember that...";
				}
				statementLearning = LearningStage.NOT_ENGAGED;
				return "That's for informing me!";
			}
		}
		return "Maybe some other time then";

	}

	private String learnQuestion()
	{
		LinkedList<String> keywords = new LinkedList<String>(
				Arrays.asList(tokenizeSentence()));
		if (questionLearning == LearningStage.NOT_ENGAGED)
		{
			Logger.Log("Test");
			questionLearning = LearningStage.ASK_ANSWER;
			return "I don't have an answer for that, can you tell me?";
		}

		else if (questionLearning == LearningStage.ASK_ANSWER)
		{
			newMessage = userInput;
			questionLearning = LearningStage.ASK_IF_TRUE;
			return "Is that true?";
		}

		else if (questionLearning == LearningStage.ASK_IF_TRUE)
		{

			if (keywords.contains("YES") || keywords.contains("YAH")
					|| keywords.contains("YEP") || keywords.contains("SURE"))
			{
				try
				{
					questionDatabase.writeResponse(
							questionDatabase.getLastKeywordsGiven(),
							newMessage);
				}
				catch (Exception e)
				{
					return "I failed to remember that...";
				}
				questionLearning = LearningStage.NOT_ENGAGED;
				return "That's one for the books.";
			}
		}
		return "Maybe some other time then";
	}

}
