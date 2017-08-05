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

package kicker.test.tools.junit.writeRead.filesystem;

import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import kicker.common.configuration.Configuration;
import kicker.common.record.IMonitoringRecord;
import kicker.monitoring.core.configuration.ConfigurationFactory;
import kicker.monitoring.core.controller.MonitoringController;
import kicker.monitoring.core.controller.WriterController;
import kicker.monitoring.writer.filesystem.AsciiFileWriter;
import kicker.test.tools.junit.writeRead.TestAnalysis;
import kicker.test.tools.junit.writeRead.TestDataRepository;
import kicker.test.tools.junit.writeRead.TestProbe;
import kicker.analysis.plugin.reader.filesystem.AsciiLogReader;

/**
 * @author Christian Wulf
 *
 * @since 1.13
 */
public class AsciiWriterReaderTest {

	private static final TestDataRepository TEST_DATA_REPOSITORY = new TestDataRepository();
	private static final int TIMEOUT_IN_MS = 0;

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder(); // NOCS (@Rule must be public)

	public AsciiWriterReaderTest() {
		super();
	}

	@Test
	public void testUncompressedAsciiCommunication() throws Exception {
		// 1. define records to be triggered by the test probe
		final List<IMonitoringRecord> records = TEST_DATA_REPOSITORY.newTestRecords();

		final List<IMonitoringRecord> analyzedRecords = this.testAsciiCommunication(records, false);

		// 8. compare actual and expected records
		Assert.assertThat(analyzedRecords, CoreMatchers.is(CoreMatchers.equalTo(records)));
	}

	@Test
	public void testCompressedAsciiCommunication() throws Exception {
		// 1. define records to be triggered by the test probe
		final List<IMonitoringRecord> records = TEST_DATA_REPOSITORY.newTestRecords();

		final List<IMonitoringRecord> analyzedRecords = this.testAsciiCommunication(records, true);

		// 8. compare actual and expected records
		Assert.assertThat(analyzedRecords, CoreMatchers.is(CoreMatchers.equalTo(records)));
	}

	@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
	private List<IMonitoringRecord> testAsciiCommunication(final List<IMonitoringRecord> records, final boolean shouldDecompress) throws Exception {
		// 2. define monitoring config
		final Configuration config = ConfigurationFactory.createDefaultConfiguration();
		config.setProperty(ConfigurationFactory.WRITER_CLASSNAME, AsciiFileWriter.class.getName());
		config.setProperty(WriterController.RECORD_QUEUE_SIZE, "128");
		config.setProperty(WriterController.RECORD_QUEUE_INSERT_BEHAVIOR, "1");
		config.setProperty(AsciiFileWriter.CONFIG_PATH, this.tmpFolder.getRoot().getCanonicalPath());
		config.setProperty(AsciiFileWriter.CONFIG_SHOULD_COMPRESS, Boolean.toString(shouldDecompress));
		final MonitoringController monitoringController = MonitoringController.createInstance(config);

		// 3. define analysis config
		final String[] monitoringLogDirs = TEST_DATA_REPOSITORY.getAbsoluteMonitoringLogDirNames(this.tmpFolder.getRoot());

		final Configuration readerConfiguration = new Configuration();
		readerConfiguration.setProperty(AsciiLogReader.CONFIG_PROPERTY_NAME_INPUTDIRS, Configuration.toProperty(monitoringLogDirs));
		readerConfiguration.setProperty(AsciiLogReader.CONFIG_PROPERTY_NAME_IGNORE_UNKNOWN_RECORD_TYPES, "false");
		readerConfiguration.setProperty(AsciiLogReader.CONFIG_SHOULD_DECOMPRESS, Boolean.toString(shouldDecompress));
		final TestAnalysis analysis = new TestAnalysis(readerConfiguration, AsciiLogReader.class);

		// 4. trigger records
		final TestProbe testProbe = new TestProbe(monitoringController);
		Assert.assertTrue(monitoringController.isMonitoringEnabled());
		testProbe.triggerRecords(records);
		Assert.assertTrue(monitoringController.isMonitoringEnabled());

		// 5. terminate monitoring
		monitoringController.terminateMonitoring();

		// 6. wait for termination
		monitoringController.waitForTermination(TIMEOUT_IN_MS);
		analysis.startAndWaitForTermination();

		// 7. read actual records
		return analysis.getList();
	}
}
