/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.examples.livedemo.analysis.display;

import java.util.Map;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import kieker.analysis.IProjectContext;
import kieker.common.configuration.Configuration;
import kieker.common.record.system.MemSwapUsageRecord;
import kieker.examples.livedemo.common.LimitedHashMap;

/**
 * @author Nils Christian Ehmke
 * 
 * @since 1.10
 */
public class SwapDisplayFilter extends AbstractNonAggregatingDisplayFilter<MemSwapUsageRecord, CartesianChartModel> {

	private Map<Object, Number> usedSwapData;
	private Map<Object, Number> freeSwapData;

	private ChartSeries usedSwapSeries;
	private ChartSeries freeSwapSeries;

	public SwapDisplayFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	@Override
	protected CartesianChartModel createChartModel(final int numberOfEntries) {
		final CartesianChartModel model = new CartesianChartModel();

		this.usedSwapSeries = new ChartSeries();
		this.freeSwapSeries = new ChartSeries();

		this.usedSwapData = new LimitedHashMap<>(numberOfEntries);
		this.freeSwapData = new LimitedHashMap<>(numberOfEntries);

		model.addSeries(this.usedSwapSeries);
		model.addSeries(this.freeSwapSeries);

		this.usedSwapSeries.setData(this.usedSwapData);
		this.freeSwapSeries.setData(this.freeSwapData);

		this.usedSwapSeries.setLabel("Used Swap");
		this.freeSwapSeries.setLabel("Free Swap");

		return model;
	}

	@Override
	protected void fillChartModelWithRecordData(final CartesianChartModel chartModel, final MemSwapUsageRecord record, final String minutesAndSeconds,
			final int numberOfEntries) {
		this.usedSwapData.put(minutesAndSeconds, record.getSwapUsed());
		this.freeSwapData.put(minutesAndSeconds, record.getSwapFree());
	}

}
