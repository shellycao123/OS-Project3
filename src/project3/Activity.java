package project3;

//represent each activity to be performed
public class Activity {
	String type; 
	int delay;
	int resource;
	int claim; 
	
	public Activity(String type, int delay, int resource, int claim) {
		this.type = type; 
		this.delay = delay;
		this.resource = resource;
		this.claim  = claim; 
	}
	
	public String toString() {
		return "Type: "+this.type+" delay: "+delay+"Resource: "+ this.resource+" Claim: "+this.claim;
	}
}
