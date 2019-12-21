package org.junit;

import java.util.*;

import org.junit.runner.notification.Failure;

import java.io.*;

public class DataPack {
    
    public static List<DataPack> data = new ArrayList<DataPack>();
    
    public static void clear() {
        data = new ArrayList<DataPack>();
    }
    
    public static void pass(int d) {
        data.add(new DataPack(getStackTrace(),1.0,d));
    }
    
    public static void fail(int d) {
        data.add(new DataPack(getStackTrace(),0.0,d));
    }
    
    public static void checklong(long a, long b,int d) {
        data.add(new DataPack(getStackTrace(),Math.exp(0-Math.abs(a-b)),d));
    }
    
    public static void checkdouble(double a, double b,int d) {
	if(Double.isNaN(a) || Double.isNaN(b))data.add(new DataPack(getStackTrace(),0,d));
        else data.add(new DataPack(getStackTrace(),Math.exp(0-Math.abs(a-b)),d));
    }
    
    public static void checkString(String a, String b, int d){
        data.add(new DataPack(getStackTrace(),Math.exp(0-minedit(a,b)),d));
    }
    
    public static void dataDump() {
        for(DataPack dp : data) {
	    System.out.println("Key: "+dp.ste[dp.depth+2]);
            if(dp.ste[dp.depth+2].toString().equals("org.junit.runners.model.TestClass.getOnlyConstructor(TestClass.java:205)"))continue;
            System.out.println(dp.toString());
        }
    }
    
    public static void dataDump2(List<Failure> list) {
        double finalscore = 0;
        double finalscore2 = 0;
        if(data.size()>0 && list.size() == 0 ) {
            Map<String, Double> scoremap = new TreeMap<String, Double>();
            for(DataPack dp : data) {
		        if(dp.ste[dp.depth+2].toString().equals("org.junit.runners.model.TestClass.getOnlyConstructor(TestClass.java:205)"))continue;
                String key = (dp.ste[dp.depth+3].toString()+dp.ste[dp.depth+2].toString())+(dp.ste[dp.depth+1].toString());
                if(scoremap.containsKey(key)) {
                    double prev = scoremap.get(key);
                    if(dp.score<prev)scoremap.put(key, dp.score);
                }else {
                    scoremap.put(key, dp.score);
                }
            }
            for(String key : scoremap.keySet()) {
		    if(!Double.isNaN(scoremap.get(key)))
                finalscore += scoremap.get(key);
		        if(scoremap.get(key) == 1.0) finalscore2 += 1.0;
            }
            //finalscore = finalscore /scoremap.size();
        }
        ArrayList<Double> finalscores = new ArrayList<Double>();
        finalscores.add(finalscore);
        finalscores.add(finalscore2);
        try {
          ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("Temp.arr"));
          oos.writeObject(finalscores);
          oos.flush();
          oos.close();
        }catch(Throwable e) {e.printStackTrace();}
    }
    
    
    
    public static String testparser(String testa) {
        int start = testa.indexOf("(");
        int end = testa.lastIndexOf(")");
        return testa.substring(start+1,end);
    }
    
    @Override
    public String toString() {
        return ste[depth+2].toString() + " Recorded AntiDistance of " + score;
    }
    
    public static StackTraceElement[] getStackTrace() {
        return (new Throwable()).getStackTrace();
    }
    
    public StackTraceElement[] ste = null;
    public double score = -1;
    public int depth;
    public DataPack(StackTraceElement[] a, double b, int d) {
        ste=a;
        score=b;
        depth=d;
    }
    
    public static int minedit(String a, String b) {
        int[][] mem = new int[a.length()+1][b.length()+1];
        for(int i = 0; i < a.length(); i++) {
            for(int j = 0; j < b.length(); j++) {
                mem[i][j]=-1;
            }
        }
        return mineditHelper(a,b,0,0,mem);
    }
    
    public static int mineditHelper(String sa, String sb, int a, int b, int[][] mem) {
        
        if(mem[a][b]>=0)return mem[a][b];
        
        if(a==sa.length()) {
            mem[a][b]=sb.length()-b;
        }
        else if(b==sb.length()) {
            mem[a][b]=sa.length()-a;
        }
        else {
            if(sa.charAt(a)==sa.charAt(b))mem[a][b]= mineditHelper(sa,sb,a+1,b+1,mem);
            else {
                int pusha = mineditHelper(sa,sb, a+1,b,mem);
                int pushb = mineditHelper(sa,sb,a,b+1,mem);
                if(pusha>pushb)mem[a][b]=pushb+1;
                else mem[a][b]=pusha+1;
            }
        }
        return mem[a][b];
        
    }
}
