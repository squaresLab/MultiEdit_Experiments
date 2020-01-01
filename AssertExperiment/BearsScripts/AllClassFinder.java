import java.io.*;
import java.util.*;

public class AllClassFinder{
public static ArrayList<String> classes = new ArrayList<String>();
public static void main(String[] args)throws IOException{
File root = new File(args[0]);
recursive(root,"");
PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( args[1])));
for(String s : classes){
writer.println(s.substring(s.indexOf(".")+1));
}
writer.close();
}
public static void recursive(File file, String stack){
if(file.isDirectory()){
String newstack = stack+file.getName()+".";
for(File f: file.listFiles()){
recursive(f, newstack);
}
}
else{
String name = file.getName();
if(name.length()>=5&&name.substring(name.length()-5).equals(".java"))
classes.add(stack+name.substring(0,name.length()-5));
}
}
}

