/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
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

package kieker.analysis.reader;

import kieker.analysis.AnalysisController;
import kieker.common.record.IMonitoringRecordReceiver;

/**
 * TODO: refactor analysis readers similar to monitoring writers!
 * See ticket http://samoa.informatik.uni-kiel.de:8000/kieker/ticket/161
 * 
 * @author Andre van Hoorn
 */
public interface IMonitoringReader {

	/**
	 * Initialize instance from passed initialization string which is typically
	 * a list of separated parameter/values pairs. The implementing class {@link AbstractMonitoringReader} includes convenient methods to extract
	 * configuration values from an initString.
	 * 
	 * @param initString
	 *            the initialization string
	 * @return true if the initialization was successful; false if an error occurred
	 */
	public boolean init(String initString);

	/**
	 * Adds the given record receiver. This method is only used by the framework
	 * and should not be called manually to register a receiver. Use an {@link AnalysisController} instead.
	 * 
	 * @param receiver
	 *            the receiver
	 */
	public void addRecordReceiver(IMonitoringRecordReceiver receiver);

	/**
	 * Starts the reader. This method is intended to be a blocking operation,
	 * i.e., it is assumed that reading has finished before this method returns.
	 * The method should indicate an error by the return value false.
	 * 
	 * In asynchronous scenarios, the {@link #terminate()} method can be used
	 * to initiate the termination of this method.
	 * 
	 * @return true if reading was successful; false if an error occurred
	 */
	public boolean read();

	/**
	 * Initiates a termination of the reader. This method is only used by the
	 * framework and should not be called manually to register a receiver. Use
	 * the method {@link kieker.analysis.AnalysisController#terminate()} instead.
	 * 
	 * After receiving this notification,
	 * the reader should terminate its {@link #read()} method.
	 */
	public void terminate();
}
