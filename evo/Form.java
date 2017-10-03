package evo;

import java.util.ArrayList;
import java.util.Random;

// Form (e.g. not a life form)
public class Form implements Comparable<Form>, Runnable {
	int id;

	ArrayList<Instruction> code;
	int[] output;
	int[] input;
	int[] mem;
	int codesize = 15;
	int memsize = 2;
	int iosize = 2;
	int ops; // Non-nop operations in code.
	int execCost = 0;
	int runCount = 0;
	int maxOps = 100; // Maximum allowed operations per run.
	boolean producedOutput = false;
	boolean finished = false;
	static Random rand = new Random();
	float score;
	ArrayList<Float> scores = new ArrayList<Float>();

	int cp = 0;

	void init() {
		code = new ArrayList<Instruction>();

		mem = new int[memsize];
		output = new int[iosize];

		reset();
	}

	void reset() {
		cp = 0;
		finished = false;
		producedOutput = false;

		for (int i = 0; i < output.length; i++) {
			output[i] = 0;
		}
		for (int i = 0; i < mem.length; i++) {
			mem[i] = 0;
		}
	}

	void print() {
		System.out.println("CODE  cp @ " + cp);
		for (int i = 0; i < code.size(); i++) {
			// if (!code.get(i).noop())
			System.out.println("  " + i + " : " + code.get(i));
		}

		System.out.println("INPUT: ");
		if (input == null) {
			System.out.println("  Form never run; no input.");
		} else { 
			for (int i = 0; i < input.length; i++) {
				System.out.println("  input[" + i + "] : " + input[i]);
			}
		}

		System.out.println("OUTPUT: ");
		for (int i = 0; i < output.length; i++) {
			if (output[i] != 0) {
				System.out.println("  output[" + i + "] : " + output[i]);
			}
		}

		System.out.println("SCORE: " + score);
		if (runCount > 0) {
			System.out.println("avg run cost: " + runCost() + "  opCost:" + opCost());
		}
	}

	// Generate a form from nothing (maybe random, maybe empty).
	Form() {
		init();

		for (int i = 0; i < codesize; i++) {
			code.add(new Instruction());
		}

		/*
		 * // Simple program to output 99.
		 * 
		 * // set 99 to position 20. code[p++] = 7; code[p++] = 20; code[p++] =
		 * 20;
		 * 
		 * // output position 20 to output slot 0. code[p++] = 6; code[p++] =
		 * 20; code[p++] = 0;
		 * 
		 * // end execution. code[p++] = 8;
		 * 
		 * ops = 0; for (int i = 0; i < code.length; i++) { initialcode[i] =
		 * code[i]; if (code[i] != 0) ops++; }
		 */
	}

	// Generate a form from a parent with mutation optional.
	Form(Form parent, boolean mutate) {
		init();

		int j = 0;
		int mutationRate = 50; // 1 in n.

		Instruction newInst;

		// j is the incrementor for the parent.
		while (code.size() < codesize && j < parent.code.size()) {

			// Three forms of mutation :
			// (1) operation/value mutation.
			// (2) segment removal, duplication.
			// (3) random additional instructions.

			boolean segmentMutation = true;

			if (segmentMutation && mutate) {

				// Transcription error. Skip/duplicate.
				if (rand.nextInt(mutationRate) == 0) {
					j = rand.nextInt(parent.codesize);
					continue;
				}

				// Add new random instructions; more when the parent code size is small.
				if ((rand.nextInt(mutationRate)) == 0) {
					int count = rand.nextInt(Math.max(1, codesize - parent.code.size()));
					for (int i = 0; i < count; i++) {
						newInst = new Instruction();
						newInst.operation = rand.nextInt(Instruction.maxOp());
						newInst.p1 = rand.nextInt(parent.codesize * 2) / 2;
						newInst.p2 = rand.nextInt(parent.codesize * 2) / 2;
						newInst.p3 = rand.nextInt(parent.codesize * 2) / 2;
						newInst.p4 = rand.nextInt(parent.codesize * 2) / 2;
					}
				}
			}

			// Copy the operation from the parent.
			newInst = parent.code.get(j).copy();

			if (mutate) {
				// Mutate the operation.
				if ((rand.nextInt(mutationRate)) == 0) {
					newInst.operation = rand.nextInt(Instruction.maxOp());
				}

				// Mutate parameters.
				if ((rand.nextInt(mutationRate)) == 0) {
					newInst.p1 = rand.nextInt(parent.codesize * 2) - parent.codesize;
				}
				if ((rand.nextInt(mutationRate)) == 0) {
					newInst.p2 = rand.nextInt(parent.codesize * 2) - parent.codesize;
				}
				if ((rand.nextInt(mutationRate)) == 0) {
					newInst.p3 = rand.nextInt(parent.codesize * 2) - parent.codesize;
				}
				if ((rand.nextInt(mutationRate)) == 0) {
					newInst.p4 = rand.nextInt(parent.codesize * 2) - parent.codesize;
				}
			}

			code.add(newInst);

			j++;
		}

		// System.out.println("parent was " + parent.code.size() + " and child is " + code.size());
	}

	float runCost() {
		if (runCount == 0) {
			return 0;
		}
		return execCost / runCount;
	}

	// Count of instructions; .001 for no-op. 1 for others.
	float opCost() {
		float count = 0.0f;
		for (Instruction i : code) {
			if (i.operation == Instruction.NOOP) {
				count += .001f;
			} else {
				count += 1.0f;
			}
		}
		return count;
	}


	@Override
	public void run() {
		runCode(null);
	}

	void runCode(int[] newInput) {
		runCount++;

		if (newInput != null) {
			input = newInput;
		}

		if (input == null) {
			System.out.println("Input is null.  Fatal!");
			System.exit(1);
		}


		reset();
		int opsleft = maxOps;

		while (!finished && opsleft > 0 && cp < code.size() && cp >= 0) {
			step();
			opsleft--;
			execCost++;
		}
	}

	private void addleq() {
		Instruction op = code.get(cp);
		mem[op.p1] += mem[op.p2];
		if (mem[op.p1] <= 0)
			cp = mem[op.p3];
		else
			cp++;
	}

	private void decnzj() {
		Instruction op = code.get(cp);

		mem[op.p1] = mem[op.p1] - mem[op.p2];
		if (mem[op.p1] < 0)
			cp = op.p3;
		else
			cp++;
	}

	private void inceq() {
		// increment (P1eq, plus 1 and branch if equal to another value)[7]
		Instruction op = code.get(cp);

		mem[op.p1] += 1;
		if (mem[op.p1] == mem[op.p2])
			cp = op.p3;
		else
			cp++;
	}

	private void subleq() {
		// // subtraction (subleq, subtract and branch if less than or equal)[8][9]
		Instruction op = code.get(cp);

		mem[op.p1] -= mem[op.p2];
		if (mem[op.p1] <= mem[op.p3])
			cp = op.p4;
		else
			cp++;
	}

	private void copyToResult() {
		// Copy value at mem position to result position.
		Instruction op = code.get(cp);
		producedOutput = true;

		output[op.p2] = mem[op.p1];
		cp++;
	}

	private void copyFromInput() {
		// Copy value at input position to mem position.
		Instruction op = code.get(cp);

		mem[op.p2] = input[op.p1];
		cp++;
	}

	private void jump() {
		Instruction op = code.get(cp);
		cp = op.p1;
	}

	private void setval() {
		// Copy value to result position.
		Instruction op = code.get(cp);

		mem[op.p1] = op.p2;
		cp++;
	}

	private void step() {
		try {
			if (cp < 0 || cp > code.size()) {
				finished = true;
				return;
			}
			Instruction op = code.get(cp);

			// Operations based on https://en.wikipedia.org/wiki/One_instruction_set_computer
			// "Arithmetic-based Turing-complete machines".
			switch (op.operation) {
			case 0: // no-op.
				cp++;
				break;
			case 1: // simple jump
				jump();
				break;
			case 2: // addition (addleq, add and branch if less than or equal to zero)[5]
				addleq();
				break;
			case 3: // decrement (DJN, decrement and branch (jump) if nonzero)[6]
				decnzj();
				break;
			case 4: // increment (P1eq, plus 1 and branch if equal to another value)[7]
				inceq();
				break;
			case 5: // subtraction (subleq, subtract and branch if less than or equal)[8][9]
				subleq();
				break;
			case 6: // copy to result
				copyToResult();
				break;
			case 7: // set value at location.
				setval();
				break;
			case 8: // end execution.
				finished = true;
				break;
			case 9: // copy from input
				copyFromInput();
				break;
			default: // Invalid instruction.
				finished = true;
				break;
			}
		} catch (Exception e) {
			finished = true;
			return;
		}
	}

	@Override
	public int compareTo(Form o) {
		if (score > o.score) {
			return -1;
		} else if (score < o.score) {
			return 1;
		} else {
			/* Note : Comparing on execution cost when the problem isn't
			 * solved is bad.  It results in optimizing for an immediately terminating program.
			 */

			/* Insight: 
			 * It also seems to be bad for general evolution.  To achieve better-than-plateau
			 * behavior we have to have a lot of mutation line up and that might result in higher
			 * cost forms that behave only as well as the lowest cost form.  Removing the higher
			 * cost versions kills that chance.*/

			// System.out.println("Evaluating form based on code and cost. Score @ " + score);

			// Prefer more efficient code, but only when the answer is correct. Score of 0
			// indicates a correct answer.
			if (score == 0.0) { 
				if ((runCost()) < o.runCost()) {
					return -1;
				} else if (runCost() > o.runCost()) {
					return 1;
				} else {
					// Prefer smaller code.
					if ((opCost()) < o.opCost()) {
						return -1;
					} else if (opCost() > o.opCost()) {
						return 1;
					}
				}
			}
		}
		return 0;
	}
}
