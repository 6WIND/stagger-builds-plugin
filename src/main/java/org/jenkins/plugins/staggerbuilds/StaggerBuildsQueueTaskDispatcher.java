/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2015, 6WIND S.A. All rights reserved.                 *
 *                                                                     *
 * This file is part of the Jenkins Stagger Builds Plugin and is       *
 * published under the MIT license.                                    *
 *                                                                     *
 * See the "LICENSE.txt" file for more information.                    *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.jenkins.plugins.staggerbuilds;

import hudson.Extension;
import hudson.model.Executor;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.model.queue.CauseOfBlockage;
import jenkins.model.Jenkins;

@Extension
public class StaggerBuildsQueueTaskDispatcher extends QueueTaskDispatcher {

	@Override
	public CauseOfBlockage canTake(Node node, BuildableItem item) {

		StaggerBuildsNodeProperty p = node.getNodeProperties().get(
				StaggerBuildsNodeProperty.class);

		// do not stagger if disabled for this node
		if (p == null)
			return null;

		// If items are "pending", any executor may accept them anytime.
		// Wait for "pending" list to be empty to be able to check the
		// start time of builds.
		if (Jenkins.getInstance().getQueue().getPendingItems().size() > 0)
			return new BecausePendingBuilds();

		// Evaluate the time since the last build was started on this node.
		long staggerTime = Long.MAX_VALUE;
		for (Executor e : node.toComputer().getExecutors())
			if (e.isBusy())
				staggerTime = Math.min(staggerTime, e.getElapsedTime());

		if (staggerTime < p.getStaggerSeconds() * 1000)
			return new BecauseStaggerEnabled(node, p.getStaggerSeconds());

		return null;
	}

	public static class BecauseStaggerEnabled extends CauseOfBlockage {

		private Node node;
		private long staggerDuration;

		public BecauseStaggerEnabled(Node node, long staggerDuration) {
			this.node = node;
			this.staggerDuration = staggerDuration;
		}

		@Override
		public String getShortDescription() {
			return String.format("Build staggering is enabled for node %s. "
					+ "Minimum %d seconds between builds.", node,
					staggerDuration);
		}
	}

	public static class BecausePendingBuilds extends CauseOfBlockage {
		@Override
		public String getShortDescription() {
			return "Builds are pending in the queue";
		}
	}
}
