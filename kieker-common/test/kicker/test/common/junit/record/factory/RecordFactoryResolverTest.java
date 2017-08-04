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

package kicker.test.common.junit.record.factory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import kicker.common.record.IMonitoringRecord;
import kicker.common.record.factory.IRecordFactory;
import kicker.common.record.factory.RecordFactoryResolver;
import kicker.common.record.flow.trace.operation.AfterOperationEvent;
import kicker.common.record.flow.trace.operation.AfterOperationEventFactory;
import kicker.test.common.junit.AbstractKiekerTest;
import kicker.test.common.util.record.factory.TestRecord;

/**
 * @author Christian Wulf
 *
 * @since 1.11
 */
public class RecordFactoryResolverTest extends AbstractKiekerTest {

	private RecordFactoryResolver recordFactoryResolver;

	public RecordFactoryResolverTest() {
		// Nothing to do
	}

	@Before
	public void before() throws Exception {
		this.recordFactoryResolver = new RecordFactoryResolver();
	}

	@Test
	public void testRecordWithFactory() {
		final String recordClassName = AfterOperationEvent.class.getName();
		final IRecordFactory<? extends IMonitoringRecord> recordFactory = this.recordFactoryResolver.get(recordClassName);
		Assert.assertEquals(AfterOperationEventFactory.class, recordFactory.getClass());
	}

	@Test
	public void testRecordWithoutFactory() {
		final String recordClassName = TestRecord.class.getName();
		@SuppressWarnings("unused")
		final IRecordFactory<? extends IMonitoringRecord> recordFactory = this.recordFactoryResolver.get(recordClassName);
		Assert.assertNull(recordFactory);
	}

	@Test
	public void testNotExistingRecord() {
		final String recordClassName = "record.that.does.not.exist";
		@SuppressWarnings("unused")
		final IRecordFactory<? extends IMonitoringRecord> recordFactory = this.recordFactoryResolver.get(recordClassName);
		Assert.assertNull(recordFactory);
	}

}
