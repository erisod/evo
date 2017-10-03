package evo;

public class evo {
	
	
	public static void main(String[] args) {
		// Problem p = new Negate();
		Problem p = new Multiply();

		// p.runCycle(0);

		p.runManyCycles(1000000);
		
		System.out.println("Done.");
	}
}