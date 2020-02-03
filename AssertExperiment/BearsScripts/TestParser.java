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
			if(line.indexOf(" T E S T S")>=0)atTest=true;
			if(atTest){
				if(line.length()>=8 && line.indexOf("Running ")>=0 && line.substring(line.indexOf("Running ")+8).indexOf(" ")<0){
					tests.add(line.substring(line.indexOf("Running ")+8));
					String line2 = input.nextLine();
					while(line2.indexOf("Tests run: ")<0) line2 = input.nextLine();
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
