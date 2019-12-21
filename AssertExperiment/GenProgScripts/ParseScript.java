import java.util.*;
import java.io.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
public class ParseScript{
  public static void main(String[] args) throws IOException{
    Scanner input = new Scanner(new File(args[0]+"/diff.diff"));
    ArrayList<String> fulltext = new ArrayList<String>();
    while(input.hasNextLine()){
      fulltext.add(input.nextLine());
    }
    
    boolean pass = false;
    ArrayList<ParseLineObject> changes = new ArrayList<ParseLineObject>();
    Set<Integer> validSet = new TreeSet<Integer>();
    
    String filename = null;
    while(!pass){
    changes = new ArrayList<ParseLineObject>();
    validSet = new TreeSet<Integer>();
    validSet.add(0);
    Scanner inp = new Scanner(System.in);
    filename = null;
    int linenum = -1;
    int priocounter = 0;
    int delcounter = -1;
    for(int i = 0; i < fulltext.size(); i++){
      String line = fulltext.get(i);
      if(line.length()> 7 && line.substring(0,7).equals("diff -r")){
        String[] splitted = line.split(" ");
        filename = splitted[2];
      }
      else if(line.length()>= 3 && line.substring(0,3).equals("---")) continue;
      else if(line.length()>= 7 && line.substring(0,7).equals("Only in")) continue;
      else if(line.length()> 0 && line.charAt(0)=='<'){
        ParseLineObject plo = new ParseLineObject(filename, linenum+delcounter, priocounter, true, line.substring(2));
        delcounter++;
        priocounter++;
        plo.prt();
        plo.group = inp.nextInt();
        validSet.add(plo.group);
        changes.add(plo);
      }
      else if(line.charAt(0)=='>'){
        ParseLineObject plo = new ParseLineObject(filename, linenum, priocounter, false, line.substring(2));
        priocounter++;
        plo.prt();
        plo.group = inp.nextInt();
        validSet.add(plo.group);
        changes.add(plo);
      }
      else{
        int firsta = line.indexOf("a");
        int firstd = line.indexOf("d");
        int firstc = line.indexOf("c");
        int firstcom = line.indexOf(",");
        if(firsta < 0)firsta = 100000000;
        if(firstd < 0)firstd = 100000000;
        if(firstc < 0)firstc = 100000000;
        if(firstcom < 0)firstcom = 100000000;
        linenum = Integer.parseInt(line.substring(0,min4(firsta,firstd,firstc,firstcom)));
        delcounter = 0;
      }
    }
    
      for(int i: validSet){
      System.out.println(i+":");
      for(ParseLineObject plo : changes){
        if(plo.group==i)plo.prt();
      }
    }
    
    System.out.println("0 for pass, 1 for fail:");
    int p = inp.nextInt();
    if(p==0)pass=true;
    }
    
    Set<PartialEdits> powerset = new HashSet<PartialEdits>();
    powerpop(powerset, changes,validSet);
    
    /*
    int counter = 0;
    for(Set<ParseLineObject> subset : powerset){
      System.out.println("This is subset "+counter+":");
      for(ParseLineObject plo : subset){
        plo.prt();
      }
      counter++;
    }
    */
    
    

    Scanner input2 = new Scanner(new File(filename));
    ArrayList<String> edittext = new ArrayList<String>();
    while(input2.hasNextLine()){
      edittext.add(input2.nextLine());
    }
    
    for(PartialEdits pe : powerset){
      String nums = "";
      for(int a : pe.patches){
        nums += a;
      }
      makebugcopy(nums, args[0]);
      int splitindex = filename.indexOf("bugorig");
      String newname = filename.substring(0,splitindex) + "bug"+nums+filename.substring(splitindex+7);
      PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter( newname )));
      for(int i = 0; i < edittext.size(); i++){
        boolean del = false;
        for(ParseLineObject plo : pe.edits){
          if(plo.del && plo.linenum == i+1){
            del=true;
            break;
          }
        }
        if(!del)writer.println(edittext.get(i));
        PriorityQueue<ParseLineObject> pq = new PriorityQueue<ParseLineObject>();
        for(ParseLineObject plo : pe.edits){
          if(plo.del == false && plo.linenum == i+1){
            pq.add(plo);
          }
        }
        while(!pq.isEmpty()){
          ParseLineObject plo = pq.poll();
          writer.println(plo.line);
        }
      }
      writer.close();
      evalpartialrepair(nums, args[0], args[1], args[2], args[3]);
    }
    
  }
  public static void evalpartialrepair(String nums, String folder, String gp4jhome, String zemppath, String runcommand){
    DefaultExecutor executor = new DefaultExecutor();
    try{
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      executor.setExitValue(0);
      executor.setStreamHandler(new PumpStreamHandler(out));
      try{executor.execute(CommandLine.parse("rm -rf "+zemppath));}catch(Throwable e){e.printStackTrace();}
      executor.execute(CommandLine.parse("mv "+folder+"/bug"+nums+" "+zemppath));
      //System.exit(0);
      executor.setWorkingDirectory(new File(zemppath));
      //executor.execute(CommandLine.parse("cat "+zemppath+"/src/java/org/apache/commons/lang/text/StrBuilder.java"));
      executor.execute(CommandLine.parse("sh runCompile.sh"));
      //executor.execute(CommandLine.parse("cat "+zemppath+"/src/java/org/apache/commons/lang/text/StrBuilder.java"));
      //System.out.println("timeout -sHUP 200h java -ea -Dlog4j.configurationFile=file:"+gp4jhome+"/src/log4j.properties -Dfile.encoding=UTF-8 -classpath "+gp4jhome+"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar clegoues.genprog4java.main.Main "+zemppath+"/defects4j.config");
	//System.exit(0);
      executor.execute(CommandLine.parse("timeout -sHUP 200h java -ea -Dlog4j.configurationFile=file:"+gp4jhome+"/src/log4j.properties -Dfile.encoding=UTF-8 -classpath "+gp4jhome+"/target/uber-GenProg4Java-0.0.1-SNAPSHOT.jar clegoues.genprog4java.main.Main "+zemppath+"/defects4j.config"));
  //    executor.execute(CommandLine.parse("cat "+zemppath+"/src/main/java/org/apache/commons/lang3/math/NumberUtils.java"));
      //executor.execute(CommandLine.parse("echo $(pwd)"));
//	System.out.println(runcommand);
  //    executor.execute(CommandLine.parse(runcommand));
      //executor.execute(CommandLine.parse("cat "+zemppath+"/src/java/org/apache/commons/lang/text/StrBuilder.java"));
      PrintWriter writer = new PrintWriter(new FileWriter(new File(folder+"/results/"+nums+".out")));
      out.flush();
      writer.println(out.toString());
      writer.close();
    }catch(Throwable e){System.out.println(e.toString());}
  }
  public static void makebugcopy(String nums, String folder){
    DefaultExecutor executor = new DefaultExecutor();
    try{
      executor.execute(CommandLine.parse("cp -r "+folder+"/bugorig "+folder+"/bug"+nums));
    }catch(Throwable e){System.out.println(e.toString());}
  }
  public static void powerpop(Set<PartialEdits> set, ArrayList<ParseLineObject> changes, Set<Integer> valid){
    int[] pows = new int[valid.size()];
    pows[0]=1;
    for(int i =1; i < valid.size(); i++){
      pows[i] = pows[i-1]*2;
    }
    for(int i = 1; i < pows[valid.size()-1]-1; i++){
      Set<Integer> tmpvalid = new TreeSet<Integer>();
      Set<ParseLineObject> subset = new HashSet<ParseLineObject>();
      tmpvalid.add(0);
      int counter = 0;
      for(int a : valid){
        if(a==0)continue;
        if((i/pows[counter])%2==1)tmpvalid.add(a);
        counter ++;
      }
      for(ParseLineObject plo : changes){
        if(tmpvalid.contains(plo.group)){
           subset.add(plo);
        }
      }
      set.add(new PartialEdits(subset,tmpvalid));
    }
  }
  public static int min4(int a, int b, int c, int d){
    if(a<=b && a<=c && a<=d ) return a;
    if(b<=a && b<=c && b<=d ) return b;
    if(c<=a && c<=b && c<=d ) return c;
    return d;
  }
}

class ParseLineObject implements Comparable<ParseLineObject>{
  public String filename;
  public int linenum;
  public int prio;
  public boolean del;
  public String line;
  public int group = 0;
  public ParseLineObject(String fn, int ln, int pr, boolean d, String li){
    filename = fn;
    linenum = ln;
    prio = pr;
    del = d;
    line = li;
  }
  public void prt(){
    char delc = '>';
    if(del)delc='<';
    System.out.println(linenum+" "+delc+" "+line);
  }
  public int compareTo(ParseLineObject plo){
    return prio - plo.prio;
  }
}

class PartialEdits{
  public Set<ParseLineObject> edits;
  public Set<Integer> patches;
  public PartialEdits(Set<ParseLineObject> plo,Set<Integer> pa){
    edits=plo; patches = pa;
  }
}
