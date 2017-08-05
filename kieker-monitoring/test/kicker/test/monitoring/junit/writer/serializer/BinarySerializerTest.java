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

package kicker.test.monitoring.junit.writer.serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import kicker.common.configuration.Configuration;
import kicker.common.record.controlflow.OperationExecutionRecord;
import kicker.common.util.dataformat.VariableLengthEncoding;
import kicker.monitoring.core.configuration.ConfigurationFactory;
import kicker.monitoring.core.controller.IMonitoringController;
import kicker.monitoring.core.controller.MonitoringController;
import kicker.monitoring.writer.collector.ChunkingCollector;
import kicker.monitoring.writer.serializer.BinarySerializer;
import kicker.test.monitoring.junit.writer.collector.TestRawDataStorage;
import kicker.test.monitoring.junit.writer.collector.TestRawDataWriter;

/**
 * Tests for the default binary serializer.
 *
 * @author Holger Knoche
 *
 * @since 1.13
 */
public class BinarySerializerTest {

	public BinarySerializerTest() {
		// Default Constructor
	}
	
	@Test
	public void testSingleRecord() throws IOException, InterruptedException {
		final String testId = "testSingleRecord";
		final int recordCount = 1;
		final int taskRunInterval = 10;
		final int deferredWriteDelay = 50;

		final IMonitoringController controller = this.createController(testId, taskRunInterval, deferredWriteDelay);

		for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
			final OperationExecutionRecord record = new OperationExecutionRecord("op()", "SESS-ID", 0, recordIndex, recordIndex, "host", recordIndex, 1);
			controller.newMonitoringRecord(record);
		}

		// Terminate monitoring and wait for termination
		controller.terminateMonitoring();
		controller.waitForTermination(2000);
		
		// Retrieve written data from the data storage
		final byte[] data = TestRawDataStorage.getInstance().getData(testId);

		// Check some basic numbers, since full decoding is not yet supported
		final ByteBuffer dataBuffer = ByteBuffer.wrap(data);

		// Read the string table offset (last four bytes), plus 8 bytes for the container header
		dataBuffer.position(data.length - 4);
		final int stringTableOffset = dataBuffer.getInt() + 8;

		// Read the number of strings in the string table
		dataBuffer.position(stringTableOffset);
		final int numberOfStrings = VariableLengthEncoding.decodeInt(dataBuffer);

		// Check the size of the string table
		Assert.assertEquals(9, numberOfStrings);
	}

	@Test
	public void testMultipleRecords() throws InterruptedException {
		final String testId = "testMultipleRecords";
		final int recordCount = 15;
		final int taskRunInterval = 10;
		final int deferredWriteDelay = 50;

		final IMonitoringController controller = this.createController(testId, taskRunInterval, deferredWriteDelay);

		for (int recordIndex = 0; recordIndex < recordCount; recordIndex++) {
			final OperationExecutionRecord record = new OperationExecutionRecord("operation()", "SESS-ID", 0, recordIndex, recordIndex, "hostname", recordIndex, 1);
			controller.newMonitoringRecord(record);
		}

		// Terminate monitoring
		controller.terminateMonitoring();
		controller.waitForTermination(2000);

		// Retrieve written data from the data storage
		final byte[] data = TestRawDataStorage.getInstance().getData(testId);

		// Check some basic numbers, since full decoding is not yet supported
		final ByteBuffer dataBuffer = ByteBuffer.wrap(data);

		// Read the string table offset (last four bytes), plus 8 bytes for the container header
		dataBuffer.position(data.length - 4);
		final int stringTableOffset = dataBuffer.getInt() + 8;

		// Read the number of strings in the string table
		dataBuffer.position(stringTableOffset);
		final int numberOfStrings = VariableLengthEncoding.decodeInt(dataBuffer);

		// Check the size of the string table
		Assert.assertEquals(9, numberOfStrings);
	}

	private IMonitoringController createController(final String testId, final int taskRunInterval, final int deferredWriteDelay) {
		final Configuration configuration = ConfigurationFactory.createDefaultConfiguration();

		configuration.setProperty(ConfigurationFactory.WRITER_CLASSNAME, ChunkingCollector.class.getName());
		configuration.setProperty(ChunkingCollector.CONFIG_SERIALIZER_CLASSNAME, BinarySerializer.class.getName());
		configuration.setProperty(ChunkingCollector.CONFIG_WRITER_CLASSNAME, TestRawDataWriter.class.getName());
		configuration.setProperty(ChunkingCollector.CONFIG_TASK_RUN_INTERVAL, taskRunInterval);
		configuration.setProperty(ChunkingCollector.CONFIG_DEFERRED_WRITE_DELAY, deferredWriteDelay);
		configuration.setProperty(TestRawDataWriter.CONFIG_TEST_ID, testId);

		return MonitoringController.createInstance(configuration);
	}

}