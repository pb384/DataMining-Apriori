import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AprioriOutput {
	
	public static void main(String[] args) {

		if (args == null || args.length == 0 || args[0] == null || args[0].isEmpty()) {
			System.out.println("Please pass file name");
		} 
		else {
			AprioriAlgorithm apriori = AprioriAlgorithm.getInstance();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			try {

				System.out.print("Enter the percentage of Minimum Support : ");
				int minimumSupport = Integer.parseInt(bufferedReader.readLine());

				System.out.print("Enter the percentage of Minimum Confidence : ");
				int minimumConfidence = Integer.parseInt(bufferedReader.readLine());

				apriori.toGenerateRules(minimumSupport, minimumConfidence, args[0]);

				bufferedReader.close();
			} catch (IOException e) {
				System.out.println("Please enter valid input");
			}
		}

	}

}
