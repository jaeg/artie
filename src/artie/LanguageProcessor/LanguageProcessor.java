package artie.LanguageProcessor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class LanguageProcessor
{

	private static String lastSentence = "";
	private static String[] lastSentencePOS;
	
	
	private LanguageProcessor()
	{

	}

	
	public static String[] getVerbs(String sentence)
	{
		LinkedList<String> verbs = new LinkedList<String>();
		String tokens[] = tokenizeSentence(sentence);
		String taggedTokens[] = tagSentencePOS(sentence);
		String verbTags[] =
		{ "VBN", "VBG", "VBD", "VB" };
		for (int i = 0; i < taggedTokens.length; i++)
		{
			for (int j = 0; j < verbTags.length; j++)
			{
				if (taggedTokens[i].contains(verbTags[j]))
				{
					verbs.add(tokens[i]);
					taggedTokens[i] = "";
				}
			}
		}
		return verbs.toArray(new String[verbs.size()]);
	}

	public static String[] getNames(String sentence)
	{
		InputStream modelIn = null;
		TokenNameFinderModel model = null;
		try
		{
			modelIn = new FileInputStream("Models/en-ner-person.bin");
			model = new TokenNameFinderModel(modelIn);
			if (modelIn != null)
			{
				modelIn.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		NameFinderME nameFinder = new NameFinderME(model);
		String[] sentenceTokens = tokenizeSentence(sentence);
		Span nameSpans[] = nameFinder.find(sentenceTokens);
		// LinkedList<String> names = new LinkedList<String>();
		for (int i = 0; i < nameSpans.length; i++)
		{
			for (int j = nameSpans[i].getStart(); j < nameSpans[i].getEnd(); j++)
				System.out.println(sentenceTokens[j]);

		}
		return sentenceTokens;
	}

	public static String[] getNouns(String sentence)
	{
		LinkedList<String> nouns = new LinkedList<String>();
		String tokens[] = tokenizeSentence(sentence);
		String taggedTokens[] = tagSentencePOS(sentence);
		String nounTags[] =
		{ "NNPS", "NNP", "NNS", "NN" };
		for (int i = 0; i < taggedTokens.length; i++)
		{
			for (int j = 0; j < nounTags.length; j++)
			{
				if (taggedTokens[i].contains(nounTags[j]))
				{
					nouns.add(tokens[i]);
					taggedTokens[i]="";
				}
			}
		}
		return nouns.toArray(new String[nouns.size()]);
	}

	public static String[] getAdjectives(String sentence)
	{
		LinkedList<String> adjective = new LinkedList<String>();
		String tokens[] = tokenizeSentence(sentence);
		String taggedTokens[] = tagSentencePOS(sentence);
		String adjectiveTags[] =
		{ "JJS","JJR","JJ"};
		for (int i = 0; i < taggedTokens.length; i++)
		{
			for (int j = 0; j < adjectiveTags.length; j++)
			{
				if (taggedTokens[i].contains(adjectiveTags[j]))
				{
					adjective.add(tokens[i]);
					taggedTokens[i]="";
				}
			}
		}
		return adjective.toArray(new String[adjective.size()]);
	}
	
	public static String[] getAdverbs(String sentence)
	{
		LinkedList<String> adverbs = new LinkedList<String>();
		String tokens[] = tokenizeSentence(sentence);
		String taggedTokens[] = tagSentencePOS(sentence);
		String adverbTags[] =
		{"RBS","RBR","RB" };
		for (int i = 0; i < taggedTokens.length; i++)
		{
			for (int j = 0; j < adverbTags.length; j++)
			{
				if (taggedTokens[i].contains(adverbTags[j]))
				{
					adverbs.add(tokens[i]);
					taggedTokens[i]="";
				}
			}
		}
		return adverbs.toArray(new String[adverbs.size()]);
	}
	//TODO finish getAdjectivesForNoun
	public static String[] getAdjectivesForNoun(String sentence, String noun)
	{
		return new String[0];
	}

	//TODO Finish getCompleteSubject
	// Or whatever “Lazy dog” is considered in “The lazy dog sleeps all day”
	public static String[] getCompleteSubject(String sentence)
	{
		return new String[0];
	}

	//TODO Finish getCompleteAction
	public static String[] getComplete(String sentence) // ”Sleeps all day”
	{
		return new String[0];
	}

	public static String[] getSentences(String sentence)
	{
		InputStream modelIn = null;
		SentenceModel model = null;
		try
		{
			modelIn = new FileInputStream("Models/en-sent.bin");
			model = new SentenceModel(modelIn);

			if (modelIn != null)
			{
				modelIn.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		String sentences[] = sentenceDetector.sentDetect(sentence);

		return sentences;
	}

	public static String[] tokenizeSentence(String sentence)
	{
		InputStream modelIn = null;
		TokenizerModel model = null;
		try
		{
			modelIn = new FileInputStream("Models/en-token.bin");
			model = new TokenizerModel(modelIn);
			if (modelIn != null)
			{
				modelIn.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		Tokenizer tokenizer = new TokenizerME(model);
		String tokens[] = tokenizer.tokenize(sentence);

		return tokens;

	}

	public static String[] tagSentencePOS(String sentence)
	{
		
		if (lastSentence.equals(sentence))
		{
			return lastSentencePOS;
		}
		
		InputStream modelIn = null;
		POSModel model = null;
		try
		{
			modelIn = new FileInputStream("Models/en-pos-maxent.bin");
			model = new POSModel(modelIn);
			if (modelIn != null)
			{
				modelIn.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		POSTaggerME tagger = new POSTaggerME(model);
		lastSentence = sentence;
		String tags[] = tagger.tag(tokenizeSentence(sentence));
		lastSentencePOS = tags;
		return tags;
	}
}
