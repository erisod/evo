package evo;

import java.util.ArrayList;
import java.util.Random;

// Form (e.g. not a life form)
public class Form implements Comparable<Form> {
	int id;

	ArrayList<Instruction> code;
	int[] output;
	int[] input;
	int[] mem;
	int codesize = 50;
	int memsize = 10;
	int iosize = 5;
	int ops; // Non-nop operations in code.
	int execCost = 0;
	int runCount = 0;
	boolean producedOutput = false;
	boolean finished = false;
	Random rand = new Random();
	float score;
	ArrayList<Float> scores = new ArrayList<Float>();

	int cp = 0;

	void init() {
		code = new ArrayList<Instruction>();

		mem = new int[memsize];
		output = new int[iosize];
		input = new int[iosize];

		reset();
	}

	void reset() {
		input[0] = rand.nextInt(200);
		input[1] = rand.nextInt(200);
		cp = 0;
		finished = false;
		producedOutput = false;

		for (int i = 0; i < iosize; i++) {
			output[i] = 0;
		}
		for (int i = 0; i < memsize; i++) {
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
		for (int i = 0; i < input.length; i++) {
			if (input[i] != 0) {
				System.out.println("  input[" + i + "] : " + input[i]);
			}
		}

		System.out.println("OUTPUT: ");
		for (int i = 0; i < output.length; i++) {
			if (output[i] != 0) {
				System.out.println("output[" + i + "] : " + output[i]);
			}
		}

		System.out.println("SCORE: " + score);
		if (runCount > 0) {
			System.out.println("avg run cost " + execCost / runCount);
		}
		System.out.println("operations total: " + ops);
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

	// Generate a form from a parent with mutation.
	Form(Form parent) {
		init();

		int j = 0;
		int mutationRate = 1; // 1 in n.

		// j is the incrementor for the parent.
		while (code.size() < codesize && j < parent.code.size()) {

			// Three forms of mutation :
			// (1) operation/value mutation.
			// (2) segment removal, duplication.
			// (3) random additional instructions.

			// Induce transcription error. Overwrite (lose copied segment).
			if (code.size() > 0 && rand.nextInt(mutationRate) == 0) {
				for (int k = rand.nextInt(code.size()); k > 0; k--) {
					code.remove(code.size() - 1);
				}
				continue;
			}

			// Transcription error. Skip/duplicate.
			if (rand.nextInt(mutationRate) == 0) {
				j = rand.nextInt(parent.codesize);
				continue;
			}

			// Copy the operation from the parent.
			Instruction newInst = parent.code.get(j).copy();
			
			// Mutate the operation.
			if ((rand.nextInt(mutationRate)) == 0) {
				newInst.operation = rand.nextInt(Instruction.maxOp());
			}
			
			// Mutate parameters.
			if ((rand.nextInt(mutationRate)) == 0) {
				newInst.p1 = rand.nextInt(parent.codesize * 2) / 2;
			}
			if ((rand.nextInt(mutationRate)) == 0) {
				newInst.p2 = rand.nextInt(parent.codesize * 2) / 2;
			}
			if ((rand.nextInt(mutationRate)) == 0) {
				newInst.p3 = rand.nextInt(parent.codesize * 2) / 2;
			}
			if ((rand.nextInt(mutationRate)) == 0) {
				newInst.p4 = rand.nextInt(parent.codesize * 2) / 2;
			}

			code.add(newInst);

			// Add some random instructions.
			if ((rand.nextInt(mutationRate)) == 0) {
				for (int i = 0; i < 20; i++) {
					newInst = new Instruction();
					newInst.operation = rand.nextInt(Instruction.maxOp());
					newInst.p1 = rand.nextInt(parent.codesize * 2) / 2;
					newInst.p2 = rand.nextInt(parent.codesize * 2) / 2;
					newInst.p3 = rand.nextInt(parent.codesize * 2) / 2;
					newInst.p4 = rand.nextInt(parent.codesize * 2) / 2;
				}
			}

			j++;
		}
	}

	void run() {
		runCount++;
		System.out.println("run() called");
		// System.out.println("code size : " + code.size());
		// System.out.println("addr: " + this);

		reset();
		int maxOps = 50;
		int opsleft = maxOps;
		
		while (!finished && opsleft-- > 0) {
			step();
		}

		execCost += maxOps - opsleft;
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

		mem[op.p2] = mem[op.p2] - mem[op.p1];
		if (mem[op.p2] < 0)
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
		// Copy value at position to result position.
		Instruction op = code.get(cp);
		producedOutput = true;

		output[op.p2] = mem[op.p1];
		cp++;
	}

	private void copyFromInput() {
		// Copy value at position to result position.
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
			case 0:  // no-op.
				cp++;
				break;
			case 1:  // simple jump
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
			// Compare execution cost.
			if ((execCost / runCount) < (o.execCost / o.runCount)) {
				return -1;
			} else if ((execCost / runCount) > (o.execCost / o.runCount)) {
				return 1;
			} else {
				// Compare code cost (size of code).
				if (ops < o.ops) {
					return -1;
				} else if (ops > o.ops) {
					return 1;
				} else
					return 0;
			}
		}
	}
}
