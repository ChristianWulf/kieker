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

package kicker.examples.livedemo.view;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import kicker.analysis.exception.AnalysisConfigurationException;
import kicker.analysis.plugin.filter.forward.ListCollectionFilter;
import kicker.common.logging.Log;
import kicker.common.logging.LogFactory;
import kicker.examples.livedemo.analysis.LiveDemoAnalysis;
import kicker.examples.livedemo.analysis.sink.CPUUtilizationDisplayFilter;
import kicker.examples.livedemo.analysis.sink.ClassLoadingDisplayFilter;
import kicker.examples.livedemo.analysis.sink.CompilationDisplayFilter;
import kicker.examples.livedemo.analysis.sink.ComponentFlowDisplayFilter;
import kicker.examples.livedemo.analysis.sink.GCCountDisplayFilter;
import kicker.examples.livedemo.analysis.sink.GCTimeDisplayFilter;
import kicker.examples.livedemo.analysis.sink.JVMHeapDisplayFilter;
import kicker.examples.livedemo.analysis.sink.JVMNonHeapDisplayFilter;
import kicker.examples.livedemo.analysis.sink.MemoryDisplayFilter;
import kicker.examples.livedemo.analysis.sink.MethodFlowDisplayFilter;
import kicker.examples.livedemo.analysis.sink.MethodResponsetimeDisplayFilter;
import kicker.examples.livedemo.analysis.sink.SwapDisplayFilter;
import kicker.examples.livedemo.analysis.sink.ThreadsStatusDisplayFilter;
import kicker.examples.livedemo.common.EnrichedOperationExecutionRecord;
import kicker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

/**
 * @author Bjoern Weissenfels, Nils Christian Ehmke
 * 
 * @since 1.9
 */
@ManagedBean(name = "analysisBean", eager = true)
@ApplicationScoped
public class AnalysisBean {

	private static final Log LOG = LogFactory.getLog(AnalysisBean.class);

	public AnalysisBean() {
		try {
			LiveDemoAnalysis.getInstance().initializeAnalysis();
		} catch (final IllegalStateException ex) {
			AnalysisBean.LOG.error("Could not initialize analysis", ex);
		} catch (final AnalysisConfigurationException ex) {
			AnalysisBean.LOG.error("Could not initialize analysis", ex);
		}
	}

	@PostConstruct
	protected void startThreads() {
		LiveDemoAnalysis.getInstance().startAnalysis();
	}

	public SystemModelRepository getSystemModelRepository() {
		return LiveDemoAnalysis.getInstance().getSystemModelRepository();
	}

	public CPUUtilizationDisplayFilter getCPUUtilizationDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getCPUUtilizationDisplayFilter();
	}

	public MemoryDisplayFilter getMemoryDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getMemoryDisplayFilter();
	}

	public SwapDisplayFilter getSwapDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getSwapDisplayFilter();
	}

	public ListCollectionFilter<EnrichedOperationExecutionRecord> getRecordListFilter() {
		return LiveDemoAnalysis.getInstance().getRecordListFilter();
	}

	public MethodResponsetimeDisplayFilter getMethodResponsetimeDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getMethodResponsetimeDisplayFilter();
	}

	public MethodFlowDisplayFilter getMethodFlowDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getMethodFlowDisplayFilter();
	}

	public ComponentFlowDisplayFilter getComponentFlowDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getComponentFlowDisplayFilter();
	}

	public ClassLoadingDisplayFilter getClassLoadingDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getClassLoadingDisplayFilter();
	}

	public ThreadsStatusDisplayFilter getThreadsStatusDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getThreadsStatusDisplayFilter();
	}

	public CompilationDisplayFilter getJitCompilationDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getJitCompilationDisplayFilter();
	}

	public GCCountDisplayFilter getGcCountDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getGcCountDisplayFilter();
	}

	public GCTimeDisplayFilter getGcTimeDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getGcTimeDisplayFilter();
	}

	public JVMHeapDisplayFilter getJvmHeapMemoryDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getJvmHeapMemoryDisplayFilter();
	}

	public JVMNonHeapDisplayFilter getJvmNonHeapMemoryDisplayFilter() {
		return LiveDemoAnalysis.getInstance().getJvmNonHeapMemoryDisplayFilter();
	}

}
