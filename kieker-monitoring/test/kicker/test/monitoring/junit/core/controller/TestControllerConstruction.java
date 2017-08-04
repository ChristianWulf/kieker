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

package kicker.test.monitoring.junit.core.controller;

import org.junit.Assert;
import org.junit.Test;

import kicker.common.configuration.Configuration;
import kicker.monitoring.core.configuration.ConfigurationFactory;
import kicker.monitoring.core.controller.IMonitoringController;
import kicker.monitoring.core.controller.MonitoringController;
import kicker.test.common.junit.AbstractKiekerTest;
import kicker.test.monitoring.util.DefaultConfigurationFactory;

/**
 * @author Andre van Hoorn, Jan Waller
 * 
 * @since 1.3
 */
public class TestControllerConstruction extends AbstractKiekerTest { // NOCS

	/**
	 * Test if the initialization of the monitoring controller from a given configuration works.
	 */
	@Test
	public void testConstructionFromConfig() {

		final Configuration configuration = DefaultConfigurationFactory.createDefaultConfigurationWithDummyWriter();
		{// Test with default values // NOCS
			final IMonitoringController kieker = MonitoringController.createInstance(configuration);
			Assert.assertEquals("monitoring should not be terminated", false, kieker.isMonitoringTerminated());
			Assert.assertEquals("monitoringEnabled values differ", configuration.getBooleanProperty(ConfigurationFactory.MONITORING_ENABLED),
					kieker.isMonitoringEnabled());
			kieker.terminateMonitoring();
		}
		{// NOCS
			configuration
					.setProperty(ConfigurationFactory.MONITORING_ENABLED,
							Boolean.toString(!configuration.getBooleanProperty(ConfigurationFactory.MONITORING_ENABLED)));
			final IMonitoringController kieker = MonitoringController.createInstance(configuration);
			Assert.assertEquals("monitoring should not be terminated", false, kieker.isMonitoringTerminated());
			Assert.assertEquals("monitoringEnabled values differ", configuration.getBooleanProperty(ConfigurationFactory.MONITORING_ENABLED),
					kieker.isMonitoringEnabled());
			kieker.terminateMonitoring();
		}
	}

	/**
	 * Make sure that {@link MonitoringController#getInstance()} always returns the same instance.
	 */
	@Test
	public void testSingletonGetterOnlyOneInstance() {
		Assert.assertSame("singleton getter returned different objects", MonitoringController.getInstance(), MonitoringController.getInstance());
		Assert.assertEquals("monitoring should not be terminated", false, MonitoringController.getInstance().isMonitoringTerminated());
	}
}
