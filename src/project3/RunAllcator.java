package project3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
public class RunAllcator {

	public static void main(String[] args) {
		//initialize the program by reading the input
		try {
			String fName = new Scanner(System.in).nextLine();
			File file= new File(fName);
			Scanner fs = new Scanner(file);
			Task[] tasks = new Task[fs.nextInt()]; //create list of tasks
			int total = fs.nextInt();
			Resource[] resources = new Resource[total];//create a list of resources
			for(int i = 0; i < total; i++) {
				resources[i] = new Resource(fs.nextInt());
			}
			for( int i =0; i< tasks.length;i++) {
				tasks[i] = new Task(total,i);
			}
			//insert info into the objects
			while(fs.hasNext()) {
				String type = fs.next();
				tasks[fs.nextInt()-1].act.add(new Activity(type,fs.nextInt(),fs.nextInt(),fs.nextInt()));
			}
			
			/*for( int i =0; i< tasks.length;i++) {
				System.out.println(tasks[i].act);
			}*/
			
			//run the FIFO algorithm
			FIFO(resources,tasks);
			

			
		
			
			
			
		}
		catch(FileNotFoundException e){
			System.err.println("Error reading the input file: "+e);
		}

	}
	
	public static void FIFO(Resource[] resources, Task[] tasks) {
		int finish = 0;
		ArrayList<Task> blocked = new ArrayList<>();
		int[] pointer = new int[tasks.length];//which activity the task is currently one
		boolean lastDead = false;//if the last cycle is to resolve deadlock
		
		//loop thru the tasks for each cycle
		while(finish != tasks.length){
			ArrayList<Task> unblocked = new ArrayList<>();
			int[] released = new int[resources.length];
			
			//check the blocked first
			for(Task bl : blocked) {
				bl.totalTime++;
				Activity cur = bl.act.get(pointer[bl.pos]);
				if(resources[cur.resource-1].available >= cur.claim) {
					resources[cur.resource-1].available -= cur.claim;
					bl.curDelay = bl.act.get(++pointer[bl.pos]).delay;
					bl.cur[cur.resource-1] +=cur.claim;
					unblocked.add(bl);
					System.out.println("Blocked Task"+bl.pos+" completes its request"+cur.claim+ " (i.e., the request is granted");
				}
				//otherwise wait 
				else {
					System.out.println("Blocked Task"+bl.pos+"is still waiting. Finish: "+finish);
					bl.waitTime++;
				}
			}

			//run regularly
			for(Task task: tasks) {
				//execute according to the claim, if the task is not finished
				if(!task.isFinished && !task.isBlocked) {
					//if the task is  currently delayed
					if(task.curDelay != 0) {
						task.curDelay--;
						task.totalTime++;
						System.out.println("Task is calculating: "+task.curDelay);
					}
					else {
						Activity cur = task.act.get(pointer[task.pos]);//current activity
						switch(cur.type) {
						case("initiate"):
							task.claims[cur.resource-1] = cur.claim;
							task.curDelay = task.act.get(++pointer[task.pos]).delay;
							task.totalTime++;
							break;
						case("request"):
							task.totalTime++;
							//grant the request if there is available resource
							if(resources[cur.resource-1].available >= cur.claim) {
								resources[cur.resource-1].available -= cur.claim;
								task.curDelay = task.act.get(++pointer[task.pos]).delay;
								task.cur[cur.resource-1] +=cur.claim;
								System.out.println("Task"+task.pos+" completes its request"+cur.claim+ " (i.e., the request is granted");
							}
							//otherwise wait 
							else {
								blocked.add(task);
								task.isBlocked = true;
								System.out.println("Task"+task.pos+"is waiting");
								task.waitTime++;
							}
							break;
						case("release"):
							System.out.println("Task"+task.pos+" completes its release (i.e., the request is granted");
							task.totalTime++;
							released[cur.resource-1] +=cur.claim;
							task.curDelay = task.act.get(++pointer[task.pos]).delay;
							task.cur[cur.resource-1] -=cur.claim;
							break;
						case("terminate"):
							System.out.println("Task"+task.pos+" terminates at "+task.totalTime);
							finish++;
							task.isFinished = true;
							break;	
						}
					}

				}

			}
			for(Task canRun: unblocked) {
				blocked.remove(canRun);
				canRun.isBlocked = false;
			}
			for(int l = 0; l<resources.length;l++) {
				resources[l].available += released[l];
				//System.out.println(resources[l].available+" items available ");
			}
			//if there is a deadlock
			if(blocked.size() == tasks.length-finish) {
				System.out.println("Blcoked size"+blocked.size());
				if(lastDead) {//adjust for aborting more than one task in a cycle
					System.out.println("second deadlock in a row");
					for(Task ta : tasks) {
						if(!ta.isFinished) {
							ta.totalTime--;
							ta.waitTime--;
						}

					}
				}
				for(Task t : tasks) {//drop the lowest task
					if(!t.isFinished) {
						System.out.println("Deadlock detected. Abrting the lowest number tasks. "+ t.pos);
						for(int j = 0; j<resources.length;j++) {
							resources[j].available += t.cur[j];
							t.cur[j] = 0;
						}
						t.isFinished=true;
						t.isAborted = true;
						finish++;
						blocked.remove(t);
						break;
					}
				}
				lastDead = true;
			}
			else {
				lastDead = false;
			}

			System.out.println("");
		}
		
		int total = 0;
		int totalWait = 0;
		System.out.println("\t\tFIFO");
		//printout the result
		for(Task t:tasks) {
			if(t.isAborted) {
				System.out.printf("Task %2d:\tAborted\n",t.pos+1);
			}
			else {
				total += t.totalTime;
				totalWait += t.waitTime;
				System.out.printf("Task %2d: %7d %2d %2d %%\n", t.pos+1,t.totalTime,t.waitTime,Math.round(((double)t.waitTime)/t.totalTime*100));
			}
			
		}
		System.out.printf("Total:   %7d %2d %2d %%\n",total,totalWait,Math.round(((double)totalWait)/total*100));
		

	}

}
