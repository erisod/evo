package evo;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormTest {

	@Test
	public void iocopy() {
		int[] input = new int[10];
		input[0] = 99;

		Form tf = new Form(); // MyClass is tested

		tf.code.get(0).operation = 9;
		tf.code.get(0).p1 = 0;
		tf.code.get(0).p2 = 0;

		tf.code.get(1).operation = 6;
		tf.code.get(1).p1 = 0;
		tf.code.get(1).p2 = 0;

		tf.code.get(2).operation = 8;

		assertEquals("Expect output[0] = 0 (pre-run)", 0, tf.output[0]);
		tf.runCode(input);
		assertEquals("Expect output[0] = 99", 99, tf.output[0]);
	}

	@Test
	public void add() {
		int[] input = new int[10];
		input[0] = 103;
		input[1] = 20;

		Form tf = new Form();

		tf.code.get(0).operation = 9;
		tf.code.get(0).p1 = 0;
		tf.code.get(0).p2 = 0;

		tf.code.get(1).operation = 9;
		tf.code.get(1).p1 = 1;
		tf.code.get(1).p2 = 1;

		tf.code.get(2).operation = 2;
		tf.code.get(2).p1 = 0;
		tf.code.get(2).p2 = 1;
		tf.code.get(2).p3 = 3;

		tf.code.get(3).operation = 6;
		tf.code.get(3).p1 = 0;
		tf.code.get(3).p2 = 0;

		tf.code.get(4).operation = 8;

		assertEquals("Expect output[0] = 0 (pre-run)", 0, tf.output[0]);
		tf.runCode(input);
		assertEquals("Expect output[0] = 123", 123, tf.output[0]);
	}

	@Test
	public void copyForm() {
		int[] input = new int[10];
		input[0] = 103;
		input[1] = 20;

		Form form = new Form();
		form.code.get(0).operation = 9;
		assertEquals("Expect original op[0]=9", 9, form.code.get(0).operation);

		Form formCopy = new Form(form, false);

		assertEquals("Expect copy op[0]=9", 9, formCopy.code.get(0).operation);

		// Modify original, confirm copy is unchanged.
		form.code.get(0).operation = 3;
		assertEquals("Expect original op[0]=3", 3, form.code.get(0).operation);
		assertEquals("Expect copy op[0]=9", 9, formCopy.code.get(0).operation);
	}

	@Test
	public void costEvalTest() {
		int[] input = new int[10];
		input[0] = 103;
		input[1] = 20;

		// Create an endless loop form.
		Form tf = new Form();

		tf.code.get(0).operation = 1;
		tf.code.get(0).p1 = 1;

		tf.code.get(1).operation = 1;
		tf.code.get(1).p1 = 0;

		assertEquals("Expect tf.runcost() is 0", 0.0f, tf.runCost(), 0.0f);
		assertEquals("Expect tf.opcost() is 2", 2.0f, tf.opCost(), 0.5f);

		tf.runCode(input);

		assertEquals("Expect tf.runcost() after run of infinite loop is max ", tf.runCost(), (float) tf.maxOps, 0.0f);

		assertEquals("Expect correct run count of 1", 1, tf.runCount);

		tf.runCode(input);

		assertEquals("Expect tf.runcost() after run of infinite loop is max ", tf.runCost(), (float) tf.maxOps, 0.0f);
		assertEquals("Expect correct run count of 1", 2, tf.runCount);

		// Reset.
		tf.runCount = 0;
		tf.execCost = 0;

		assertEquals("obvious ", tf.runCount, 0);
		assertEquals("obvious ", tf.execCost, 0);

		assertEquals("Expect tf.runcost() after reset is 0 ", tf.runCost(), (float) 0.0f, 0.0f);

		tf.code.get(0).operation = 0;
		tf.code.get(1).operation = 8; // endexec

		tf.runCode(input);
		assertEquals("Expect tf.runcost() after short code is 2 ", tf.runCost(), (float) 2.0f, 0.0f);
	}

	@Test
	public void costEvalTest2() {
		int[] input = new int[10];
		input[0] = 103;
		
		// Code evolved for negation.
		/*
		  0 : copyin 0 1  (copy input 0 to mem1)
		  1 : decnzj 0 1 4  (decrement mem0 by mem 1 and if !=0 jump to code4)
		  2 : noop
		  3 : noop
		  4 : copyres 0 0  (copy mem0 to output0)
		 */
		Form tf = new Form();

		tf.code.get(0).operation = Instruction.COPYIN;
		tf.code.get(0).p1 = 1;

		tf.code.get(1).operation = Instruction.DECNZJ;
		tf.code.get(1).p1 = 0;
		tf.code.get(1).p2 = 1;
		tf.code.get(1).p3 = 4;

		tf.code.get(2).operation = Instruction.NOOP;
		tf.code.get(3).operation = Instruction.NOOP;

		tf.code.get(4).operation = Instruction.COPYRES;
		tf.code.get(4).p1 = 0;
		tf.code.get(4).p2 = 0;
		
		tf.code.get(5).operation = Instruction.ENDEXEC;

		tf.runCode(input);

		assertEquals("Expect tf.runcost() ", (float) 6.0f, tf.runCost(), 0.0f);
		assertEquals("Expect tf.opcost() ", (float) 4.0f, tf.opCost(), 0.1f);

	}
}