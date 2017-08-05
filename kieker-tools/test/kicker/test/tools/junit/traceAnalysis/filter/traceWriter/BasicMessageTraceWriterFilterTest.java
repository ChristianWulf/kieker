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

package kicker.test.tools.junit.traceAnalysis.filter.traceWriter;

import java.io.IOException;
import java.util.List;

import kicker.common.configuration.Configuration;
import kicker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kicker.tools.traceAnalysis.filter.AbstractTraceProcessingFilter;
import kicker.tools.traceAnalysis.filter.traceWriter.MessageTraceWriterFilter;
import kicker.tools.traceAnalysis.systemModel.MessageTrace;
import kicker.analysis.AnalysisController;

/**
 * 
 * @author Andre van Hoorn
 * 
 * @since 1.5
 */
public class BasicMessageTraceWriterFilterTest extends AbstractTraceWriterFilterTest { // NOPMD (TestClassWithoutTestCases)

	/**
	 * Default constructor.
	 */
	public BasicMessageTraceWriterFilterTest() {
		// empty default constructor
	}

	@Override
	protected AbstractTraceProcessingFilter provideWriterFilter(final String filename, final AnalysisController analysisController) throws IOException {
		final Configuration filterConfiguration = new Configuration();
		filterConfiguration.setProperty(MessageTraceWriterFilter.CONFIG_PROPERTY_NAME_OUTPUT_FN, filename);

		return new MessageTraceWriterFilter(filterConfiguration, analysisController);
	}

	@Override
	protected String provideFilterInputName() {
		return AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES;
	}

	@Override
	protected String provideExpectedFileContent(final List<Object> loggedEvents) {
		final StringBuilder strB = new StringBuilder();
		for (final Object o : loggedEvents) {
			if (o instanceof MessageTrace) {
				strB.append(o.toString()).append(AbstractTraceWriterFilterTest.SYSTEM_NEWLINE_STRING);
			}
		}
		return strB.toString();
	}
}
