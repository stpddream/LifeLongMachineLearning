package edu.eatonlab.imageprofessor.server;

import java.util.ArrayList;
import java.util.ListIterator;

public class ServerStatus {
	public static int budget;
	public static int numLocalImg; //TODO: NOT IMPLEMENTED
	
	public static void logQuery(String keyword, int size) {
		QueryHistory.queryKeys.add(keyword);
		QueryHistory.querySize.add(size);
	}
	
	public static String getQuery(int size) {
		return QueryHistory.toString(size);
	}
	
	public static int getNumQueries() {
		return QueryHistory.querySize.size();
	}

	
	private  static class QueryHistory {
		public static ArrayList<String> queryKeys = new ArrayList<String>();
		public static ArrayList<Integer> querySize = new ArrayList<Integer>();
		
		
		public static String toString(int size) {
			String resStr = "";
			
			ListIterator keysIterator =queryKeys.listIterator(queryKeys.size()); 
			ListIterator sizeIterator = querySize.listIterator(querySize.size());
			int cnt = 0;
			
			if(size == -1) cnt = -9999; //Return all if size if -1
			
			while(keysIterator.hasPrevious() && sizeIterator.hasPrevious()) {
				cnt++;
				if(cnt > size) return resStr;
				resStr += "QUERY " + keysIterator.previous() + " " + sizeIterator.previous() + "\n";
			}
			return resStr;
		}
	}
	
}
