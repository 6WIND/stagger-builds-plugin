package org.jenkins.plugins.staggerbuilds;

import hudson.Extension;
import hudson.model.Node;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class StaggerBuildsNodeProperty extends NodeProperty<Node> {

	private final int staggerSeconds;

	@DataBoundConstructor
	public StaggerBuildsNodeProperty(int staggerSeconds) {
		this.staggerSeconds = staggerSeconds;
	}

	public int getStaggerSeconds() {
		return staggerSeconds;
	}

	@Extension
	public static class DescriptorImpl extends NodePropertyDescriptor {

		@Override
		public String getDisplayName() {
			return "Stagger Builds";
		}

		public FormValidation doCheckStaggerSeconds(@QueryParameter String value) {
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return FormValidation.error(e.toString());
			}
			return FormValidation.ok();
		}
	}
}
