package genepi.imputationserver.steps.vcf;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class VcfFileUtilTest {

	@Test
	public void testLoad() {

		record LoadTestCase(
				String path,
				int chunkSize,
				boolean createIndex,
				String chromosome,
				boolean phased,
				int samples,
				boolean hasChrPrefix,
				String errorPrefix) {
		}

		List<LoadTestCase> cases = List.of(
				new LoadTestCase(
						"test-data/data/chr20-phased/chr20.R50.merged.1.330k.recode.small.vcf.gz",
						10_000_000,
						false,
						"20",
						true,
						51,
						false,
						null),
				new LoadTestCase(
						"test-data/data/chr20-unphased/chr20.R50.merged.1.330k.recode.unphased.small.vcf.gz",
						10_000_000,
						false,
						"20",
						false,
						51,
						false,
						null),
				new LoadTestCase(
						"test-data/data/chrX-phased/small.chrX.vcf.gz",
						10_000_000,
						false,
						"X",
						true,
						51,
						false,
						null),
				new LoadTestCase(
						"test-data/data/chr20-phased-hg38/chr20.R50.merged.1.330k.recode.small.hg38.vcf.gz",
						10_000_000,
						false,
						"20",
						true,
						51,
						true,
						null),
				new LoadTestCase(
						"test-data/data/wrong_chrs/minimac_test.50.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"The provided VCF file contains more than one chromosome."),
				new LoadTestCase(
						"test-data/data/wrong_vcf/emptry.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"Unable to parse header with error:"),
				new LoadTestCase(
						"",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"Unable to parse header with error:"),
				new LoadTestCase(
						"test-data/data/wrong_columns/only.2.cols.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"The provided VCF file is not tab-delimited"),
				new LoadTestCase(
						"test-data/data/wrong_columns/bad.header.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"Unable to parse header with error:"),
				new LoadTestCase(
						"test-data/data/wrong_columns/repeated.subjects.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"Two individuals or more have the following ID:"),
				new LoadTestCase(
						"test-data/data/wrong_columns/malformed.alleles.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"The provided VCF file is malformed at variation"),
				new LoadTestCase(
						"test-data/data/header/minimac_test.50.vcf.gz",
						10_000_000,
						false,
						null,
						false,
						0,
						false,
						"No genotypes found in the VCF file."));

		for (LoadTestCase testCase : cases) {
			try {
				VcfFile vcf = VcfFileUtil.load(testCase.path, testCase.chunkSize, testCase.createIndex);

				assertNull(testCase.errorPrefix);

				assertNotNull(vcf);
				assertEquals(testCase.path, vcf.getVcfFilename());
				assertEquals(testCase.path + ".tbi", vcf.getIndexFilename());
				assertEquals(testCase.chunkSize, vcf.getChunkSize());
				assertTrue(vcf.getChunks().size() > 0);
				assertEquals(testCase.chromosome, vcf.getChromosome());
				assertEquals(testCase.phased, vcf.isPhased());
				assertEquals(testCase.hasChrPrefix, vcf.hasChrPrefix());
			} catch (IOException e) {
				assertNotNull(testCase.errorPrefix);
				assertTrue(e.getMessage().startsWith(testCase.errorPrefix));
			}
		}
	}

	@Test
	public void testCreateIndex() {
		// TODO
	}
}
