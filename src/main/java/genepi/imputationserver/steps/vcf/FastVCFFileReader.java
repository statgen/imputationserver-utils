package genepi.imputationserver.steps.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import genepi.io.FileUtil;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

public class FastVCFFileReader {

	private final List<String> samples;

	private int snpsCount = 0;

	private final int samplesCount;

	private MinimalVariantContext variantContext;

	private final List<String> header = new ArrayList<>();

	private final String filename;

	protected BufferedReader in;

	private int lineNumber;

    private final VCFLineParser parser;

	public FastVCFFileReader(String filename) throws IOException {
		// load header
		VCFFileReader reader = new VCFFileReader(new File(filename), false);
		VCFHeader header = reader.getFileHeader();
		samples = header.getGenotypeSamples();
		samplesCount = samples.size();
		variantContext = new MinimalVariantContext(samplesCount);
		reader.close();

		parser = new VCFLineParser(samplesCount);

		this.filename = filename;
		FileInputStream inputStream = new FileInputStream(filename);
		InputStream in2 = FileUtil.decompressStream(inputStream);
		this.in = new BufferedReader(new InputStreamReader(in2));
	}

	public List<String> getGenotypedSamples() {
		return samples;
	}

	public MinimalVariantContext getVariantContext() {
		return variantContext;
	}

	public int getSnpsCount() {
		return snpsCount;
	}

	public int getSamplesCount() {
		return samplesCount;
	}

	public boolean next() throws IOException {
		while(true) {
            String line;

            if ((line = this.in.readLine()) != null) {
				try {
					this.lineNumber++;
					if (line.trim().isEmpty()) {
						continue;
					}

					// Check if the line starts with '#' and skip processing for header lines
					if (line.startsWith("#")) {
						header.add(line);
						continue;
					}

					// Parse non-header lines
					this.parseLine(line);
					return true;
				} catch (Exception var2) {
					throw new IOException(this.filename + ": Line " + this.lineNumber + ": " + var2.getMessage());
				}
			}

			return false;
		}
	}

	protected void parseLine(String line) throws IOException {
		variantContext = parser.parseLine(line);
		if (variantContext.getNSamples() != samplesCount) {
			throw new IOException("Line " + lineNumber + ": different number of samples.");
		}
		snpsCount++;
	}

	public void close() throws IOException {
		in.close();
	}

	public List<String> getFileHeader() {
		return header;
	}
}
