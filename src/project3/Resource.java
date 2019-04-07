package project3;

public class Resource {
	int total;
	int occupied;
	int available;
	
	public Resource(int total) {
		this.total=total;
		this.available = total;
		this.occupied = 0;
	}
	
}
