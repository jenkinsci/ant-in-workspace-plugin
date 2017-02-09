package org.jenkinsci.plugins.ant_in_workspace;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * 
 * This is the Class that contains the per-Job configuration for the AntInWorkspace Plugin.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntInWorkspaceJobPropertyInfo implements Describable<AntInWorkspaceJobPropertyInfo> {

	String antInWorkspace;

	@DataBoundConstructor
	public AntInWorkspaceJobPropertyInfo(@CheckForNull String antInWorkspace) {
		this.antInWorkspace = antInWorkspace;
	}

	public String getAntInWorkspace() {
		return antInWorkspace;
	}

	public void setAntInWorkspace(String antInWorkspace) {
		this.antInWorkspace = antInWorkspace;
	}

	@Override
	public Descriptor<AntInWorkspaceJobPropertyInfo> getDescriptor() {
		return Jenkins.getActiveInstance().getDescriptorByType(DescriptorImpl.class);
	}

	@Extension
	public static class DescriptorImpl extends Descriptor<AntInWorkspaceJobPropertyInfo> {
		@Override
		public String getDisplayName() {
			return "AntInWorkspaceJobPropertyInfo";
		}
	}

}
