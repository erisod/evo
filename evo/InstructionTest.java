package evo;

import static org.junit.Assert.*;

import org.junit.Test;

public class InstructionTest {

	@Test
	public void InstructionCopy() {

		Instruction ti = new Instruction();
		ti.operation = 9;

		assertEquals("Expect ti op = 9", 9, ti.operation);

		Instruction tc = ti.copy();

		assertEquals("Expect tc op = 9", 9, tc.operation);

		ti.operation = 6;

		assertEquals("Expect ti op = 6", 6, ti.operation);
		assertEquals("Expect tc op = 9", 9, tc.operation);
	}
}