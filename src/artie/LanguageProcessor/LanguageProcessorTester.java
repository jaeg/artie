package artie.LanguageProcessor;


public class LanguageProcessorTester
{
	public static void main(String[] args)
	{
		String sentence = "I like hats.";
		System.out.println("Here is the string split into sentences: ");
		outputArray(LanguageProcessor.getSentences(sentence));
		
		System.out.println("\nHere are the tokens for this sentence: ");
		outputArray(LanguageProcessor.tokenizeSentence(sentence));
		
		System.out.println("\nHere are the names from the sentence:");
		LanguageProcessor.getNames(sentence);
		
		System.out.println("\nHere is the sentence after it has been tagged: ");
		outputArray(LanguageProcessor.tagSentencePOS(sentence));
	
		System.out.println("\nHere are all the verbs in the sentence: ");
		outputArray(LanguageProcessor.getVerbs(sentence));
		
		System.out.println("\nHere are all the nouns in the sentence: ");
		outputArray(LanguageProcessor.getNouns(sentence));
		
		System.out.println("\nHere are all the adjectives in the sentence: ");
		outputArray(LanguageProcessor.getAdjectives(sentence));
		
		System.out.println("\nHere are all the adverbs in the sentence: ");
		outputArray(LanguageProcessor.getAdverbs(sentence));
	}
	
	private static void outputArray(String[] array)
	{
		for (int i=0; i<array.length;i++)
		{
			System.out.println(array[i]);
		}
	}
}
