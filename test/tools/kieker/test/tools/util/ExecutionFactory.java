/***************************************************************************
 * Copyright 2012 by
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

package kieker.test.tools.util;

import org.junit.Assert;

import kieker.common.util.Signature;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.ExecutionContainer;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

/**
 * 
 * @author Andre van Hoorn
 */
public class ExecutionFactory {

	private static final String DEFAULT_STRING = "N/A";
	private final SystemModelRepository systemEntityFactory;

	public ExecutionFactory(final SystemModelRepository systemEntityFactory) {
		this.systemEntityFactory = systemEntityFactory;
	}

	public Execution genExecution(
			final String componentTypeName, final String componentInstanceName, final String executionContainerName,
			final String operationName, final String[] modifierList, final String returnType, final String[] paramTypeList,
			final long traceId,
			final String sessionId,
			final long tin, final long tout, final int eoi, final int ess) {
		if ((componentTypeName == null) || (componentInstanceName == null) || (operationName == null)) {
			throw new NullPointerException("None of the String args must be null.");
		}

		/* Register component type (if it hasn't been registered before) */
		ComponentType componentTypeA = this.systemEntityFactory.getTypeRepositoryFactory().lookupComponentTypeByNamedIdentifier(componentTypeName);
		if (componentTypeA == null) {
			componentTypeA = this.systemEntityFactory.getTypeRepositoryFactory().createAndRegisterComponentType(componentTypeName, componentTypeName);
		}
		Assert.assertEquals("Unexpected component type name", componentTypeName, componentTypeA.getTypeName());

		/* Register operation (if it hasn't been registered before) */
		Operation operationAa = this.systemEntityFactory.getOperationFactory().lookupOperationByNamedIdentifier(operationName);
		if (operationAa == null) {
			operationAa = this.systemEntityFactory.getOperationFactory().createAndRegisterOperation(
					operationName, componentTypeA,
					new Signature(operationName, modifierList, returnType, paramTypeList));
		}
		if (componentTypeA.getOperations().contains(operationAa)) {
			componentTypeA.addOperation(operationAa);
		}

		/* Register assembly component (if it hasn't been registered before) */
		AssemblyComponent assemblyComponentA = this.systemEntityFactory.getAssemblyFactory().lookupAssemblyComponentInstanceByNamedIdentifier(componentInstanceName);
		if (assemblyComponentA == null) {
			assemblyComponentA = this.systemEntityFactory.getAssemblyFactory().createAndRegisterAssemblyComponentInstance(componentInstanceName, componentTypeA);
		}

		/* Register execution container (if it hasn't been registered before) */
		ExecutionContainer containerC = this.systemEntityFactory.getExecutionEnvironmentFactory().lookupExecutionContainerByNamedIdentifier(executionContainerName);
		if (containerC == null) {
			containerC = this.systemEntityFactory.getExecutionEnvironmentFactory().createAndRegisterExecutionContainer(executionContainerName,
					executionContainerName);
		}

		/* Register allocation component (if it hasn't been registered before) */
		final String allocationName = componentInstanceName + "::" + executionContainerName;
		AllocationComponent allocationComponentA =
				this.systemEntityFactory.getAllocationFactory().lookupAllocationComponentInstanceByNamedIdentifier(allocationName);
		if (allocationComponentA == null) {
			allocationComponentA = this.systemEntityFactory.getAllocationFactory().createAndRegisterAllocationComponentInstance(allocationName,
					assemblyComponentA, containerC);
		}

		final Execution ret = new Execution(operationAa, allocationComponentA, traceId, sessionId, eoi, ess, tin, tout, false);

		return ret;
	}

	/**
	 * Creates an Execution object initialized with the passed values.
	 * The remaining values of the Execution object are assigned default
	 * values.
	 * 
	 * @param componentTypeName
	 * @param componentInstanceName
	 * @param operationName
	 * @param traceId
	 * @param tin
	 * @param tout
	 * @param eoi
	 * @param ess
	 * @throws NullPointerException
	 *             if one of the String args has the value null.
	 * @return the created Execution object
	 */
	public Execution genExecution(final String componentTypeName, final String componentInstanceName, final String operationName, final long traceId,
			final String sessionId,
			final long tin, final long tout, final int eoi, final int ess) {
		return this.genExecution(componentTypeName, componentInstanceName, ExecutionFactory.DEFAULT_STRING, // hostname
				operationName, new String[] { ExecutionFactory.DEFAULT_STRING }, ExecutionFactory.DEFAULT_STRING, new String[] { ExecutionFactory.DEFAULT_STRING },
				traceId, sessionId, tin, tout,
				eoi, ess);
	}

	public Execution genExecution(final String componentTypeName, final String componentInstanceName, final String hostName, final String operationName,
			final long traceId,
			final String sessionId,
			final long tin, final long tout, final int eoi, final int ess) {
		return this.genExecution(componentTypeName, componentInstanceName, hostName,
				operationName, new String[] { ExecutionFactory.DEFAULT_STRING }, ExecutionFactory.DEFAULT_STRING, new String[] { ExecutionFactory.DEFAULT_STRING },
				traceId, sessionId, tin, tout,
				eoi, ess);
	}

	/**
	 * Creates an Execution object initialized with the passed values.
	 * The remaining values of the Execution object are assigned default
	 * values.
	 * 
	 * @param traceId
	 * @param tin
	 * @param tout
	 * @param eoi
	 * @param ess
	 * @return the created Execution object
	 */
	public Execution genExecution(final long traceId, final String sessionId, final long tin, final long tout, final int eoi, final int ess) {
		return this.genExecution(ExecutionFactory.DEFAULT_STRING, // component type
				ExecutionFactory.DEFAULT_STRING, // component instance
				ExecutionFactory.DEFAULT_STRING, // operation name
				traceId, sessionId, tin, tout, eoi, ess);
	}
}