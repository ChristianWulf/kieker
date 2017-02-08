/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.analysisteetime.trace.reconstruction;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;

import kieker.analysisteetime.model.analysismodel.deployment.DeployedComponent;
import kieker.analysisteetime.model.analysismodel.deployment.DeployedOperation;
import kieker.analysisteetime.model.analysismodel.deployment.DeploymentContext;
import kieker.analysisteetime.model.analysismodel.deployment.DeploymentModel;
import kieker.analysisteetime.model.analysismodel.trace.OperationCall;
import kieker.analysisteetime.model.analysismodel.trace.Trace;
import kieker.analysisteetime.model.analysismodel.trace.TraceFactory;
import kieker.common.record.flow.trace.TraceMetadata;
import kieker.common.record.flow.trace.operation.AfterOperationEvent;
import kieker.common.record.flow.trace.operation.AfterOperationFailedEvent;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;

/**
 *
 * @author S�ren Henning
 *
 * @since 1.13
 */
public class TraceReconstructionBuffer {

	private final TraceFactory factory = TraceFactory.eINSTANCE;
	private final DeploymentModel deploymentModel;
	private final TraceMetadata traceMetadata;
	private final Instant traceStart;

	private final Deque<BeforeOperationEvent> stack = new LinkedList<>();
	private OperationCall root;
	private OperationCall current;

	public TraceReconstructionBuffer(final DeploymentModel deploymentModel, final TraceMetadata traceMetadata) {
		this.deploymentModel = deploymentModel;
		this.traceMetadata = traceMetadata;
		// TODO Temp Get from TraceMetadata
		// final long epochMilli = 0;
		// this.traceStart = Instant.ofEpochMilli(epochMilli);
		this.traceStart = Instant.now();
	}

	public void handleBeforeOperationEventRecord(final BeforeOperationEvent record) {
		this.stack.push(record);

		final OperationCall newCall = this.factory.createOperationCall();

		// TODO Calculate Start, if necessary
		final long nanosOffset = this.traceMetadata.getLoggingTimestamp() - record.getTimestamp();
		newCall.setStart(this.traceStart.plusNanos(nanosOffset));

		final DeploymentContext context = this.deploymentModel.getDeploymentContexts().get(this.traceMetadata.getHostname());
		final DeployedComponent component = context.getComponents().get(record.getClassSignature());
		final DeployedOperation operation = component.getContainedOperations().get(record.getOperationSignature());
		newCall.setOperation(operation);

		newCall.setOrderIndex(record.getOrderIndex());
		newCall.setStackDepth(this.stack.size() - 1);

		if (this.root == null) {
			this.root = newCall;
		} else {
			this.current.getChildren().add(newCall);
		}
		this.current = newCall;
	}

	public void handleAfterOperationEventRecord(final AfterOperationEvent record) {
		final BeforeOperationEvent beforeEvent = this.stack.pop();

		// TODO This does not work for other time units
		final long durationInNanos = record.getTimestamp() - beforeEvent.getTimestamp();
		this.current.setDuration(Duration.ofNanos(durationInNanos));

		if (record instanceof AfterOperationFailedEvent) {
			final String failedCause = ((AfterOperationFailedEvent) record).getCause();
			this.current.setFailed(true);
			this.current.setFailedCause(failedCause);
		}

		this.current = this.current.getParent();

		// TODO
		/*
		 * if (TraceReconstructor.this.activateAdditionalLogChecks) {
		 * if (!beforeEvent.getOperationSignature().equals(record.getOperationSignature())) {
		 * TraceReconstructor.this.faultyTraceBuffers.add(this);
		 * TraceReconstructor.this.traceBuffers.remove(this.traceID);
		 * }
		 * }
		 */
	}

	public Trace reconstructTrace() {
		final Trace trace = this.factory.createTrace();
		trace.setRootOperationCall(this.root);
		trace.setTraceID(this.traceMetadata.getTraceId());
		return trace;
	}

	public boolean isTraceComplete() {
		return this.stack.isEmpty();
	}

}
