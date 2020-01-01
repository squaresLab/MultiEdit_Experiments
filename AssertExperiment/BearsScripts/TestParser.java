import java.io.*;
import java.util.*;

//args[0]: input repairnote maven test log
//args[1]: directory to store pos and neg

public class TestParser{
	public static void main(String[] args) throws IOException{
		Scanner input = new Scanner(new File(args[0]));
		boolean atTest = false;
		ArrayList<String> tests = new ArrayList<String>();
		ArrayList<Boolean> testpass = new ArrayList<Boolean>();
		while(input.hasNextLine()){
			String line = input.nextLine();
			if(line.equals(" T E S T S"))atTest=true;
			if(atTest){
				if(line.length()>=8 && line.substring(0,8).equals("Running ")){
					tests.add(line.substring(8));
					String line2 = input.nextLine();
					while(line2.length() < 11 || !line2.substring(0,11).equals("Tests run: ")) line2 = input.nextLine();
					if(line2.indexOf("Failures: 0")>=0 && line2.indexOf("Errors: 0")>=0)testpass.add(true);else testpass.add(false);
				}
			}
		}
		PrintWriter writer1 = new PrintWriter( new BufferedWriter( new FileWriter( args[1]+"/pos.tests" )));
		PrintWriter writer2 = new PrintWriter( new BufferedWriter( new FileWriter( args[1]+"/neg.tests" )));
                for(int i = 0; i < tests.size(); i++){
			if(testpass.get(i))writer1.println(tests.get(i));else writer2.println(tests.get(i));
		}
                writer1.close();
		writer2.close();
	}
}
