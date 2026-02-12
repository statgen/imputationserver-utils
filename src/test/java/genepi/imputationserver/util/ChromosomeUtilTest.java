package genepi.imputationserver.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ChromosomeUtilTest {

	@Test
	public void testIsValidChromosome() {
		for (int i = 1; i <= 23; i++) {
			// Numbers 1 ... 23 (inclusive) are valid.
			String chr = String.valueOf(i);
			assertTrue(ChromosomeUtil.isValidChromosome(chr));

			// They can also be prepended with 'chr'.
			String withPrefix = "chr" + chr;
			assertTrue(ChromosomeUtil.isValidChromosome(withPrefix));
		}

		// X is valid.
		assertTrue(ChromosomeUtil.isValidChromosome("X"));
		assertTrue(ChromosomeUtil.isValidChromosome("chrX"));

		// Case-sensitive: lowercase x not valid.
		assertFalse(ChromosomeUtil.isValidChromosome("x"));
		assertFalse(ChromosomeUtil.isValidChromosome("chrx"));

		// Integers out of range or with preceding 0's are not valid.
		for (String chr : List.of("-1", "0", "24", "-15", "99", "04", "0020")) {
			assertFalse(ChromosomeUtil.isValidChromosome(chr));
		}
	}

	@Test
	public void testIsChrX() {
		// 23, X, and the same with 'chr' prefix are all valid
		for (String chr : List.of("X", "23", "chrX", "chr23")) {
			assertTrue(ChromosomeUtil.isChrX(chr));
		}

		// Wrong case etc. not valid.
		for (String chr : List.of("x", "023", "  chrX", "CHR23")) {
			assertFalse(ChromosomeUtil.isChrX(chr));
		}

		// Other valid chromosomes not accepted.
		for (String chr : List.of("1", "17", "20")) {
			assertFalse(ChromosomeUtil.isChrX(chr));
		}
	}

	@Test
	public void testIsChrMT() {
		// 'MT' and 'chrMT' are valid.
		for (String chr : List.of("MT", "chrMT")) {
			assertTrue(ChromosomeUtil.isChrMT(chr));
		}

		// Wrong case etc. not valid.
		for (String chr : List.of("mt", "CHRMT", "MT  ")) {
			assertFalse(ChromosomeUtil.isChrMT(chr));
		}

		// Other valid chromosomes not accepted.
		for (String chr : List.of("X", "17", "20")) {
			assertFalse(ChromosomeUtil.isChrMT(chr));
		}
	}

	@Test
	public void testNameToId() {
		record TestCase(String name, int id, boolean success, String errorMessage) {
		}

		List<TestCase> testCases = List.of(
				new TestCase("1", 1, true, null),
				new TestCase("2", 2, true, null),
				new TestCase("13", 13, true, null),
				new TestCase("14", 14, true, null),
				new TestCase("23", 23, true, null),
				new TestCase("X", 23, true, null),

				new TestCase("chr1", 1, true, null),
				new TestCase("chr2", 2, true, null),
				new TestCase("chr13", 13, true, null),
				new TestCase("chr14", 14, true, null),
				new TestCase("chr23", 23, true, null),
				new TestCase("chrX", 23, true, null),

				new TestCase(null, 0, false, "chromosome name must be non-null and non-empty."),
				new TestCase("", 0, false, "chromosome name must be non-null and non-empty."),

				new TestCase("Y", 0, false, "Unrecognized chromosome name:"),
				new TestCase("x", 0, false, "Unrecognized chromosome name:"),
				new TestCase("CHR8", 0, false, "Unrecognized chromosome name:"),
				new TestCase("chrchr9", 0, false, "Unrecognized chromosome name:"),
				new TestCase(" 1", 0, false, "Unrecognized chromosome name:"),
				new TestCase("0", 0, false, "Unrecognized chromosome name:"),
				new TestCase("24", 0, false, "Unrecognized chromosome name:"),
				new TestCase("-5", 0, false, "Unrecognized chromosome name:"));

		for (TestCase testCase : testCases) {
			try {
				int id = ChromosomeUtil.nameToId(testCase.name);

				assertTrue(testCase.success);
				assertEquals(testCase.id, id);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
				assertTrue(e.getMessage().startsWith(testCase.errorMessage));
			}
		}
	}

	@Test
	public void testIdToName() {
		record TestCase(String name, int id, boolean success) {
		}

		List<TestCase> testCases = List.of(
				new TestCase("1", 1, true),
				new TestCase("2", 2, true),
				new TestCase("13", 13, true),
				new TestCase("14", 14, true),
				new TestCase("X", 23, true),

				new TestCase("", -5, false),
				new TestCase("", 0, false),
				new TestCase("", 24, false));

		for (TestCase testCase : testCases) {
			try {
				String name = ChromosomeUtil.idToName(testCase.id);

				assertTrue(testCase.success);
				assertEquals(testCase.name, name);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
			}
		}
	}

	@Test
	public void testEncodeIds() {
		record TestCase(Iterable<Integer> ids, int code, boolean success, String errorMessage) {
		}

		List<TestCase> testCases = List.of(
				// You can send immutable lists
				new TestCase(
						List.of(1, 6, 7, 18, 19, 20, 23),
						0b10011100000000001100001,
						true,
						null),

				// ...and immutable sets
				new TestCase(
						Set.of(2, 4, 6),
						0b101010,
						// 654321
						true,
						null),

				// ...and ArrayLists
				new TestCase(
						new ArrayList<>(List.of(10, 11, 21, 22)),
						0b01100000000011000000000,
						true,
						null),

				// Empty collections are fine
				new TestCase(
						new HashSet<>(Set.of()),
						0,
						true,
						null),

				// Repeated values are ignored
				new TestCase(
						List.of(1, 1, 1, 2, 2, 2, 2),
						0b11,
						true,
						null),

				// Must be in range, above...
				new TestCase(
						Set.of(1, 2, 3, 99),
						0,
						false,
						"Ids must be in range 1 ... 23 (inclusive)"),

				// ...and below
				new TestCase(
						List.of(-13),
						0,
						false,
						"Ids must be in range 1 ... 23 (inclusive)"));

		for (TestCase testCase : testCases) {
			try {
				int code = ChromosomeUtil.encodeIds(testCase.ids);

				assertTrue(testCase.success);
				assertEquals(testCase.code, code);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
				assertTrue(e.getMessage().startsWith(testCase.errorMessage));
			}
		}
	}

	@Test
	public void testEncodeNames() {
		record TestCase(Iterable<String> names, int code, boolean success, String errorMessage) {
		}

		List<TestCase> testCases = List.of(
				// Just the number is fine...
				new TestCase(
						List.of("1", "6", "7", "18", "19", "20", "23"),
						0b10011100000000001100001,
						true,
						null),

				// ...but 'chr' prefix is fine as well.
				new TestCase(
						Set.of("chr2", "chr4", "chr6"),
						0b101010,
						// 654321
						true,
						null),

				// You can even mix!
				new TestCase(
						new ArrayList<>(List.of("10", "chr11", "chr21", "22")),
						0b01100000000011000000000,
						true,
						null),

				// Empty collections are fine
				new TestCase(
						new HashSet<>(Set.of()),
						0,
						true,
						null),

				// Repeated values are ignored
				new TestCase(
						List.of("X", "X", "23", "23", "23"),
						0b10000000000000000000000,
						true,
						null),

				// All values must be valid.
				new TestCase(
						Set.of("1", "2", "3", "CHR8"),
						0,
						false,
						"Unrecognized chromosome name: CHR8"),

				// Case matters
				new TestCase(
						List.of("x", "10"),
						0,
						false,
						"Unrecognized chromosome name: x"),

				// Whitespace matters
				new TestCase(
						List.of("20", "21  ", "22"),
						0,
						false,
						"Unrecognized chromosome name: 21  "));

		for (TestCase testCase : testCases) {
			try {
				int code = ChromosomeUtil.encodeNames(testCase.names);

				assertTrue(testCase.success);
				assertEquals(testCase.code, code);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
				assertTrue(e.getMessage().startsWith(testCase.errorMessage));
			}
		}
	}

	@Test
	public void testDecodeIds() {
		record TestCase(int code, Set<Integer> ids, boolean success, String errorMessage) {
		}

		List<TestCase> testCases = List.of(
				// Each bit becomes a chromosome ID by position
				new TestCase(
						0b10011100000000001100001,
						Set.of(1, 6, 7, 18, 19, 20, 23),
						true,
						null),

				// 0 -> empty set
				new TestCase(
						0,
						Set.of(),
						true,
						null),

				// Only bits in range 0...22 should be set.
				new TestCase(
						-1,
						null,
						false,
						"Code is out of range: -1"),

				// Only bits in range 0...22 should be set.
				new TestCase(
						1 << 23,
						null,
						false,
						"Code is out of range: 8388608"));

		for (TestCase testCase : testCases) {
			try {
				Set<Integer> ids = ChromosomeUtil.decodeIds(testCase.code);

				assertTrue(testCase.success);
				assertEquals(testCase.ids, ids);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
				assertTrue(e.getMessage().startsWith(testCase.errorMessage));
			}
		}
	}

	@Test
	public void testDecodeNames() {
		record TestCase(int code, Set<String> names, boolean success, String errorMessage) {
		}

		List<TestCase> testCases = List.of(
				// Each bit becomes a chromosome ID by position
				new TestCase(
						0b10011100000000001100001,
						Set.of("1", "6", "7", "18", "19", "20", "X"),
						true,
						null),

				// 0 -> empty set
				new TestCase(
						0,
						Set.of(),
						true,
						null),

				// Only bits in range 0...22 should be set.
				new TestCase(
						-1,
						null,
						false,
						"Code is out of range: -1"),

				// Only bits in range 0...22 should be set.
				new TestCase(
						1 << 23,
						null,
						false,
						"Code is out of range: 8388608"));

		for (TestCase testCase : testCases) {
			try {
				Set<String> names = ChromosomeUtil.decodeNames(testCase.code);

				assertTrue(testCase.success);
				assertEquals(testCase.names, names);

			} catch (IllegalArgumentException e) {

				assertFalse(testCase.success);
				assertTrue(e.getMessage().startsWith(testCase.errorMessage));
			}
		}
	}
}
