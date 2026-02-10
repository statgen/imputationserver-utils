package genepi.imputationserver.steps.vcf;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import genepi.io.text.LineReader;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.variant.vcf.VCFCodec;

public final class VcfFileUtil {

    private VcfFileUtil() {
    }

    public static VcfFile load(String vcfFilename, int chunksize, boolean createIndex) throws IOException {
        if (vcfFilename == null) {
            throw new IllegalArgumentException("vcfFilename must be non-null.");
        }

        if (chunksize < 1) {
            throw new IllegalArgumentException(("chunksize must be strictly positive."));
        }

        Set<Integer> chunks = new HashSet<>();
        Set<String> chromosomes = new HashSet<>();
        Set<String> rawChromosomes = new HashSet<>();
        int noSnps = 0;
        int noSamples;

        try {
            VCFFileReader reader = new VCFFileReader(new File(vcfFilename), false);
            noSamples = reader.getFileHeader().getGenotypeSamples().size();
            reader.close();

            LineReader lineReader = new LineReader(vcfFilename);
            boolean phased = true;

            while (lineReader.next()) {
                String line = lineReader.get();

                if (!line.startsWith("#")) {
                    String[] tiles = line.split("\t", 10);

                    if (tiles.length < 3) {
                        throw new IOException("The provided VCF file is not tab-delimited");
                    }

                    String chromosome = tiles[0];
                    rawChromosomes.add(chromosome);
                    chromosome = chromosome.replaceAll("chr", "");
                    int position = Integer.parseInt(tiles[1]);

                    if (phased) {
                        boolean containsSymbol = tiles[9].contains("/");
                        if (containsSymbol) {
                            phased = false;
                        }
                    }

                    chromosomes.add(chromosome);
                    if (chromosomes.size() > 1) {
                        throw new IOException(
                                "The provided VCF file contains more than one chromosome. Please split your input VCF file by chromosome");
                    }

                    String ref = tiles[3];
                    String alt = tiles[4];

                    if (ref.equals(alt)) {
                        throw new IOException("The provided VCF file is malformed at variation " + tiles[2]
                                + ": reference allele (" + ref + ") and alternate allele  (" + alt + ") are the same.");
                    }

                    int chunk = position / chunksize;
                    if (position % chunksize == 0) {
                        chunk = chunk - 1;
                    }
                    chunks.add(chunk);
                    noSnps++;

                } else {
                    if (line.startsWith("#CHROM")) {
                        String[] tiles = line.split("\t");

                        // check sample names, stop when not unique
                        HashSet<String> samples = new HashSet<>();

                        for (String sample : tiles) {
                            if (samples.contains(sample)) {
                                reader.close();
                                throw new IOException("Two individuals or more have the following ID: " + sample);
                            }
                            samples.add(sample);
                        }
                    }
                }
            }

            lineReader.close();

            // create index
            if (createIndex && !new File(vcfFilename + ".tbi").exists()) {
                try {
                    createIndex(vcfFilename);
                } catch (Exception e) {
                    throw new IOException(
                            "The provided VCF file is malformed. Error during index creation: " + e.getMessage());
                }
            }

            VcfFile pair = new VcfFile();
            pair.setVcfFilename(vcfFilename);
            pair.setIndexFilename(vcfFilename + ".tbi");
            pair.setNoSnps(noSnps);
            pair.setNoSamples(noSamples);
            pair.setChunks(chunks);
            pair.setChromosomes(chromosomes);

            boolean hasChrPrefix = false;
            for (String chromosome : rawChromosomes) {
                if (chromosome.startsWith("chr")) {
                    hasChrPrefix = true;
                    break;
                }
            }

            pair.setRawChromosomes(rawChromosomes);
            pair.setChrPrefix(hasChrPrefix);
            pair.setPhased(phased);
            pair.setChunkSize(chunksize);

            return pair;

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private static final Set<String> validChromosomes = Set.of(
            "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "20", "21", "22", "23", "X",

            "chr1", "chr2", "chr3", "chr4", "chr5", "chr6", "chr7", "chr8", "chr9",
            "chr10", "chr11", "chr12", "chr13", "chr14", "chr15", "chr16", "chr17", "chr18", "chr19",
            "chr20", "chr21", "chr22", "chr23", "chrX"
    );

    public static boolean isValidChromosome(String chromosome) {
        return validChromosomes.contains(chromosome);
    }

    public static boolean isChrX(String chromosome) {
        return chromosome.equals("X")
                || chromosome.equals("23")
                || chromosome.equals("chrX")
                || chromosome.equals("chr23");
    }

    public static boolean isChrMT(String chromosome) {
        return chromosome.equals("MT") || chromosome.equals("chrMT");
    }

    public static void createIndex(String vcfFilename, boolean force) throws IOException {
        File index = new File(vcfFilename + ".tbi");

        if (force) {
            if (index.exists()) {
                index.delete();
            }
        }

        if (!index.exists()) {
            createIndex(vcfFilename);
        }
    }

    public static void createIndex(String vcfFilename) throws IOException {
        TabixIndex index = IndexFactory.createTabixIndex(new File(vcfFilename), new VCFCodec(), TabixFormat.VCF, null);
        index.writeBasedOnFeatureFile(new File(vcfFilename));
    }
}