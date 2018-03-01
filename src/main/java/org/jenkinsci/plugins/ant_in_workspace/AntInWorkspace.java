package org.jenkinsci.plugins.ant_in_workspace;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Ant;
import net.sf.json.JSONObject;

/**
 * 
 * Jenkins plugin to provide the Ant from the current Workspace. <br>
 * This is to support projects, that must use a specific version of Ant that is
 * (checked-in with the source code and is) available in the current workspace.
 * With this plugin you can use the checked-in Ant for building the current
 * project. <br>
 * The plugin allows to configure a global path for the AntInWorkspace or a
 * per-Job configuration.
 *
 * @author stephan.watermeyer, Diebold Nixdorf
 */
public class AntInWorkspace extends Ant {

    private static final Logger LOGGER = Logger.getLogger(AntInWorkspace.class.getName());

    /** Used to store the Workspace Directory on "perform" */
    private String mPathToAnt;

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
        } else if (mPathToAnt == null) {
            LOGGER.log(Level.INFO, "Path to Ant is not set. Cannot use Ant from workspace.");
        } else {
            LOGGER.log(Level.INFO, "use Ant from workspace: " + mPathToAnt);
            retVal = new AntInstallation("Ant In Workspace", mPathToAnt, null);
        }
        return retVal;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

        // Evaluate if the Job has a custom AntInWorkspace directory set.
        final String antInWorkspace;
        if (build.getEnvironment(listener).containsKey(AntInWorkspaceBuildWrapper.ENV_VAR_CUSTOM_ANT_IN_WORKSPACE)) {
            antInWorkspace = build.getEnvironment(listener).get(AntInWorkspaceBuildWrapper.ENV_VAR_CUSTOM_ANT_IN_WORKSPACE);
        } else {
            antInWorkspace = getDescriptor().getAntWorkspaceFolder();
        }

        final FilePath toCheck = build.getWorkspace();
        if (toCheck == null) {
            return false;
        }
        final String workspace = appendSeparatorIfNecessary(toCheck.getRemote());

        // Important to store this into member variable
        mPathToAnt = workspace + antInWorkspace;

        final AntInstallation ant = getAnt();
        if (ant != null && launcher.isUnix()) {
            validateAndMakeAntExecutable(build.getWorkspace(), ant);
        }
        return super.perform(build, launcher, listener);
    }

    void validateAndMakeAntExecutable(@Nonnull final FilePath pWorkspace, @Nonnull final AntInstallation pAnt) throws AbortException {
        final hudson.FilePath pathToAntBinary;
        final String pathToAntInWorkspace = pAnt.getHome() + "/bin/ant";
        if (pWorkspace.isRemote()) {
            LOGGER.log(Level.FINE, "searching ANT on remote node");
            final VirtualChannel channel = pWorkspace.getChannel();
            pathToAntBinary = new hudson.FilePath(channel, pathToAntInWorkspace);
        } else {
            LOGGER.log(Level.FINE, "searching ANT on master");
            pathToAntBinary = new hudson.FilePath(new File(pathToAntInWorkspace));
        }

        try {
            if (!pathToAntBinary.exists()) {
                throw new AbortException("Ant does not exist in Workspace: " + pathToAntBinary.getRemote());
            }
        
            LOGGER.log(Level.FINE, "Change file permissions: " + pathToAntBinary.getRemote());
            pathToAntBinary.chmod(0755);
        } catch(AbortException e) {
            // just throw
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "failed to use ant", e);
            throw new AbortException("Unable to make Ant executable (IO Error): " + pathToAntBinary.getRemote());
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "failed to use ant2", e);
            throw new AbortException("Unable to make Ant executable (Interrupt): " + pathToAntBinary.getRemote());
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
         * No Choices should be given. We choose automatically the Ant from the
         * Workspace.
         */
        public AntInstallation[] getInstallations() {
            return new AntInstallation[] {};
        }

        @Override
        public boolean configure(StaplerRequest staplerRequest, JSONObject json) throws FormException {
            antWorkspaceFolder = json.getString("antWorkspaceFolder");
            antWorkspaceFolder = appendSeparatorIfNecessary(antWorkspaceFolder);
            save();
            return true;
        }

        public String getAntWorkspaceFolder() {
            if (antWorkspaceFolder == null) {
                LOGGER.log(Level.INFO, "Ant in Workspace is not configured. Return default: ''");
                antWorkspaceFolder = "";
            }
            return antWorkspaceFolder;
        }

    }

    /**
     * Checks if the given argument has and ending file separator.
     * 
     * @param pPath
     *            the path to check
     * @return the modified path
     */
    static String appendSeparatorIfNecessary(String pPath) {
        String retVal = pPath;
        if (retVal != null && retVal.endsWith("/") == false) {
            retVal += "/";
        }
        return retVal;
    }
}
