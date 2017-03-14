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

package kieker.analysisteetime;

import java.util.stream.Collectors;

import kieker.analysisteetime.model.analysismodel.type.OperationType;

/**
 * @author S�ren Henning
 *
 * @since 1.13
 */
public class JavaFullOperationNameBuilder implements OperationNameBuilder {

	@Override
	public String build(final OperationType operationType) {
		final StringBuilder builder = new StringBuilder();
		if (operationType.getModifiers().size() > 0) {
			builder.append(operationType.getModifiers().stream().collect(Collectors.joining(" ", "", " ")));
		}
		builder.append(operationType.getReturnType());
		builder.append(' ');
		builder.append(operationType.getName());
		builder.append(operationType.getParameterTypes().stream().collect(Collectors.joining(" ,", "(", ")")));
		return builder.toString();
	}

}
