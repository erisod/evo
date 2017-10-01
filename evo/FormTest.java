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
		tf.run(input);
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
		tf.run(input);
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
		tf.run(input);

		assertEquals("Expect tf.runcost() after run of infinite loop is max ", tf.runCost(), (float) tf.maxOps, 0.0f);

		assertEquals("Expect correct run count of 1", 1, tf.runCount);

		tf.run(input);

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

		tf.run(input);
		// tf.run(input);
		// tf.run(input);

		assertEquals("Expect tf.runcost() after short code is 2 ", tf.runCost(), (float) 2.0f, 0.0f);

	}
}