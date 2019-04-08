package project3;
import java.util.*;

//represent each task in the system
public class Task {
	//data field
	ArrayList<Activity> act = new ArrayList<>();
	boolean isFinished = false;
	boolean isAborted = false;
	boolean isBlocked = false;
	int totalTime = 0;
	int waitTime =0;
	int curDelay = 0;
	int pos;
	
	int[] claims;//list of claims for all resources
	int[] cur; //currently occupied
	
	public Task(int claim, int pos) {
		this.claims = new int[claim];
		this.cur= new int[claim];
		this.pos = pos;
	}
	
	public void reset() {
		isFinished = false;
		isAborted = false;
		isBlocked = false;
		
		totalTime = 0;
		waitTime =0;
		curDelay = 0;
		this.cur = new int[this.claims.length];
	}
	
	
}
