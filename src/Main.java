import java.util.Scanner;

import artie.ai.Artie;

public class Main
{
	public static void main(String[] args)
	{
		Artie artie = new Artie();
		
		Scanner scanner = new Scanner(System.in);
		String input = "";
		System.out.println("ARTIE ONLINE...");
		
		while (!input.equals("quit"))
		{
			input = scanner.nextLine();
			System.out.println("Me: "+input);
			System.out.println("ARTIE: "+artie.getResponse(input));
		}
		
		System.out.println("ARTIE OFFLINE...");
		
		
	}

}
