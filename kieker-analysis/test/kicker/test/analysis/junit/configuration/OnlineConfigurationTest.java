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

package kicker.test.analysis.junit.configuration;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import kicker.analysis.AnalysisController;
import kicker.analysis.IAnalysisController;
import kicker.analysis.IProjectContext;
import kicker.analysis.configuration.GlobalConfigurationRegistry;
import kicker.analysis.exception.AnalysisConfigurationException;
import kicker.analysis.exception.PluginNotFoundException;
import kicker.analysis.plugin.AbstractUpdateableFilterPlugin;
import kicker.analysis.plugin.annotation.Plugin;
import kicker.analysis.plugin.annotation.Property;
import kicker.analysis.plugin.reader.list.ListReader;
import kicker.common.configuration.Configuration;
import kicker.test.common.junit.AbstractKiekerTest;

/**
 * This is a test making sure that it is possible to update the configuration of specific plugins during runtime.
 * 
 * @author Markus Fischer, Nils Christian Ehmke
 * 
 * @since 1.10
 */
public class OnlineConfigurationTest extends AbstractKiekerTest {

	@Rule
	public ExpectedException exception = ExpectedException.none(); // NOPMD NOCS

	public OnlineConfigurationTest() {
		// No code necessary
	}

	@Test
	public void testUpdatingNonExistingPlugin() throws PluginNotFoundException { // NOPMD (missing assert - we expect an exception)
		this.exception.expect(PluginNotFoundException.class);
		GlobalConfigurationRegistry.getInstance().updateConfiguration(42, new Configuration(), true);
	}

	@Test
	@SuppressWarnings("unused")
	public void testUpdatingPlugin() throws PluginNotFoundException, IllegalStateException, AnalysisConfigurationException {
		final IAnalysisController analysisController = new AnalysisController();

		new ListReader<String>(new Configuration(), analysisController);
		final UpdateableFilter filter = new UpdateableFilter(new Configuration(), analysisController);

		// Make sure the default configuration is loaded
		Assert.assertEquals("original-configuration", filter.getConfiguredContent());

		analysisController.run();

		// Now update the configuration
		final int pluginID = GlobalConfigurationRegistry.getInstance().registerUpdateableFilterPlugin(filter);

		final Configuration newConfiguration = new Configuration();
		newConfiguration.setProperty(UpdateableFilter.PROPERTY_NAME, "new-configuration");
		GlobalConfigurationRegistry.getInstance().updateConfiguration(pluginID, newConfiguration, true);

		Assert.assertEquals("new-configuration", filter.getConfiguredContent());
	}

	/**
	 * A simple plugin that can be updated.
	 * 
	 * @author Markus Fischer, Nils Christian Ehmke
	 * 
	 * @since 1.10
	 */
	@Plugin(programmaticOnly = true,
			configuration = @Property(name = UpdateableFilter.PROPERTY_NAME, defaultValue = UpdateableFilter.PROPERTY_VALUE, updateable = true))
	public static class UpdateableFilter extends AbstractUpdateableFilterPlugin {

		public static final String PROPERTY_NAME = "property";
		public static final String PROPERTY_VALUE = "original-configuration";

		private String configuredContent;

		public UpdateableFilter(final Configuration configuration, final IProjectContext projectContext) {
			super(configuration, projectContext);

			this.configuredContent = configuration.getStringProperty(PROPERTY_NAME);
		}

		public String getConfiguredContent() {

			return this.configuredContent;
		}

		@Override
		public void setCurrentConfiguration(final Configuration configuration, final boolean update) {
			if (!update || this.isPropertyUpdateable(PROPERTY_NAME)) {
				this.configuredContent = configuration.getStringProperty(PROPERTY_NAME);
			}
		}

		@Override
		public Configuration getCurrentConfiguration() {
			throw new UnsupportedOperationException();
		}
	}

}
