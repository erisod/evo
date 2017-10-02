package evo;

public class evo {
	
	
	public static void main(String[] args) {
		// Problem p = new NCopy();
		Problem p = new Negate();

		// p.runCycle(0);

		p.runManyCycles(100000);
		
		System.out.println("Done.");
	}
}