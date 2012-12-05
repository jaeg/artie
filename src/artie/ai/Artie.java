package artie.ai;

import java.io.File;
import java.util.Scanner;

public class Artie
{
	private String userInput; // Provided by user
	private String myName; // Loaded from settings file initially
	private String userName; // Provided by user
	private LearningStage questionLearning;
	private LearningStage statementLearning;
	private double trainingImpact; // Loaded from settings file

	public String getResponse(String input)
	{
		userInput = input;
		return "";
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
				if (userInput.contains(word))
				{
					return SentenceType.QUESTION;
				}

			}
		} catch (Exception ex)
		{
			System.out.println(ex);
			return SentenceType.UNDECIDED;
		}

		return SentenceType.STATEMENT;
	}

	private String[] tokenizeSentence()
	{
		String tokens[] = userInput.split("([.,!?:;\"-]|\\s)+");
		return tokens;
	}

	private String handleStatement()
	{
		return "";
	}

	private String handleQuestion()
	{
		return "";
	}

	private void train(double amount)
	{

	}

	private String learnStatement()
	{
		return "Learning Statement Engaged";
	}

	private String learnQuestion()
	{
		return "Learning Question Engaged";
	}

}
