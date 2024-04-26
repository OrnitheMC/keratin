package net.ornithemc.keratin.task;

import java.io.File;

import javax.inject.Inject;

import net.fabricmc.stitch.commands.CommandSplitTiny;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.task.DownloadAndExtractJarTask;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class DownloadIntermediaryGen2Task extends DownloadAndExtractJarTask {

	@Inject
	public DownloadIntermediaryGen2Task() {
		getOutput().set(getProject().provider(() -> getExtension().getFiles().getMergedIntermediaryMappings()));
	}

	@Override
	public void run() throws Exception {
		getProject().getLogger().lifecycle(":downloading intermediary");

		super.run();

		KeratinGradleExtension keratin = getExtension();
		VersionDetails details = keratin.getVersionDetails();

		if (!details.sharedMappings()) {
			File merged = keratin.getFiles().getMergedIntermediaryMappings();
			File client = keratin.getFiles().getClientIntermediaryMappings();
			File server = keratin.getFiles().getServerIntermediaryMappings();

			boolean splitClient = (details.client() && (!client.exists() || isRefreshDependencies()));
			boolean splitServer = (details.server() && (!server.exists() || isRefreshDependencies()));

			if (splitClient || splitServer) {
				new CommandSplitTiny().run(new String[] {
					merged.getAbsolutePath(),
					details.client() ? client.getAbsolutePath() : "-",
					details.server() ? server.getAbsolutePath() : "-"
				});
			}
		}
	}
}
