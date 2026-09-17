package genepi.imputationserver;

import genepi.imputationserver.steps.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = BuildInfo.APP_NAME, version = BuildInfo.VERSION)
public class App {

	private static final String COPYRIGHT = "(c) 2023-2026 Imputation Server Team";

	public static void main(String[] args) {
		System.err.println();
		System.err.println(BuildInfo.APP_NAME + " " + BuildInfo.VERSION + " (" + BuildInfo.COMMIT_ID_SHORT
				+ (BuildInfo.COMMIT_DIRTY ? "*" : "") + ")");
		System.err.println(BuildInfo.URL);
		System.err.println(COPYRIGHT);
		System.err.println();

		CommandLine commandLine = new CommandLine(new App());
		commandLine.addSubcommand("validate", new InputValidationCommand());
		commandLine.addSubcommand("run-qc", new QualityControlCommand());
		commandLine.addSubcommand("estimate-ancestry", new EstimateAncestryCommand());
		commandLine.addSubcommand("prepare-trace", new PrepareTraceCommand());
		commandLine.addSubcommand("version", new VersionCommand());
		commandLine.setExecutionStrategy(new CommandLine.RunLast());
		int result = commandLine.execute(args);
		System.exit(result);
	}
}
