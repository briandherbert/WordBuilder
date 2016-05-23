package com.burningaltar.learnwordsnumbers;

import java.util.ArrayList;

public class CharNode {
	public char c;
	public ArrayList <CharNode>links = new ArrayList<CharNode>();


	CharNode(char uC){
		c = uC;
	}

	public void addLink(CharNode cn){
		links.add(cn);
	}
}
