package genepi.imputationserver.util;

import genepi.imputationserver.steps.fastqc.legend.SitesEntry;
import genepi.imputationserver.steps.vcf.MinimalVariantContext;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GenomicToolsTest {

	@Test
	public void testIsValid() {
		record TestCase(String allele, boolean result) {
		}

		List<TestCase> testCases = List.of(
				new TestCase("A", true),
				new TestCase("a", true),
				new TestCase(" A", false),
				new TestCase("C", true),
				new TestCase("c", true),
				new TestCase("CC", false),
				new TestCase("G", true),
				new TestCase("g", true),
				new TestCase("g\t", false),
				new TestCase("T", true),
				new TestCase("Y", false),
				new TestCase("", false),
				new TestCase("ABC", false));

		for (TestCase testCase : testCases) {
			boolean observed = GenomicTools.isValid(testCase.allele);
			assertEquals(testCase.result, observed);
		}
	}

	@Test
	public void testMatch() {
		record TestCase(String snpRef, String snpAlt, char entryRef, char entryAlt, boolean matches) {
		}

		List<TestCase> testCases = List.of(
				// If we match ref <-> ref and alt <-> alt, it works.
				new TestCase("G", "A", 'G', 'A', true),
				new TestCase("T", "C", 'T', 'C', true),

				// Anything after the first character in the SNP is inconsequential.
				new TestCase("GG", "Asdfg ", 'G', 'A', true),

				// Doesn't work if they are flipped.
				new TestCase("G", "A", 'A', 'G', false),
				// Doesn't work if ref doesn't match.
				new TestCase("G", "A", 'T', 'A', false),
				// Doesn't work if alt doesn't match.
				new TestCase("G", "A", 'G', 'C', false),
				// Case matters.
				new TestCase("G", "A", 'g', 'a', false),
				// Leading whitespace matters.
				new TestCase(" G", "A", 'G', 'A', false));

		for (TestCase testCase : testCases) {
			MinimalVariantContext snp = new MinimalVariantContext(1);
			snp.setReferenceAllele(testCase.snpRef);
			snp.setAlternateAllele(testCase.snpAlt);

			SitesEntry refEntry = new SitesEntry();
			refEntry.setRefAllele(testCase.entryRef);
			refEntry.setAltAllele(testCase.entryAlt);

			boolean matches = GenomicTools.match(snp, refEntry);

			assertEquals(testCase.matches, matches);
		}
	}

	@Test
	public void testAlleleSwitch() {
		record TestCase(String snpRef, String snpAlt, char entryRef, char entryAlt, boolean isSwitched) {
		}

		List<TestCase> testCases = List.of(
				// If we match ref <-> alt and alt <-> ref, it works.
				new TestCase("G", "A", 'A', 'G', true),
				new TestCase("T", "C", 'C', 'T', true),

				// Anything after the first character in the SNP is inconsequential.
				new TestCase("GG", "Asdfg ", 'A', 'G', true),

				// Doesn't work if they are in matching order.
				new TestCase("G", "A", 'G', 'A', false),
				// Doesn't work if ref doesn't match alt.
				new TestCase("G", "A", 'C', 'G', false),
				// Doesn't work if alt doesn't match ref.
				new TestCase("G", "A", 'A', 'T', false),
				// Case matters.
				new TestCase("G", "A", 'a', 'g', false),
				// Leading whitespace matters.
				new TestCase(" G", "A", 'A', 'G', false));

		for (TestCase testCase : testCases) {
			MinimalVariantContext snp = new MinimalVariantContext(1);
			snp.setReferenceAllele(testCase.snpRef);
			snp.setAlternateAllele(testCase.snpAlt);

			SitesEntry refEntry = new SitesEntry();
			refEntry.setRefAllele(testCase.entryRef);
			refEntry.setAltAllele(testCase.entryAlt);

			boolean isSwitched = GenomicTools.alleleSwitch(snp, refEntry);

			assertEquals(testCase.isSwitched, isSwitched);
		}
	}

	@Test
	public void testStrandFlip() {
		record TestCase(String snpRef, String snpAlt, char entryRef, char entryAlt, boolean isStrandFlip) {
		}

		List<TestCase> testCases = List.of(
				// GA matches CT
				new TestCase("G", "A", 'C', 'T', true),
				// AC matches TG
				new TestCase("A", "C", 'T', 'G', true),
				// CA matches GT
				new TestCase("C", "A", 'G', 'T', true),
				// CT matches GA
				new TestCase("C", "T", 'G', 'A', true),

				// Unlike in match() and switch(), the following characters in SNP matter.
				new TestCase("GG", "Asdfg ", 'C', 'T', false),

				// Doesn't work if we flip the order
				new TestCase("G", "A", 'T', 'C', false),
				// Doesn't work if ref is not inverse of ref.
				new TestCase("G", "A", 'G', 'T', false),
				// Doesn't work if alt is not inverse of alt
				new TestCase("G", "A", 'T', 'T', false),
				// Case matters.
				new TestCase("G", "A", 'c', 't', false),
				// Leading whitespace matters.
				new TestCase(" G", "A", 'C', 'T', false));

		for (TestCase testCase : testCases) {
			MinimalVariantContext snp = new MinimalVariantContext(1);
			snp.setReferenceAllele(testCase.snpRef);
			snp.setAlternateAllele(testCase.snpAlt);

			SitesEntry refEntry = new SitesEntry();
			refEntry.setRefAllele(testCase.entryRef);
			refEntry.setAltAllele(testCase.entryAlt);

			boolean isStrandFlip = GenomicTools.strandFlip(snp, refEntry);

			assertEquals(testCase.isStrandFlip, isStrandFlip);
		}
	}

    @Test
    public void testStrandFlipAndAlleleSwitch() {
        record TestCase(String snpRef, String snpAlt, char entryRef, char entryAlt, boolean isFlippedAndSwitched) {
        }

        List<TestCase> testCases = List.of(
                // AC matches GT
                new TestCase("A", "C", 'G', 'T', true),
                // CA matches TG
                new TestCase("C", "A", 'T', 'G', true),
                // AG matches CT
                new TestCase("A", "G", 'C', 'T', true),
                // GA matches TC
                new TestCase("G", "A", 'T', 'C', true),
                // TG matches CA
                new TestCase("T", "G", 'C', 'A', true),
                // GT matches AC
                new TestCase("G", "T", 'A', 'C', true),
                // CT matches AG
                new TestCase("C", "T", 'A', 'G', true),
                // TC matches GA
                new TestCase("T", "C", 'G', 'A', true),

                // Like strandFlip(), the following characters in SNP matter.
                new TestCase("GG", "Asdfg ", 'T', 'C', false),

                // Doesn't work if we flip the order (i.e., only strand-flip, no allele switch).
                new TestCase("G", "A", 'C', 'T', false),

                // Doesn't work if it's only an allele switch (no strand flip).
                new TestCase("G", "A", 'A', 'G', false),

                // Case matters.
                new TestCase("G", "A", 't', 'c', false),

                // Leading whitespace matters.
                new TestCase(" G", "A", 'T', 'C', false));

        for (TestCase testCase : testCases) {
            MinimalVariantContext snp = new MinimalVariantContext(1);
            snp.setReferenceAllele(testCase.snpRef);
            snp.setAlternateAllele(testCase.snpAlt);

            SitesEntry refEntry = new SitesEntry();
            refEntry.setRefAllele(testCase.entryRef);
            refEntry.setAltAllele(testCase.entryAlt);

            boolean isFlippedAndSwitched = GenomicTools.strandFlipAndAlleleSwitch(snp, refEntry);

            assertEquals(testCase.isFlippedAndSwitched, isFlippedAndSwitched);
        }
    }
}
