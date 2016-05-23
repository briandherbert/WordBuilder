package com.burningaltar.learnwordsnumbers;

import android.content.Context;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LexRunner {
	public static final int LEX_ID = R.raw.commonmineupper;
	public static final int LEX_SIZE_IN_BYTES = 354106;

	static byte[] byteBulk = new byte[LEX_SIZE_IN_BYTES];

	public static final char CHAR_ENDWORD = '.';

	CharNode root=new CharNode('@');
	CharNode currentNode;
	Context context=null;

	public static final int[] WORDFILE_RES_IDS= {
		R.raw.awords,
		R.raw.bwords,
		R.raw.cwords,
		R.raw.dwords,
		R.raw.ewords,
		R.raw.fwords,
		R.raw.gwords,
		R.raw.hwords,
		R.raw.iwords,
		R.raw.jkwords,
		R.raw.jkwords,
		R.raw.lwords,
		R.raw.mwords,
		R.raw.nwords,
		R.raw.owords,
		R.raw.pqwords,
		R.raw.pqwords,
		R.raw.rwords,
		R.raw.swords,
		R.raw.twords,
		R.raw.uwords,
		R.raw.vwwords,
		R.raw.vwwords,
		R.raw.xyzwords,
		R.raw.xyzwords,
		R.raw.xyzwords};

	public static short getIdFromChar(char c){
		if(c=='A') return 0;
		else if(c=='B') return 1;
		else if(c=='C') return 2;
		else if(c=='D') return 3;
		else if(c=='E') return 4;
		else if(c=='F') return 5;
		else if(c=='G') return 6;
		else if(c=='H') return 7;
		else if(c=='I') return 8;
		else if(c=='J') return 9;
		else if(c=='K') return 10;
		else if(c=='L') return 11;
		else if(c=='M') return 12;
		else if(c=='N') return 13;
		else if(c=='O') return 14;
		else if(c=='P') return 15;
		else if(c=='Q') return 16;
		else if(c=='R') return 17;
		else if(c=='S') return 18;
		else if(c=='T') return 19;
		else if(c=='U') return 20;
		else if(c=='V') return 21;
		else if(c=='W') return 22;
		else if(c=='X') return 23;
		else if(c=='Y') return 24;
		else if(c=='Z') return 25;
		else if(c==' ') return 26;
		else if(c==CHAR_ENDWORD) return 27;
		return -1;
	}

	public LexRunner(Context uContext)
	{
		context = uContext;
		long startTime = System.currentTimeMillis();
		currentNode = root;

		System.out.println("Time to package into tree: " + (System.currentTimeMillis() - startTime));
	}

	public char[] getNextCharsEasy(String s) throws Exception{
		System.out.println("Looking for letters after " + s + " has context " + context);
		InputStream istream = context.getResources().openRawResource(WORDFILE_RES_IDS[getIdFromChar(s.charAt(0))]);
		//FileInputStream fstream = new FileInputStream("c:/commonmineupper.txt");
		DataInputStream in = new DataInputStream(istream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine="";
		String letters = "";

		int idx = 0;
		while ((strLine = br.readLine()) != null && idx<s.length())   {
			if(strLine.length() < s.length())
				continue;
			int compare = s.compareTo(strLine.substring(0,s.length()));
			if(compare > 0)
				continue;
			else if(compare ==0){
				if(strLine.length() == s.length())
					letters += '.';
				else if(letters.length()==0 || strLine.charAt(s.length())!=letters.charAt(letters.length()-1))
					letters += strLine.charAt(s.length());
			}else
				break;
		}
		istream.close();
		br.close();
		return letters.toCharArray();
	}


	public void populateCharNodesCoded(Context context) throws Exception{
		InputStream istream = context.getResources().openRawResource(LEX_ID);
		istream.read(byteBulk);

		CharNode[] currentNodes = new CharNode[15];

		int i= 0;
		while(i<byteBulk.length){
			CharNode cn = new CharNode((char)(byteBulk[i+1]));
			currentNodes[byteBulk[i]] = cn;
			if(byteBulk[i]==0)
				root.addLink(cn);
			else{
				currentNodes[byteBulk[i]-1].addLink(cn);
			}
			//System.out.println("putting "+byteBulk[i+1] + " at "+ byteBulk[i]);
			i+=2;
		}
	}

	public char[] getNextChars(char c){
    	for(CharNode cn:currentNode.links){
    		if(c == cn.c){
    			currentNode = cn;
    			break;
    		}else if(c < cn.c)
    			break;
    	}

    	char[] nextChars = new char[currentNode.links.size()];
        System.out.print("Possible following letters: ");
        for(int i=0; i<currentNode.links.size();i++)
        	nextChars[i] = currentNode.links.get(i).c;
        return nextChars;
	}

	public char[] getNextChars(String s){
        CharNode trail = root;
        char [] nextChars;
        for(int i=0; i<s.length();i++){
        	char c = s.charAt(i);
        	for(CharNode cn:trail.links){
        		if(c == cn.c){
        			trail = cn;
        			break;
        		}else if(c < cn.c)
        			break;
        	}
        }
        nextChars = new char[trail.links.size()];
        for(int i =0;i<trail.links.size();i++)
        	nextChars[i] = trail.links.get(i).c;
        return nextChars;
	}

	public void reset(){
		currentNode = root;
	}
}
