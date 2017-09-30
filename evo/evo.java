package evo;

public class evo {
	
	
	public static void main(String[] args) {
		Problem p = new Negate();

		// p.runCycle(0);

		p.runManyCycles(1000);
		
		System.out.println("Done.");
	}
}