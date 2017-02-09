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

package kieker.analysisteetime.dependencygraphs;

import kieker.analysisteetime.model.analysismodel.assembly.AssemblyComponent;
import kieker.analysisteetime.model.analysismodel.assembly.AssemblyOperation;
import kieker.analysisteetime.model.analysismodel.deployment.DeployedOperation;
import kieker.analysisteetime.util.graph.Graph;
import kieker.analysisteetime.util.graph.Vertex;

/**
 * Dependency graph builder for <strong>operation</strong> dependency graphs
 * at the <strong>assembly level</strong>.
 *
 * @author S�ren Henning
 *
 * @since 1.13
 */
public class AssemblyLevelOperationDependencyGraphBuilder extends AbstractDependencyGraphBuilder {

	public AssemblyLevelOperationDependencyGraphBuilder() {
		super();
	}

	@Override
	protected Vertex addVertex(final DeployedOperation deployedOperation) {
		final AssemblyOperation operation = deployedOperation.getAssemblyOperation();
		final AssemblyComponent component = operation.getAssemblyComponent();

		final int componentId = this.identifierRegistry.getIdentifier(component);
		final Vertex componentVertex = this.graph.addVertexIfAbsent(componentId);

		final Graph componentSubgraph = componentVertex.addChildGraphIfAbsent();
		final int operationId = this.identifierRegistry.getIdentifier(operation);
		final Vertex operationVertex = componentSubgraph.addVertexIfAbsent(operationId);

		return operationVertex;
	}

}
