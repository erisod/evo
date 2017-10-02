package evo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Problem {

	// set of forms
	ArrayList<Form> forms;

	// max count of forms
	int maxForms = 10000;

	// ArrayList<Float> scoreHistory = new ArrayList<Float>();

	boolean solved = false;
	float topScore;
	int sameTopScoreCount = 0;


	static Random rand = new Random();

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

		long startTime = System.nanoTime();

		int[] input = new int[2];
		input[0] = rand.nextInt(100);
		input[1] = rand.nextInt(100);

		boolean useThreads = true;

		if (useThreads) {

			ExecutorService executor = Executors.newFixedThreadPool(2);

			for (Form f : forms) {
				f.input = input;
				executor.execute(f);
			}

			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch (InterruptedException e) {
				System.out.println("Threads never finished.");
			}
		} else {
			for (Form f : forms) {
				f.input = input;
				f.run();
			}
		}

		// System.out.println("Duration: " + (System.nanoTime() - startTime) / 1000000000.0f);

	}

	/*
	 * Evaluation defines the problem the forms should evolve to solve. a Zero score is
	 * good (perfect), negative score indicates distance from correct.  In the general
	 * case, the answer() helper defines the correct answer and evaluate() need not
	 * be over-riden.
	 */
	float evaluate(Form f) {
		int[] answer = answer(f);
		float gap = 0f;
		
		for (int i = 0 ; i < answer.length; i++) {
			gap += Math.abs(answer[i] - f.output[i]);
		}
		float score = -gap;
		
		return score;
	}

	/* Calculate the correct answer. */
	int[] answer(Form f) {
		int[] a = new int[1];
		a[0] = 1;

		return a;
	}

	void decorateScores() {
		for (Form f : forms) {
			float eval = evaluate(f);
			if (!f.producedOutput) {
				eval = Float.NEGATIVE_INFINITY;
			}
			f.scores.add(eval);

			double sum = 0.0f;
			for (float s : f.scores) {
				// System.out.print(s + ",");
				sum += s;
			}

			f.score = (float) (sum / f.scores.size());

			if (f.score > topScore) {
				topScore = f.score;
			}

			if (f.score == topScore) {
				sameTopScoreCount++;
				// If we get the same score for N cycles count the problem as
				// solved.
				if (sameTopScoreCount > 100 && topScore != Float.NEGATIVE_INFINITY) {
					System.out.print("Found same top score (" + topScore + ") for many cycles.  Marking solved.");
					// solved = true;
				}
			} else {
				sameTopScoreCount = 0;
			}

			// System.out.println(" --> " + f.score);
			// scoreHistory.add(f.score);
		}
	}

	void sortWinners() {
		Collections.sort(forms);
	}

	void progressFirst(int winnerCount) {
		int slotsPerWinner = maxForms / winnerCount;
		ArrayList<Form> newForms = new ArrayList<Form>();

		// Copy each of the top 10 once, then mutate them n times.
		for (int i = 0; i < winnerCount; i++) {
			newForms.add(new Form(forms.get(i), false));
			for (int j = 0; j < slotsPerWinner; j++) {
				newForms.add(new Form(forms.get(i), true));
			}
		}

		// Fill any remainders with random forms (plus 10 extra).
		for (int i = newForms.size(); i < maxForms + 10; i++) {
			newForms.add(new Form());
		}

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

		if (runid % 100 == 0 || solved) {
			System.out.println("Best form (of " + forms.size() + ") :");
			forms.get(0).print();
			System.out.println("ANSWER: ");
			int answer[] = answer(forms.get(0));
			for (int i = 0; i < answer.length; i++) {
				System.out.println("  answer[" + i + "] = " + answer[i]);
			}
		}

		if (runid % 10 == 0) {
			System.out.print("[" + runid + " S:" + forms.get(0).score + " C:"
					+ forms.get(0).execCost / forms.get(0).runCount + " ] ");
			// ops: " + forms.get(0).code.size());
		}

		progressFirst(10);
	}

	void runManyCycles(int n) {
		for (int i = 0; i < n; i++) {

			runCycle(i);
		}
	}
}

class Add extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];
		for (int val : f.input) {
			a[0] += val;
		}
		return a;
	}
}

class Negate extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];
		a[0] = -f.input[0];
		return a;
	}

}

class NCopy extends Problem {
	@Override
	int[] answer(Form f) {
		return f.input;
	}
}

class Copy extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];
		a[0] = f.input[0];
		return a;
	}
}

class Multiply extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];
		a[0] = f.input[0] * f.input[1];
		
		return a;
	}
}

class Divide extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];
		if (f.input[1] == 0) {
			a[0] = 0;
		} else {
			a[0] = f.input[0] / f.input[1];
		}
		return a;
	}
}

class Average extends Problem {
	@Override
	int[] answer(Form f) {
		int[] a = new int[1];

		for (int val : f.input) {
			a[0] += val;
		}

		a[0] = a[0] / f.input.length;
		return a;
	}
}