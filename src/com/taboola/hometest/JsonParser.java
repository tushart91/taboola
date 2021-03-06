package com.taboola.hometest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class JsonParser {
    
	private static enum Token {
		NULL, OBJECT_OPEN, OBJECT_CLOSE, ARRAY_OPEN, ARRAY_CLOSE,
		COMMA, STRING, NUMBER, COLON, TRUE, FALSE, NONE
	}
	
	private static final HashSet<Character> SKIPCHARS = 
			new HashSet<Character>(Arrays.asList(' ', '\t', '\n', '\r'));
	
	private static final HashSet<Character> INT_CHARS = 
			new HashSet<Character>(Arrays.asList('0','1','2','3','4','5',
					'6','7','8','9','+','-'));
	
	private static final HashSet<Character> FLOAT_CHARS = 
			new HashSet<Character>(Arrays.asList('.','e','E'));
	
	private static int index = 0;
	
//	public static void main(String[] args) {
//		BufferedReader br = null;
//		try {
//			StringBuilder str = new StringBuilder();
//			br = new BufferedReader(new FileReader("input1.json"));
//			String line;
//			while((line = br.readLine()) != null) {
//				str.append(line);
//			}
//			System.out.println(((HashMap)parse(str.toString())).get("title"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (br != null)br.close();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
	
	private static Object parse(String inputJson) {
		
		// handle special cases initially
		if (inputJson == null)
			return null;
		if (inputJson.equals(""))
			return null;
		
		//The outermost Json string is a value of key-value pair with no key
		char[] charArray = inputJson.toCharArray();
		Object json_object = parseValue(charArray);
		return json_object;
	}
	
	private static Object parseValue(char[] inputJson) {
		Token token = nextToken(inputJson);
		switch (token) {
			case STRING:
				return parseString(inputJson);
			case NUMBER:
				return parseNumber(inputJson);
			case OBJECT_OPEN:
				return parseObject(inputJson);
			case ARRAY_OPEN:
				return parseArray(inputJson);
			case TRUE:
				return true;
			case FALSE:
				return false;
			case NULL:
				return null;
			default: case NONE:
				break;
		}	
		
		return null;
	}
	
	private static Object parseNumber(char[] inputJson) {
		int lastIndex = index;
		boolean double_flag = false;
		while (lastIndex < inputJson.length) {
			if (FLOAT_CHARS.contains(inputJson[lastIndex])) {
				double_flag = true;
				lastIndex++;
			}
			else if (INT_CHARS.contains(inputJson[lastIndex])){
				lastIndex++;
			}
			else
				break;
		}
		String numStr = String.copyValueOf(inputJson, index, lastIndex - index);
		index = lastIndex;
		if (double_flag)
			return Double.parseDouble(numStr);
		return Integer.parseInt(numStr);
	}
	
	private static String parseString(char[] inputJson) {
		if (inputJson[index] != '"')
			return null;
		
		int lastIndex = ++index;
		for (;inputJson[lastIndex] != '"'; lastIndex++);
		String str = String.copyValueOf(inputJson, index, lastIndex - index);
		index = lastIndex + 1;
		return str;
	}
	
	private static Object[] parseArray(char[] inputJson) {
		if (nextToken(inputJson) != Token.ARRAY_OPEN) {
			return null;
		}
		ArrayList<Object> arr = new ArrayList<Object>();
		index++;
		Object obj = null;
		while (index < inputJson.length) {
			if(nextToken(inputJson) == Token.ARRAY_CLOSE) {
				index++;
				return arr.toArray();
			}
			else if (nextToken(inputJson) == Token.COMMA)
				index++;
			else {
				obj = parseValue(inputJson);
				arr.add(obj);
			}
		}
		return null;
	}
	
	private static HashMap<Object, Object> parseObject(char[] inputJson) {
		if (nextToken(inputJson) != Token.OBJECT_OPEN) {
			return null;
		}
		HashMap<Object, Object> dict = new HashMap<Object, Object>();
		index++;
		Object key = null, value = null;
		while (index < inputJson.length) {
			if(nextToken(inputJson) == Token.OBJECT_CLOSE) {
				index++;
				return dict;
			}
			else if (nextToken(inputJson) == Token.COMMA)
				index++;
			else {
				if (nextToken(inputJson) == Token.STRING)
					key = parseString(inputJson);
				else if(nextToken(inputJson) == Token.NUMBER)
					key = parseNumber(inputJson);
				
				if (nextToken(inputJson) != Token.COLON) //:
					return null;
				index++;
				value = parseValue(inputJson);
				dict.put(key, value);
			}
		}
		return null;
	}
	
	private static Token nextToken(char[] inputJson) {
		index = skipToFirstNonSpaceCharIndex(inputJson, index);
		if (index == inputJson.length)
			return Token.NULL;
		char token = inputJson[index];
		switch(token) {
			case '{':
				return Token.OBJECT_OPEN;
			case '}':
				return Token.OBJECT_CLOSE;
			case '[':
				return Token.ARRAY_OPEN;
			case ']':
				return Token.ARRAY_CLOSE;
			case ',':
				return Token.COMMA;
			case '"':
				return Token.STRING;
			case '1': case '2': case '3': case '4': case '5': case '6':
				case '7': case '8': case '9': case '0': case '-':
					return Token.NUMBER;
			case ':':
				return Token.COLON;
		}
		
		int remaining = inputJson.length - index;
		
		if (remaining >= 5 && String.copyValueOf(inputJson, index, 5).equals("false")) {
			index += 5;
			return Token.FALSE;
		}

		if (remaining >= 4 && String.copyValueOf(inputJson, index, 4).equals("true")) {
			index += 4;
			return Token.TRUE;
		}

		if (remaining >= 4 && String.copyValueOf(inputJson, index, 4).equals("null")) {
			index += 4;
			return Token.NULL;
		}

		return Token.NONE;
	}
	
	private static int skipToFirstNonSpaceCharIndex(char[] inputJson, int index) {
		for(;SKIPCHARS.contains(inputJson[index]) && index < inputJson.length; index++);
		return index;
	}
}
