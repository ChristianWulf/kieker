/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.traceAnalysis.plugins.visualization.callTree;

import kieker.tools.traceAnalysis.systemModel.SynchronousCallMessage;
import kieker.tools.traceAnalysis.systemModel.repository.AllocationComponentOperationPairFactory;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.tools.traceAnalysis.systemModel.util.AllocationComponentOperationPair;

/**
 * 
 * @author Andre van Hoorn
 */
public class TraceCallTreeNode extends AbstractCallTreeNode<AllocationComponentOperationPair> {

	private final AllocationComponentOperationPairFactory pairFactory;

	public TraceCallTreeNode(final int id, final SystemModelRepository systemEntityFactory, final AllocationComponentOperationPairFactory pairFactory,
			final AllocationComponentOperationPair entity, final boolean rootNode) {
		super(id, systemEntityFactory, entity, rootNode);
		this.pairFactory = pairFactory;
	}

	@Override
	public AbstractCallTreeNode<AllocationComponentOperationPair> newCall(final SynchronousCallMessage callMsg) {
		final AllocationComponentOperationPair destPair = this.pairFactory.getPairInstanceByPair(callMsg.getReceivingExecution().getAllocationComponent(), callMsg
				.getReceivingExecution().getOperation());
		final TraceCallTreeNode destNode = new TraceCallTreeNode(destPair.getId(), getSystemEntityFactory(), this.pairFactory, destPair, false);
		final WeightedDirectedCallTreeEdge<AllocationComponentOperationPair> e = new WeightedDirectedCallTreeEdge<AllocationComponentOperationPair>(this, destNode);
		super.appendChildEdge(e);
		return destNode;
	}
}
