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
 * PC/E Jenkins Plugin to provide the ANT from the current Workspace.
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
	 * Return the ANT from super or if not defined the ANT from the workspace.
	 */
	public AntInstallation getAnt() {
		AntInstallation retVal = super.getAnt();
		if (retVal != null) {
			LOGGER.log(Level.INFO, "use ANT from super: " + retVal.getHome());
		} else if (mWorkspace == null) {
			LOGGER.log(Level.INFO, "Workspace is not set. Cannot use ANT from workspace.");
		} else {
			LOGGER.log(Level.INFO, "use ANT from workspace");
			retVal = new AntInstallation("Workspace ANT", mWorkspace + getDescriptor().getAntWorkspaceFolder(), null);
		}
		return retVal;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		LOGGER.log(Level.FINE, "perform: " + build.getWorkspace().getRemote());

		// Important to store this into member variable first!
		mWorkspace = build.getWorkspace().getRemote();

		final AntInstallation ant = getAnt();
		if (ant != null && launcher.isUnix()) {
			makeAntExecutable(ant);
		}
		return super.perform(build, launcher, listener);
	}

	void makeAntExecutable(final AntInstallation pAnt) throws AbortException {
		final Path pathToAntBinary = new File(pAnt.getHome() + "/bin/ant").toPath();
		try {
			LOGGER.log(Level.FINE, "Change File Permissions: " + pathToAntBinary.toString());
			final Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			Files.setPosixFilePermissions(pathToAntBinary, perms);
		} catch (IOException e) {
			throw new AbortException("Unable to make ANT executable " + pathToAntBinary.toString());
		}
	}

	@Extension
	@Symbol("antpce")
	public static class DescriptorImpl extends Ant.DescriptorImpl {

		private String antWorkspaceFolder;

		public String getDisplayName() {
			return "Invoke Workspace ANT";
		}

		/**
		 * No Choices should be given. We choose automatically the ANT from the Workspace.
		 */
		public AntInstallation[] getInstallations() {
			return new AntInstallation[] {};
		}

		@Override
		public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
			antWorkspaceFolder = json.getString("antWorkspaceFolder");
			save();
			return true;
		}

		public String getAntWorkspaceFolder() {
			if (antWorkspaceFolder == null) {
				LOGGER.log(Level.FINE, "ANT in Workspace is not configured. Return default");
				antWorkspaceFolder = "DailyBuild/ant-1.7.1/";
			}
			return antWorkspaceFolder;
		}

	}
}
