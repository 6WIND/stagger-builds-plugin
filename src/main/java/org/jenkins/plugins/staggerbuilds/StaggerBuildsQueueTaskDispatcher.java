package org.jenkins.plugins.staggerbuilds;

import java.util.HashMap;
import java.util.Map;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.model.queue.CauseOfBlockage;

@Extension
public class StaggerBuildsQueueTaskDispatcher extends QueueTaskDispatcher {

	private final Map<Node, Long> lastBuildStartTimes = new HashMap<Node, Long>();

	@Override
	public CauseOfBlockage canTake(Node node, BuildableItem item) {

		StaggerBuildsNodeProperty p = node.getNodeProperties().get(
				StaggerBuildsNodeProperty.class);

		// do not stagger if disabled for this node
		if (p == null)
			return null;

		Long now = (long) (System.currentTimeMillis() / 1000.0);
		Long last = lastBuildStartTimes.get(node);
		if (last == null)
			last = (long) 0;

		if (!node.toComputer().isIdle() && now - last <= p.getStaggerSeconds())
			return new BecauseStaggerEnabled(node, now - last);

		lastBuildStartTimes.put(node, now);

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
					+ "Waiting %d seconds before starting this build.", node,
					staggerDuration);
		}
	}
}
