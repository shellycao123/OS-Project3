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
			
			//run the FIFO algorithm
			FIFO(resources,tasks);
			for(Task task: tasks) {
				task.reset();
			}
			Banker(resources,tasks);

			
		
			
			
			
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
				}
				//otherwise wait 
				else {
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
							}
							//otherwise wait 
							else {
								blocked.add(task);
								task.isBlocked = true;
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
			}
			//if there is a deadlock
			if(blocked.size() == tasks.length-finish) {
				if(lastDead) {//adjust for aborting more than one task in a cycle
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
	
	public static void Banker(Resource[] resources, Task[] tasks) {
		int finish = 0;
		ArrayList<Task> blocked = new ArrayList<>();
		int[] pointer = new int[tasks.length];//which activity the task is currently one
		ArrayList<Task> abort = new ArrayList<>();
		//loop thru the tasks for each cycle
		while(finish != tasks.length){
			ArrayList<Task> unblocked = new ArrayList<>();
			int[] released = new int[resources.length];
			
			//check the blocked first
			for(Task bl: blocked) {
				bl.totalTime++;
				Activity cur = bl.act.get(pointer[bl.pos]);
				if(grantSafe(resources,tasks,bl.pos,cur.resource-1,cur.claim)) {
					bl.curDelay = bl.act.get(++pointer[bl.pos]).delay;
					unblocked.add(bl);		
				}
				else if(bl.isAborted) {
					unblocked.add(bl);
					abort.add(bl);
					finish++;
				}
				//otherwise wait 
				else {
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
					}
					else {
						Activity cur = task.act.get(pointer[task.pos]);//current activity
						switch(cur.type) {
						case("initiate"):
							task.claims[cur.resource-1] = cur.claim;
							if(cur.claim>resources[cur.resource-1].total) {
								System.out.println("Invalid initial claim. Aborted");
								task.isAborted = true;
								task.isFinished = true;
								abort.add(task);
								finish++;
							}
							task.curDelay = task.act.get(++pointer[task.pos]).delay;
							task.totalTime++;
							break;
						case("request"):
							task.totalTime++;
							//grant the request if there is available resource
							if(grantSafe(resources,tasks,task.pos,cur.resource-1,cur.claim)) {
								task.curDelay = task.act.get(++pointer[task.pos]).delay;
							}
							//if grantSaft returned false because of unsafe state
							else if(task.isAborted) {
								abort.add(task);
								finish++;
							}
							else {
								blocked.add(task);
								task.isBlocked = true;
								task.waitTime++;
							}
							break;
						case("release"):
							task.totalTime++;
							released[cur.resource-1] +=cur.claim;
							task.curDelay = task.act.get(++pointer[task.pos]).delay;
							task.cur[cur.resource-1] -=cur.claim;
							break;
						case("terminate"):
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
			for(Task t : abort) {
				for(int j = 0; j<resources.length;j++) {
					resources[j].available += t.cur[j];
					t.cur[j] = 0;
				}
			}
			for(int l = 0; l<resources.length;l++) {
				resources[l].available += released[l];
			}
		}
		
		int total = 0;
		int totalWait = 0;
		System.out.println("\t\tBanker");
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
	//grant the claim if it is safe 
	public static boolean grantSafe(Resource[] resources, Task[] tasks, int taskNum, int wantedR, int number) {	
		//suppose it's granted
		resources[wantedR].available -= number;
		//abort the task if the claim is not valid
		if(tasks[taskNum].claims[wantedR]<number+tasks[taskNum].cur[wantedR]) {
			resources[wantedR].available += number;
			System.out.println("The request exceeds the maximum total claim. The task "+taskNum+ " is aborted.");
			tasks[taskNum].isAborted = true;
			tasks[taskNum].isFinished = true;
			return false;//if the claim exceeds current available resources,reverse the change
		}else if(resources[wantedR].available < 0){
			resources[wantedR].available += number;
			return false;
		}
		tasks[taskNum].cur[wantedR] += number;
		
		//if not safe,reverse the change and return false
		if(!isSafe(resources,tasks)) {
			resources[wantedR].available += number;
			tasks[taskNum].cur[wantedR] -= number;
			return false;
		}
		return true;
		
	}
	//check if the claim can be granted
	public static boolean isSafe(Resource[] res, Task[] tasks) {
		int[] available = new int[res.length];
		for(int i = 0;i<res.length;i++) {//deep copy the arrays for simulation
			available[i] = res[i].available;
		}
		boolean[] finished = new boolean[tasks.length];
		int totalFinished = 0;
		//count already finished ones
		for(int i = 0; i<tasks.length;i++) {
			if(tasks[i].isFinished) {
				totalFinished++;
				finished[i] = true;//if a task is done before safety checking
			}
		}
		while(totalFinished != tasks.length) {
			boolean isDead = true;
			for(int i = 0; i<tasks.length;i++) {
				int[] curClaim = getMaxClaim(tasks[i].claims,tasks[i].cur);
				if(!finished[i] && isAllowed(curClaim,available)) {// if a task is not done and the claim can be satisfied
					available = grant(available,tasks[i].cur);
					finished[i] = true;
					totalFinished++;
					isDead = false;//flip the flag if something can be granted
				}
			}
			if(totalFinished == tasks.length) {
				return true;
			};
			if(isDead) {
				return false;
			}
		}
		return false;
	}
	//get the maximum possible claim of each task
	public static int[] getMaxClaim(int[] claim, int[] occupied) {
		int[] result = new int[claim.length];
		for(int i = 0;i<claim.length;i++) {
			result[i] = claim[i]-occupied[i];
		}
		return result;
	}
	
	public static boolean isAllowed(int[]curClaim,int[] avail) {	
		for(int i = 0;i<curClaim.length;i++) {
			if(curClaim[i]>avail[i]) {
				return false;
			}
		}
		return true;
	}
	public static int[] grant(int[] resources, int[] claims) {
		for(int i =0;i<resources.length;i++) {
			resources[i] += claims[i];
		}
		return resources;
	}
	
	

}
