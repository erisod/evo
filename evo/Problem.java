package evo;

import java.util.ArrayList;
import java.util.Collections;

public class Problem {

	// set of forms
	ArrayList<Form> forms;

	// max count of forms
	int maxForms = 1;

	ArrayList<Float> scoreHistory = new ArrayList<Float>();

	boolean solved = false;
	float topScore;
	int sameTopScoreCount = 0;

	Problem() {
		forms = new ArrayList<Form>();

		// Create a bunch of forms.
		for (int i = 0; i < maxForms; i++) {
			forms.add(new Form());
		}
	}

	// Run all forms.
	void runRace() {
		// System.out.println("forms.size():" + forms.size());
		for (Form f : forms) {
			f.run();
		}
	}

	/*
	 * Evaluation defines the problem the forms should evolve to solve. A high
	 * score is good, a low score is bad.
	 */
	float evaluate(Form f) {
		/* Output 1 problem. */
		int answer = 1;
		float score = -(Math.abs(answer - f.output[0]));

		return score;
	}

	void decorateScores() {
		for (Form f : forms) {
			float eval = evaluate(f);
			if (!f.producedOutput) {
				eval = Float.MIN_VALUE;
			}
			f.scores.add(eval);

			float sum = 0.0f;
			for (float s : f.scores) {
				// System.out.print(s + ",");
				sum += s;
			}
			
			f.score = sum / f.scores.size();
			scoreHistory.add(f.score);

			if (f.score > topScore) {
				topScore = f.score;
				sameTopScoreCount = 0;
			} else if (f.score == topScore) {
				sameTopScoreCount++;
				// If we get the same score for 100 cycles count the problem as
				// solved.
				if (sameTopScoreCount > 100 && topScore != Float.MIN_VALUE) {
					solved = true;
				}
			}

			// System.out.println(" --> " + f.score);
		}
	}

	void sortWinners() {
		Collections.sort(forms);
	}

	void progressFirst(int winnerCount) {
		int slotsPerWinner = Math.max(1, maxForms / winnerCount);
		ArrayList<Form> newForms = new ArrayList<Form>();

		for (int i = 0; i < winnerCount; i++) {
			for (int j = 0; j < slotsPerWinner; j++) {
				newForms.add(new Form(forms.get(i)));
			}
		}
		

		// Fill any remainders with random forms (plus 10 extra).
		// for (int i = newForms.size(); i < maxForms + 10; i++) {
		// newForms.add(new Form());
		// }

		// Replace.
		forms = newForms;
	}

	void printScores(int n) {
		for (int i = 0; i < n; i++) {
			System.out.println("  " + i + " : " + forms.get(i).score);
		}
	}

	void runCycle(int runid) {
		int batchSize = 5;
		for (int i = 0; i < batchSize; i++) {
			runRace();
			decorateScores();
		}

		sortWinners();
		// printScores(5);

		if (runid % 1 == 0) {

			if (runid % 1 == 0) {
				forms.get(0).print();
				// System.out.println("----------");
			}

			System.out.println("Forms.size(): " + forms.size());
			System.out.println("Cycle : " + runid + " Best score: " + forms.get(0).score + ", run cost "
					+ forms.get(0).execCost / forms.get(0).runCount + " ops: " + forms.get(0).code.size());

		}

		progressFirst(10);
	}

	void runManyCycles(int n) {
		for (int i = 0; i < n; i++) {

			if (!solved) {
				runCycle(i);
			} else {
				System.out.println("Problem solved (?)  Here's the best code: ");
				forms.get(0).print();
			}
		}
	}
}

class Add extends Problem {
	@Override
	float evaluate(Form f) {
		// Addition problem.
		int answer = f.input[0] + f.input[1];
		float score = -(Math.abs(answer - f.output[0]));
		return score;
	}
}

class Negate extends Problem {
	@Override
	float evaluate(Form f) {
		int answer = -f.input[0];
		float score = -(Math.abs(answer - f.output[0]));
		return score;
	}
}

class Copy extends Problem {
	@Override
	float evaluate(Form f) {
		/* Copy input[n] to output[n] and */
		float score = -(Math.abs(f.input[0] - f.output[0]) + Math.abs(f.input[1] -f.output[1]));
		return score;
	}
}