package genepi.imputationserver.steps;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command
public class VersionCommand  implements Callable<Integer> {
	@Override
	public Integer call() {
		return 0;
	}
}
