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
                        null
                ),
                new LoadTestCase(
                        "test-data/data/chr20-unphased/chr20.R50.merged.1.330k.recode.unphased.small.vcf.gz",
                        10_000_000,
                        false,
                        "20",
                        false,
                        51,
                        false,
                        null
                ),
                new LoadTestCase(
                        "test-data/data/chrX-phased/small.chrX.vcf.gz",
                        10_000_000,
                        false,
                        "X",
                        true,
                        51,
                        false,
                        null
                ),
                new LoadTestCase(
                        "test-data/data/chr20-phased-hg38/chr20.R50.merged.1.330k.recode.small.hg38.vcf.gz",
                        10_000_000,
                        false,
                        "20",
                        true,
                        51,
                        true,
                        null
                ),
                new LoadTestCase(
                        "test-data/data/wrong_chrs/minimac_test.50.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "The provided VCF file contains more than one chromosome."
                ),
                new LoadTestCase(
                        "test-data/data/wrong_vcf/emptry.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "Unable to parse header with error:"
                ),
                new LoadTestCase(
                        "",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "Unable to parse header with error:"
                ),
                new LoadTestCase(
                        "test-data/data/wrong_columns/only.2.cols.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "The provided VCF file is not tab-delimited"
                ),
                new LoadTestCase(
                        "test-data/data/wrong_columns/bad.header.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "Unable to parse header with error:"
                ),
                new LoadTestCase(
                        "test-data/data/wrong_columns/repeated.subjects.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "Two individuals or more have the following ID:"
                ),
                new LoadTestCase(
                        "test-data/data/wrong_columns/malformed.alleles.vcf.gz",
                        10_000_000,
                        false,
                        null,
                        false,
                        0,
                        false,
                        "The provided VCF file is malformed at variation"
                )
        );

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
    public void testLoadEmptyFile() throws IOException {
        VcfFile vcf;
        String chr;

        // Loading a header-only file is not an error.
        vcf = VcfFileUtil.load("test-data/data/header/minimac_test.50.vcf.gz", 1, false);
        assertNotNull(vcf);

        // However, trying to get its chromosome is an error.
        try {
            chr = vcf.getChromosome();
            fail();
        } catch (IOException e) {
            assertTrue(e.getMessage().startsWith("No genotypes found in the VCF file."));
        }

        // The same sequence of actions works fine if the VCF file contains exactly on chromosome.
        vcf = VcfFileUtil.load("test-data/data/chr20-phased/chr20.R50.merged.1.330k.recode.small.vcf.gz", 1, false);
        assertNotNull(vcf);

        chr = vcf.getChromosome();
        assertEquals("20", chr);
    }

    @Test
    public void testIsValidChromosome() {
        for (int i = 1; i <= 23; i++) {
            // Numbers 1 ... 23 (inclusive) are valid.
            String chr = String.valueOf(i);
            assertTrue(VcfFileUtil.isValidChromosome(chr));

            // They can also be prepended with 'chr'.
            String withPrefix = "chr" + chr;
            assertTrue(VcfFileUtil.isValidChromosome(withPrefix));
        }

        // X is valid.
        assertTrue(VcfFileUtil.isValidChromosome("X"));
        assertTrue(VcfFileUtil.isValidChromosome("chrX"));

        // Case-sensitive: lowercase x not valid.
        assertFalse(VcfFileUtil.isValidChromosome("x"));
        assertFalse(VcfFileUtil.isValidChromosome("chrx"));

        // Integers out of range or with preceding 0's are not valid.
        for (String chr : List.of("-1", "0", "24", "-15", "99", "04", "0020")) {
            assertFalse(VcfFileUtil.isValidChromosome(chr));
        }
    }

    @Test
    public void testIsChrX() {
        // 23, X, and the same with 'chr' prefix are all valid
        for (String chr : List.of("X", "23", "chrX", "chr23")) {
            assertTrue(VcfFileUtil.isChrX(chr));
        }

        // Wrong case etc. not valid.
        for (String chr : List.of("x", "023", "  chrX", "CHR23")) {
            assertFalse(VcfFileUtil.isChrX(chr));
        }

        // Other valid chromosomes not accepted.
        for (String chr : List.of("1", "17", "20")) {
            assertFalse(VcfFileUtil.isChrX(chr));
        }
    }

    @Test
    public void testIsChrMT() {
        // 'MT' and 'chrMT' are valid.
        for (String chr : List.of("MT", "chrMT")) {
            assertTrue(VcfFileUtil.isChrMT(chr));
        }

        // Wrong case etc. not valid.
        for (String chr : List.of("mt", "CHRMT", "MT  ")) {
            assertFalse(VcfFileUtil.isChrMT(chr));
        }

        // Other valid chromosomes not accepted.
        for (String chr : List.of("X", "17", "20")) {
            assertFalse(VcfFileUtil.isChrMT(chr));
        }
    }

    @Test
    public void testCreateIndex() {
        // TODO
    }
}
