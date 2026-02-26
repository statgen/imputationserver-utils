package genepi.imputationserver;

public final class BuildInfo {
	private BuildInfo() {}

	public static final String APP = "${project.artifactId}";
	public static final String VERSION = "${project.version}";
	public static final String URL = "${url}";
}
