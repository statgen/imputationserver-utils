package genepi.imputationserver.util;

import genepi.imputationserver.steps.fastqc.SnpStats;
import genepi.imputationserver.steps.fastqc.legend.SitesEntry;
import genepi.imputationserver.steps.vcf.MinimalVariantContext;

public class GenomicTools {

	private static final String GC = "GC";
	private static final String CG = "CG";
	private static final String TA = "TA";
	private static final String AT = "AT";
	private static final String CT = "CT";
	private static final String GA = "GA";
	private static final String TC = "TC";
	private static final String AG = "AG";
	private static final String GT = "GT";
	private static final String CA = "CA";
	private static final String TG = "TG";
	private static final String AC = "AC";
	private static final String T = "T";
	private static final String G = "G";
	private static final String C = "C";
	private static final String A = "A";

	public static boolean isValid(String allele) {
		return allele.equalsIgnoreCase(A)
                || allele.equalsIgnoreCase(C)
                || allele.equalsIgnoreCase(G)
				|| allele.equalsIgnoreCase(T);
	}

	public static boolean match(MinimalVariantContext snp, SitesEntry refEntry) {
		char studyRef = snp.getReferenceAllele().charAt(0);
		char studyAlt = snp.getAlternateAllele().charAt(0);
		char legendRef = refEntry.getRefAllele();
		char legendAlt = refEntry.getAltAllele();

		if (studyRef == legendRef && studyAlt == legendAlt) {
			return true;
		}

		return false;
	}

	public static boolean alleleSwitch(MinimalVariantContext snp, SitesEntry refEntry) {
		char studyRef = snp.getReferenceAllele().charAt(0);
		char studyAlt = snp.getAlternateAllele().charAt(0);

		char legendRef = refEntry.getRefAllele();
		char legendAlt = refEntry.getAltAllele();

		// all simple cases
		if (studyRef == legendAlt && studyAlt == legendRef) {
			return true;
		}

		return false;
	}

	public static boolean strandFlip(MinimalVariantContext snp, SitesEntry refEntry) {
		String studyGenotype = snp.getGenotype();
		String referenceGenotype = refEntry.getGenotype();

        return switch (studyGenotype) {
            case AC -> referenceGenotype.equals(TG);
            case CA -> referenceGenotype.equals(GT);
            case AG -> referenceGenotype.equals(TC);
            case GA -> referenceGenotype.equals(CT);
            case TG -> referenceGenotype.equals(AC);
            case GT -> referenceGenotype.equals(CA);
            case CT -> referenceGenotype.equals(GA);
            case TC -> referenceGenotype.equals(AG);
            default -> false;
        };
    }

	public static boolean complicatedGenotypes(MinimalVariantContext snp, SitesEntry refEntry) {
		String studyGenotype = snp.getGenotype();
		String referenceGenotype = refEntry.getGenotype();

		if ((studyGenotype.equals(AT) || studyGenotype.equals(TA))
				&& (referenceGenotype.equals(AT) || referenceGenotype.equals(TA))) {

			return true;

		} else if ((studyGenotype.equals(CG) || studyGenotype.equals(GC))
				&& (referenceGenotype.equals(CG) || referenceGenotype.equals(GC))) {

			return true;

		}

		return false;
	}

	public static boolean strandFlipAndAlleleSwitch(MinimalVariantContext snp, SitesEntry refEntry) {
		String studyGenotype = snp.getGenotype();
		String referenceGenotype = refEntry.getGenotype();

        return switch (studyGenotype) {
            case AC -> referenceGenotype.equals(GT);
            case CA -> referenceGenotype.equals(TG);
            case AG -> referenceGenotype.equals(CT);
            case GA -> referenceGenotype.equals(TC);
            case TG -> referenceGenotype.equals(CA);
            case GT -> referenceGenotype.equals(AC);
            case CT -> referenceGenotype.equals(AG);
            case TC -> referenceGenotype.equals(GA);
            default -> false;
        };
    }

	public static ChiSquareObject chiSquare(MinimalVariantContext snp, SitesEntry refSnp, boolean strandSwap,
											int size) {

		// calculate allele frequency
		double chisq = 0;

		int refN = size;

		double refA = refSnp.getRefFrequency();
		double refB = refSnp.getAltFrequency();

		int majorAlleleCount;
		int minorAlleleCount;

		if (!strandSwap) {
			majorAlleleCount = snp.getHomRefCount();
			minorAlleleCount = snp.getHomVarCount();
		} else {
			majorAlleleCount = snp.getHomVarCount();
			minorAlleleCount = snp.getHomRefCount();
		}

		int countRef = snp.getHetCount() + majorAlleleCount * 2;
		int countAlt = snp.getHetCount() + minorAlleleCount * 2;

		double p = countRef / (double) (countRef + countAlt);
		double q = countAlt / (double) (countRef + countAlt);
		double studyN = (snp.getNSamples() - snp.getNoCallCount()) * 2;

		double totalQ = q * studyN + refB * refN;
		double expectedQ = totalQ / (studyN + refN) * studyN;
		double deltaQ = q * studyN - expectedQ;

		chisq += (Math.pow(deltaQ, 2) / expectedQ) + (Math.pow(deltaQ, 2) / (totalQ - expectedQ));

		double totalP = p * studyN + refA * refN;
		double expectedP = totalP / (studyN + refN) * studyN;
		double deltaP = p * studyN - expectedP;

		chisq += (Math.pow(deltaP, 2) / expectedP) + (Math.pow(deltaP, 2) / (totalP - expectedP));

		return new ChiSquareObject(chisq, p, q);
	}

	public static boolean alleleMismatch(MinimalVariantContext snp, SitesEntry refEntry) {
		char studyRef = snp.getReferenceAllele().charAt(0);
		char studyAlt = snp.getAlternateAllele().charAt(0);
		char legendRef = refEntry.getRefAllele();
		char legendAlt = refEntry.getAltAllele();

		return studyRef != legendRef || studyAlt != legendAlt;
	}

	public static SnpStats calculateAlleleFreq(MinimalVariantContext snp, SitesEntry refSnp, int size) {

		boolean strandSwap = GenomicTools.strandFlipAndAlleleSwitch(snp, refSnp)
				|| GenomicTools.alleleSwitch(snp, refSnp);

		// calculate allele frequency
		SnpStats output = new SnpStats();

		int position = snp.getStart();

		ChiSquareObject chiObj = GenomicTools.chiSquare(snp, refSnp, strandSwap, size);

		char majorAllele;
		char minorAllele;

		if (!strandSwap) {
			majorAllele = snp.getReferenceAllele().charAt(0);
			minorAllele = snp.getAlternateAllele().charAt(0);
		} else {
			majorAllele = snp.getAlternateAllele().charAt(0);
			minorAllele = snp.getReferenceAllele().charAt(0);
		}

		output.setType("SNP");
		output.setPosition(position);
		output.setChromosome(snp.getContig());
		output.setRefFrequencyA(refSnp.getRefFrequency());
		output.setRefFrequencyB(refSnp.getAltFrequency());
		output.setFrequencyA((float) chiObj.getP());
		output.setFrequencyB((float) chiObj.getQ());
		output.setChisq(chiObj.getChisq());
		output.setAlleleA(majorAllele);
		output.setAlleleB(minorAllele);
		output.setRefAlleleA(refSnp.getRefAllele());
		output.setRefAlleleB(refSnp.getAltAllele());
		output.setOverlapWithReference(true);

		return output;
	}
}
