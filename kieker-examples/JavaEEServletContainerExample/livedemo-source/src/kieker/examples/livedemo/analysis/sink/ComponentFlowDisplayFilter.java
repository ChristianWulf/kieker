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

package kicker.examples.livedemo.analysis.sink;

import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;

import kicker.analysis.IProjectContext;
import kicker.common.configuration.Configuration;
import kicker.common.record.controlflow.OperationExecutionRecord;
import kicker.common.util.signature.ClassOperationSignaturePair;

public class ComponentFlowDisplayFilter extends AbstractNonAggregatingDisplayFilter<OperationExecutionRecord, DefaultTagCloudModel> {

	private final Map<String, DefaultTagCloudItem> map = new HashMap<String, DefaultTagCloudItem>();

	public ComponentFlowDisplayFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@Override
	protected DefaultTagCloudModel createChartModel(final int numberOfEntries) {
		return new DefaultTagCloudModel();
	}

	@Override
	protected void fillChartModelWithRecordData(final DefaultTagCloudModel chartModel, final OperationExecutionRecord record, final String minutesAndSeconds,
			final int numberOfEntries) {
		final String shortClassName = ClassOperationSignaturePair.splitOperationSignatureStr(record.getOperationSignature()).getSimpleClassname();

		if (!this.map.containsKey(shortClassName)) {
			final DefaultTagCloudItem item = new DefaultTagCloudItem(shortClassName, 0);
			this.map.put(shortClassName, item);
			chartModel.addTag(item);
		}

		final DefaultTagCloudItem item = this.map.get(shortClassName);
		item.setStrength(item.getStrength() + 1);
	}

}
