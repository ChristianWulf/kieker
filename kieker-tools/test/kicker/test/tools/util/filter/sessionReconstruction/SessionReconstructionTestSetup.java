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

package kicker.test.tools.util.filter.sessionReconstruction;

import kicker.test.tools.util.GenericTestSetup;
import kicker.tools.traceAnalysis.systemModel.AbstractSession;
import kicker.analysis.AnalysisController;
import kicker.analysis.plugin.filter.forward.ListCollectionFilter;

/**
 * Specific test setup for tests of the session reconstruction filter (see
 * {@link kicker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter}).
 * 
 * @author Holger Knoche
 * @since 1.10
 * 
 * @param <T>
 *            The concrete type of session used in this setup
 */
public class SessionReconstructionTestSetup<T extends AbstractSession<?>> extends GenericTestSetup<T, ListCollectionFilter<T>> {

	/**
	 * Creates a new session reconstruction test setup with the given data.
	 * 
	 * @param analysisController
	 *            The analysis controller to use
	 * @param resultCollectionPlugin
	 *            The result collection plugin to use
	 */
	public SessionReconstructionTestSetup(final AnalysisController analysisController, final ListCollectionFilter<T> resultCollectionPlugin) {
		super(analysisController, resultCollectionPlugin);
	}

}