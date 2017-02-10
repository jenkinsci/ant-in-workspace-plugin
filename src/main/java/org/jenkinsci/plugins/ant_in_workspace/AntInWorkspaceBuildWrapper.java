package org.jenkinsci.plugins.ant_in_workspace;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

/**
 * 
 * This class is the Job Configuration. If the Job has this option enabled, the path for AntInWorkspace can be customized.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntInWorkspaceBuildWrapper extends BuildWrapper implements Serializable {

	/** Environment Variable that is set if there is a Job-specific AntInWorkspace defined */
	public static final String ENV_VAR_CUSTOM_ANT_IN_WORKSPACE = "CUSTOM_ANT_IN_WORKSPACE";

	private static final long serialVersionUID = -4674143417274553383L;

	private static final Logger LOGGER = Logger.getLogger(AntInWorkspaceBuildWrapper.class.getName());

	AntInWorkspaceJobPropertyInfo info;

	@DataBoundConstructor
	public AntInWorkspaceBuildWrapper(@Nonnull AntInWorkspaceJobPropertyInfo info) {
		this.info = info;
	}

	public AntInWorkspaceJobPropertyInfo getInfo() {
		return info;
	}

	/*
	 * This method is invoked only, if the Job has a "AntInWorkspace" configured. This means we have to put the path for
	 * the defined AntInWorkspace to the environment variables. If the Job hasnt configured a custom AntInWorkspace, this method is not invoked.
	 */
	@Override
	public Environment setUp(@Nonnull AbstractBuild build, final @Nonnull Launcher launcher,
			final @Nonnull BuildListener listener) throws IOException, InterruptedException {
		return new Environment() {

			@Override
			public void buildEnvVars(Map<String, String> env) {
				LOGGER.log(Level.FINE, "put CUSTOM_ANT_IN_WORKSPACE: " + getInfo().getAntInWorkspace());
				env.put(ENV_VAR_CUSTOM_ANT_IN_WORKSPACE, getInfo().getAntInWorkspace());
			}
		};
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Use Ant in Workspace";
		}

	}
}
