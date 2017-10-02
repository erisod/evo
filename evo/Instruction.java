package evo;

import java.util.Random;

public class Instruction {
	int operation = 0;
	int p1 = 0;
	int p2 = 0;
	int p3 = 0;
	int p4 = 0;
	static Random rand = new Random();

	boolean validOp(int op) {
		if (op >= 0 && op <= 9) {
			return true;
		} else
			return false;
	}

	static int maxOp() {
		// Maximum operation number.
		int operationCount = 10;
		return operationCount;
	}

	Instruction(int randRange) {
		operation = rand.nextInt(maxOp());
		p1 = rand.nextInt(randRange * 2) / 2;
		p2 = rand.nextInt(randRange * 2) / 2;
		p3 = rand.nextInt(randRange * 2) / 2;
		p4 = rand.nextInt(randRange * 2) / 2;
	}

	Instruction() {
	}

	Instruction copy() {
		Instruction i = new Instruction();
		i.operation = operation;
		i.p1 = p1;
		i.p2 = p2;
		i.p3 = p3;
		i.p4 = p4;

		return i;
	}

	boolean noop() {
		if (operation == 0) {
			return true;
		} else
			return false;
		
	}

	public String toString() {
		String desc;

		switch (operation) {
		case 0: // no-op.
			desc = "noop";
			break;
		case 1: // simple jump
			desc = "jump code" + p1;
			break;
		case 2: // addition (addleq, add and branch if less than or equal to
				// zero)[5]
			desc = "addleq " + p1 + " " + p2 + " " + p3;
			desc += "  (add mem" + p1 + "+ mem" + p2 + " and if <= 0 jump to code" + p3 + ")";
			break;
		case 3: // decrement (DJN, decrement and branch (jump) if nonzero)[6]
			desc = "decnzj " + p1 + " " + p2 + " " + p3;
			desc += "  (decrement mem" + p1 + " by mem " + p2 + " and if !=0 jump to code" + p3 + ")";
			break;
		case 4: // increment (P1eq, plus 1 and branch if equal to another
				// value)[7]
			desc = "inceq " + p1 + " " + p2 + " " + p3;
			desc += "  (increment mem" + p1 + " and if equal to mem" + p2 + " jump to code " + p3 + ")";
			break;
		case 5: // subtraction (subleq, subtract and branch if less than or
				// equal)[8][9]
			desc = "subleq " + p1 + " " + p2 + " " + p3 + " " + p4;
			desc += "  (subtract mem" + p2 + " from mem" + p1 + " and if less than mem" + p3 + " jump to code " + p4
					+ ")";
			break;
		case 6: // copy to result
			desc = "copyres " + p1 + " " + p2;
			desc += "  (copy mem" + p1 + " to output" + p2 + ")";
			break;
		case 7: // set value at location.
			desc = "setval " + p1 + " " + p2;
			desc += "  (set " + p1 + " to " + p2 + ")";
			break;
		case 8: // end execution.
			desc = "endexec";
			break;
		case 9: // copy from input
			desc = "copyin " + p1 + " " + p2;
			desc += "  (copy input " + p1 + " to mem" + p2 + ")";
			break;
		default: // Invalid instruction.
			desc = "INVALID-OP";
			break;
		}

		return desc;
	}

	void print() {
		System.out.println(this.toString());
	}
}