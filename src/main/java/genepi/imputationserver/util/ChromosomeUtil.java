package genepi.imputationserver.util;

import java.util.*;

public final class ChromosomeUtil {

	private ChromosomeUtil() {
	}

	private static final Map<String, Integer> NAME_TO_ID = new HashMap<>();

	private static final Map<Integer, String> ID_TO_NAME = new HashMap<>();

	static {
		NAME_TO_ID.put("1", 1);
		NAME_TO_ID.put("2", 2);
		NAME_TO_ID.put("3", 3);
		NAME_TO_ID.put("4", 4);
		NAME_TO_ID.put("5", 5);
		NAME_TO_ID.put("6", 6);
		NAME_TO_ID.put("7", 7);
		NAME_TO_ID.put("8", 8);
		NAME_TO_ID.put("9", 9);
		NAME_TO_ID.put("10", 10);
		NAME_TO_ID.put("11", 11);
		NAME_TO_ID.put("12", 12);
		NAME_TO_ID.put("13", 13);
		NAME_TO_ID.put("14", 14);
		NAME_TO_ID.put("15", 15);
		NAME_TO_ID.put("16", 16);
		NAME_TO_ID.put("17", 17);
		NAME_TO_ID.put("18", 18);
		NAME_TO_ID.put("19", 19);
		NAME_TO_ID.put("20", 20);
		NAME_TO_ID.put("21", 21);
		NAME_TO_ID.put("22", 22);
		NAME_TO_ID.put("23", 23);
		NAME_TO_ID.put("X", 23);

		for (Map.Entry<String, Integer> entry : NAME_TO_ID.entrySet()) {
			if (entry.getKey().equals("23")) {
				continue; // Skip (we prefer "X")
			}
			ID_TO_NAME.put(entry.getValue(), entry.getKey());
		}
	}

	private static final Set<String> VALID_CHROMOSOMES = Set.of(
			"1", "2", "3", "4", "5", "6", "7", "8", "9",
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
			"20", "21", "22", "23", "X",

			"chr1", "chr2", "chr3", "chr4", "chr5", "chr6", "chr7", "chr8", "chr9",
			"chr10", "chr11", "chr12", "chr13", "chr14", "chr15", "chr16", "chr17", "chr18", "chr19",
			"chr20", "chr21", "chr22", "chr23", "chrX");

	/**
	 * Returns {@code true} if {@code chromosome} is a valid chromosome name, as
	 * specified by the {@code VALID_CHROMOSOMES} set. Returns {@code false}
	 * otherwise.
	 */
	public static boolean isValidChromosome(String chromosome) {
		return VALID_CHROMOSOMES.contains(chromosome);
	}

	/**
	 * Returns {@code true} if {@code chromosome} is a valid name for the X
	 * chromosome, {@code false} otherwise.
	 */
	public static boolean isChrX(String chromosome) {
		return chromosome.equals("X")
				|| chromosome.equals("23")
				|| chromosome.equals("chrX")
				|| chromosome.equals("chr23");
	}

	/**
	 * Returns {@code true} if {@code chromosome} is a valid name for the MT
	 * (mitochondrial) chromosome, {@code false} otherwise.
	 */
	public static boolean isChrMT(String chromosome) {
		return chromosome.equals("MT") || chromosome.equals("chrMT");
	}

	/**
	 * Maps a valid chromosome string (e.g., "1", "2", "21", "23", "X") to a
	 * chromosome ID in range 1...23
	 */
	public static int nameToId(String chromosome) {
		if (chromosome == null || chromosome.isEmpty()) {
			throw new IllegalArgumentException("chromosome name must be non-null and non-empty.");
		}

		String trimmed = chromosome.replaceFirst("chr", "");

		if (NAME_TO_ID.containsKey(trimmed)) {
			return NAME_TO_ID.get(trimmed);
		}

		throw new IllegalArgumentException("Unrecognized chromosome name: " + chromosome);
	}

	/**
	 * Maps a valid chromosome ID (range 1...23) to a chromosome string (e.g., "1",
	 * "2", "21", "23", "X").
	 */
	public static String idToName(int id) {
		if (ID_TO_NAME.containsKey(id)) {
			return ID_TO_NAME.get(id);
		}

		throw new IllegalArgumentException("Unrecognized chromosome ID: " + id);
	}

	/**
	 * Encodes the presence or absence of each chromosome in range 1...23 into the
	 * bits of an integer. E.g., the chromosome ID set {@code { 1, 2, 4 }} becomes
	 * the code {@code 0b1011 == 11}.
	 */
	public static int encodeIds(Iterable<Integer> ids) {
		int code = 0;

		for (int id : ids) {
			if (id < 1 || id > 23) {
				throw new IllegalArgumentException("Ids must be in range 1 ... 23 (inclusive); found: " + id);
			}
			code = code | (1 << (id - 1));
		}

		return code;
	}

	/**
	 * Encodes the presence or absence of each chromosome into the bits of an
	 * integer. E.g., the chromosome name set {@code { "1", "2", "4" }} becomes the
	 * code {@code 0b1011 == 11}.
	 */
	public static int encodeNames(Iterable<String> names) {
		Set<Integer> ids = new HashSet<>();

		for (String name : names) {
			ids.add(nameToId(name));
		}

		return encodeIds(ids);
	}

	/**
	 * Decodes the provided chromosome code into a set of present chromosome IDs.
	 * E.g., the code {@code 11 == 0b1011} becomes the set {@code { 1, 2, 4 }}.
	 */
	public static Set<Integer> decodeIds(int code) {
		Set<Integer> ids = new HashSet<>();

		// 0x7fffff = First 23 bits set => we invert to get bits that should be 0.
		final int bitmask = ~0x7fffff;
		if ((code & bitmask) != 0) {
			throw new IllegalArgumentException("Code is out of range: " + code);
		}

		for (int i = 1; i <= 23; i++) {
			int bit = (code >> (i - 1)) & 1;
			if (bit == 1) {
				ids.add(i);
			}
		}

		return ids;
	}

	/**
	 * Decodes the provided chromosome code into a set of present chromosome names.
	 * E.g., the code {@code 11 == 0b1011} becomes the set {@code { "1", "2", "4" }}.
	 */
	public static Set<String> decodeNames(int code) {
		Set<String> names = new HashSet<>();

		for (int id : decodeIds(code)) {
			names.add(idToName(id));
		}

		return names;
	}
}
