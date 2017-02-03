package com.wincornixdorf.pce.jenkins.dailybuildant;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.Ant;
import hudson.tools.ToolInstallation;

/**
 * 
 * PC/E Jenkins Plugin to provide the ANT from the current DevKit.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntPCE extends Ant {

//	final Ant mAnt;
	final String antName;

	private static final Logger LOGGER = Logger.getLogger(AntPCE.class.getName());

	@DataBoundConstructor
	public AntPCE(String targets, String antName, String antOpts, String buildFile, String properties) {
		super(targets, antName, antOpts, buildFile, properties);
		System.out.println("constructor");
		this.antName = antName;
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 * Gets the Ant to invoke,
	 * or null to invoke the default one.
	 */
	public AntInstallation getAnt() {
		LOGGER.log(Level.FINE, "get ant");
		LOGGER.log(Level.FINE, "antname: " + antName);
		AntInstallation ant = super.getAnt();
		if (ant == null) {
			LOGGER.log(Level.FINE, "use custom workspace ant");
			ant = new AntInstallation("PCE_DailyBuild", "DailyBuild/ant-1.7.1/", null);
		} else {
			LOGGER.log(Level.FINE, "use ant from super: " + ant.getHome());
		}
		return ant;
	}

	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {
		LOGGER.log(Level.FINE, "perform: " + antName);
		return super.perform(build, launcher, listener);
	}

	@Extension
	@Symbol("antpce")
	public static class DescriptorImpl extends Ant.DescriptorImpl {

		public DescriptorImpl() {
			setInstallations(new AntInstallation("PCE_DailyBuild", "DailyBuild/ant-1.7.1/", null));
		}
		/**
		 * Obtains the {@link AntInstallation.DescriptorImpl} instance.
		 */
		public AntInstallation.DescriptorImpl getToolDescriptor() {
			return ToolInstallation.all().get(AntInstallation.DescriptorImpl.class);
		}

		public String getDisplayName() {
			return "Invoke PCE ANT";
		}

		public AntInstallation[] getInstallations() {
			LOGGER.log(Level.FINE, "get installations");
			AntInstallation[] devkit = new AntInstallation[1];
			devkit[0] = new AntInstallation("PCE_DailyBuild", "DailyBuild/ant-1.7.1/", null);
			return devkit;
		}

		public void setInstallations(AntInstallation... antInstallations) {
			super.setInstallations(antInstallations);
		}
	}
}
