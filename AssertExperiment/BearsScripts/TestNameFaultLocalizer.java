import java.io.*;
import java.util.*;

public class TestNameFaultLocalizer{
	public static void main(String[] args) throws IOException{
		Scanner input = new Scanner(new File(args[0]+"/neg.tests"));
		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( args[0]+"/negtestfaultloc.txt" )));
		writer.print("[");
		while(input.hasNextLine()){
			String line = input.nextLine();
			writer.print(removeTest(line));
			if(input.hasNextLine())writer.print(",");
		}
		writer.print("]");
		writer.close();
	}
	public static String removeTest(String s){
		if(s.substring(s.length()-4).equals("Test"))return s.substring(0,s.length()-4);
		else{
			int index = s.lastIndexOf(".");
			if(s.substring(index+1,index+5).equals("Test"))return s.substring(0,index+1)+s.substring(index+5);
		} 
		return "";
	}
}
