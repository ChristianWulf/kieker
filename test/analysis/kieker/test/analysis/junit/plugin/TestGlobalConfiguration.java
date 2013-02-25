/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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
package kieker.test.analysis.junit.plugin;

import org.junit.Assert;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;

/**
 * This test makes sure that the "global configuration" of an analysis instance works.
 * 
 * @author Nils Christian Ehmke
 * 
 * @since 1.7
 */
public class TestGlobalConfiguration {

	public TestGlobalConfiguration() {
		// No code necessary
	}

	@Test
	public void testGlobalConfiguration() {
		final Configuration globalConfiguration = new Configuration();
		globalConfiguration.setProperty("key", "value");
		final IAnalysisController analysisController = new AnalysisController(globalConfiguration);

		final PropertyFilter filter = new PropertyFilter(new Configuration(), analysisController);

		Assert.assertEquals("value", filter.getProperty("key"));
	}

	@Test
	public void testNoGlobalConfiguration() {
		final IAnalysisController analysisController = new AnalysisController();

		final PropertyFilter filter = new PropertyFilter(new Configuration(), analysisController);

		Assert.assertEquals("", filter.getProperty("key"));
	}

	@Test
	public void testDefaultGlobalConfiguration() {
		final IAnalysisController analysisController = new AnalysisController();

		final PropertyFilter filter = new PropertyFilter(new Configuration(), analysisController);

		Assert.assertEquals("NANOSECONDS", filter.getProperty(IProjectContext.CONFIG_PROPERTY_NAME_RECORDS_TIME_UNIT));
	}

	@Test
	public void testOverwrittenDefaultGlobalConfiguration() {
		final Configuration globalConfiguration = new Configuration();
		globalConfiguration.setProperty(IProjectContext.CONFIG_PROPERTY_NAME_RECORDS_TIME_UNIT, "seconds");
		final IAnalysisController analysisController = new AnalysisController(globalConfiguration);

		final PropertyFilter filter = new PropertyFilter(new Configuration(), analysisController);

		Assert.assertEquals("seconds", filter.getProperty(IProjectContext.CONFIG_PROPERTY_NAME_RECORDS_TIME_UNIT));
	}

	private static class PropertyFilter extends AbstractFilterPlugin {

		public PropertyFilter(final Configuration configuration, final IProjectContext projectContext) {
			super(configuration, projectContext);
		}

		@Override
		public Configuration getCurrentConfiguration() {
			return null;
		}

		public String getProperty(final String key) {
			return this.projectContext.getProperty(key);
		}
	}
}
