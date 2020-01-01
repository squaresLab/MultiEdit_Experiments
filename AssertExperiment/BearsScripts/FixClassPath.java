import java.io.*;
import java.util.*;

public class FixClassPath {

	public static void main (String [] args) throws IOException{
		Scanner input = new Scanner(new File(args[0]));
                String line = input.nextLine();
                String[] paths = line.split(":");
		String outstr = ".";
                for(String s : paths){
			int index = s.indexOf("/.m2");
			outstr += ":"+args[1]+s.substring(index+4);
		}
		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( args[0]+"a" )));
		writer.println(outstr);
		writer.close();

	}

}


