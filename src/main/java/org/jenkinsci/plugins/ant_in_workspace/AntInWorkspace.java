package org.jenkinsci.plugins.ant_in_workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Ant;
import net.sf.json.JSONObject;

/**
 * 
 * Jenkins plugin to provide the Ant from the current Workspace. <br/>
 * This is to support projects, that must use a specific version of Ant that is checked-in with the source code. With
 * this plugin you can use the checked-in Ant for building the current project.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntInWorkspace extends Ant {

	private static final Logger LOGGER = Logger.getLogger(AntInWorkspace.class.getName());

	/** Used to store the Workspace Directory on "perform" */
	private String mWorkspace;

	@DataBoundConstructor
	public AntInWorkspace(String targets, String antName, String antOpts, String buildFile, String properties) {
		super(targets, antName, antOpts, buildFile, properties);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Return the Ant from super or if not defined the Ant from the workspace.
	 */
	public AntInstallation getAnt() {
		AntInstallation retVal = super.getAnt();
		if (retVal != null) {
			LOGGER.log(Level.INFO, "use Ant from super: " + retVal.getHome());
		} else if (mWorkspace == null) {
			LOGGER.log(Level.INFO, "Workspace is not set. Cannot use Ant from workspace.");
		} else {
			LOGGER.log(Level.INFO, "use Ant from workspace");
			retVal = new AntInstallation("Ant In Workspace", mWorkspace + getDescriptor().getAntWorkspaceFolder(),
					null);
		}
		return retVal;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		// Important to store this into member variable first!
		mWorkspace = build.getWorkspace().getRemote();
		mWorkspace = appendSlashIfNecessary(mWorkspace);

		final AntInstallation ant = getAnt();
		if (ant != null && launcher.isUnix()) {
			validateAndmakeAntExecutable(ant);
		}
		return super.perform(build, launcher, listener);
	}

	void validateAndmakeAntExecutable(final AntInstallation pAnt) throws AbortException {
		final File file = new File(pAnt.getHome() + "/bin/ant");
		if (!file.exists()) {
			throw new AbortException("Ant does not exist in Workspace: " + file.getAbsolutePath());
		}

		final Path pathToAntBinary = file.toPath();

		try {
			LOGGER.log(Level.FINE, "Change file permissions: " + pathToAntBinary.toString());
			final Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.add(PosixFilePermission.GROUP_READ);
			perms.add(PosixFilePermission.OTHERS_READ);
			Files.setPosixFilePermissions(pathToAntBinary, perms);
		} catch (IOException e) {
			throw new AbortException("Unable to make Ant executable: " + pathToAntBinary.toString());
		}
	}

	@Extension
	@Symbol("antws")
	public static class DescriptorImpl extends Ant.DescriptorImpl {

		private String antWorkspaceFolder;

		public String getDisplayName() {
			return "Invoke Ant In Workspace";
		}

		/**
		 * No Choices should be given. We choose automatically the Ant from the Workspace.
		 */
		public AntInstallation[] getInstallations() {
			return new AntInstallation[] {};
		}

		@Override
		public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
			antWorkspaceFolder = json.getString("antWorkspaceFolder");
			antWorkspaceFolder = appendSlashIfNecessary(antWorkspaceFolder);
			save();
			return true;
		}

		public String getAntWorkspaceFolder() {
			if (antWorkspaceFolder == null) {
				LOGGER.log(Level.INFO, "Ant in Workspace is not configured. Return default");
				antWorkspaceFolder = "DailyBuild/ant-1.7.1/";
			}
			return antWorkspaceFolder;
		}

	}

	static String appendSlashIfNecessary(String pPath) {
		if (pPath.endsWith("/") == false) {
			pPath += "/";
		}
		return pPath;
	}
}
