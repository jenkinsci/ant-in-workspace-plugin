package org.jenkinsci.plugins.ant_in_workspace;

import java.io.Serializable;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

/**
 * 
 * This class is the Job Configuration.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntInWorkspaceBuildWrapper extends BuildWrapper implements Serializable {

	private static final long serialVersionUID = -4674143417274553383L;

	AntInWorkspaceJobPropertyInfo info;

	@DataBoundConstructor
	public AntInWorkspaceBuildWrapper(@Nonnull AntInWorkspaceJobPropertyInfo info) {
		this.info = info;
	}

	public AntInWorkspaceJobPropertyInfo getInfo() {
		return info;
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
