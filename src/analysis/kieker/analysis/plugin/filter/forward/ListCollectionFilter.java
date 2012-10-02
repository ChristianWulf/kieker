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

package kieker.analysis.plugin.filter.forward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;

/**
 * @param <T>
 * 
 * @author Nils Ehmke, Jan Waller
 */
@Plugin(programmaticOnly = true,
		description = "A filter collecting incoming objects in a list (mostly used in testing scenarios)",
		outputPorts = @OutputPort(name = ListCollectionFilter.OUTPUT_PORT_NAME, eventTypes = { Object.class }, description = "Provides each incoming object"))
public class ListCollectionFilter<T> extends AbstractFilterPlugin {

	public static final String INPUT_PORT_NAME = "inputObject";
	public static final String OUTPUT_PORT_NAME = "outputObjects";

	private final List<T> list = Collections.synchronizedList(new ArrayList<T>());

	public ListCollectionFilter(final Configuration configuration) {
		super(configuration);
	}

	@InputPort(name = ListCollectionFilter.INPUT_PORT_NAME)
	@SuppressWarnings("unchecked")
	public void input(final Object data) {
		this.list.add((T) data);
		super.deliver(OUTPUT_PORT_NAME, data);
	}

	public void clear() {
		this.list.clear();
	}

	@SuppressWarnings("unchecked")
	public List<T> getList() {
		return new CopyOnWriteArrayList<T>((T[]) this.list.toArray());
	}

	/**
	 * Returns the current number of collected objects.
	 * 
	 * @return
	 */
	public int size() {
		return this.list.size();
	}

	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}