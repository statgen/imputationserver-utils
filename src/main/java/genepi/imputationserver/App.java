package genepi.imputationserver;

import genepi.imputationserver.steps.EstimateAncestryCommand;
import genepi.imputationserver.steps.InputValidationCommand;
import genepi.imputationserver.steps.QualityControlCommand;
import genepi.imputationserver.steps.PrepareTraceCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = BuildInfo.APP, version = BuildInfo.VERSION)
public class App {

	private static final String COPYRIGHT = "(c) 2023-2026 Imputation Server Team";

	public static void main(String[] args) {
		System.err.println();
		System.err.println(BuildInfo.APP + " " + BuildInfo.VERSION);
		System.err.println(BuildInfo.URL);
		System.err.println(COPYRIGHT);
		System.err.println();

		CommandLine commandLine = new CommandLine(new App());
		commandLine.addSubcommand("validate", new InputValidationCommand());
		commandLine.addSubcommand("run-qc", new QualityControlCommand());
		commandLine.addSubcommand("estimate-ancestry", new EstimateAncestryCommand());
		commandLine.addSubcommand("prepare-trace", new PrepareTraceCommand());
		commandLine.setExecutionStrategy(new CommandLine.RunLast());
		int result = commandLine.execute(args);
		System.exit(result);
	}
}
