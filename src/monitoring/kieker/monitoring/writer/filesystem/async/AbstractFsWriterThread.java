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

package kieker.monitoring.writer.filesystem.async;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.registry.RegistryRecord;
import kieker.monitoring.writer.AbstractAsyncThread;
import kieker.monitoring.writer.filesystem.MappingFileWriter;

/**
 * @author Matthias Rohr, Andre van Hoorn, Jan Waller
 */
public abstract class AbstractFsWriterThread extends AbstractAsyncThread {

	private static final String FILE_PREFIX = "kieker-";

	protected String fileExtension = ".dat";

	private final MappingFileWriter mappingFileWriter;
	private final String filenamePrefix;
	private final String path;
	private final int maxEntriesInFile;
	private final long maxLogSize;
	private final int maxLogFiles;

	private int entriesInCurrentFileCounter;

	private final LinkedList<FileNameSize> listOfLogFiles; // NOCS NOPMD (we explicitly need LinekdList here)
	private long totalLogSize;

	private final DateFormat dateFormat;

	private long previousFileDate;
	private long sameFilenameCounter;

	public AbstractFsWriterThread(final IMonitoringController monitoringController, final BlockingQueue<IMonitoringRecord> writeQueue,
			final MappingFileWriter mappingFileWriter, final String path, final int maxEntriesInFile, final int maxLogSize, final int maxLogFiles) {
		super(monitoringController, writeQueue);
		this.mappingFileWriter = mappingFileWriter;
		this.path = new File(path).getAbsolutePath();
		this.filenamePrefix = path + File.separatorChar + FILE_PREFIX;
		this.maxEntriesInFile = maxEntriesInFile;
		if ((maxLogSize > 0) || (maxLogFiles > 0)) {
			this.maxLogSize = maxLogSize * 1024L * 1024L; // convert from MiBytes to Bytes
			this.maxLogFiles = maxLogFiles;
			this.listOfLogFiles = new LinkedList<FileNameSize>();
		} else {
			this.maxLogSize = -1;
			this.maxLogFiles = -1;
			this.listOfLogFiles = null; // NOPMD (set explicitly to null)
		}
		// Force to initialize first file!
		this.entriesInCurrentFileCounter = maxEntriesInFile;
		// initialize Date
		this.dateFormat = new SimpleDateFormat("yyyyMMdd'-'HHmmssSSS", Locale.US);
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	protected final String getFilename() {
		final String threadName = this.getName();
		final long date = System.currentTimeMillis();
		if (this.previousFileDate == date) {
			this.sameFilenameCounter++;
		} else {
			this.sameFilenameCounter = 0;
			this.previousFileDate = date;
		}
		final StringBuilder sb = new StringBuilder(this.filenamePrefix.length() + threadName.length() + this.fileExtension.length() + 27);
		sb.append(this.filenamePrefix).append(this.dateFormat.format(new java.util.Date(date))).append("-UTC-") // NOPMD (Date)
				.append(String.format("%03d", this.sameFilenameCounter)).append('-')
				.append(threadName).append(this.fileExtension);
		return sb.toString();
	}

	@Override
	protected final void consume(final IMonitoringRecord monitoringRecord) throws Exception {
		if (monitoringRecord instanceof RegistryRecord) {
			this.mappingFileWriter.write((RegistryRecord) monitoringRecord);
		} else {
			if (++this.entriesInCurrentFileCounter > this.maxEntriesInFile) { // NOPMD
				this.entriesInCurrentFileCounter = 1;
				final String filename = this.getFilename();
				this.prepareFile(filename);
				if (this.listOfLogFiles != null) {
					if (!this.listOfLogFiles.isEmpty()) {
						final FileNameSize fns = this.listOfLogFiles.getLast();
						final long filesize = new File(fns.name).length();
						fns.size = filesize;
						this.totalLogSize += filesize;
					}
					this.listOfLogFiles.add(new FileNameSize(filename));
					if ((this.maxLogFiles > 0) && (this.listOfLogFiles.size() > this.maxLogFiles)) { // too many files (at most one!)
						final FileNameSize removeFile = this.listOfLogFiles.removeFirst();
						if (!new File(removeFile.name).delete()) { // NOCS (nested if)
							throw new IOException("Failed to delete file " + removeFile.name);
						}
						this.totalLogSize -= removeFile.size;
					}
					if (this.maxLogSize > 0) {
						while ((this.listOfLogFiles.size() > 1) && (this.totalLogSize > this.maxLogSize)) {
							final FileNameSize removeFile = this.listOfLogFiles.removeFirst();
							if (!new File(removeFile.name).delete()) { // NOCS (nested if)
								throw new IOException("Failed to delete file " + removeFile.name);
							}
							this.totalLogSize -= removeFile.size;
						}
					}
				}
			}
			this.write(monitoringRecord);
		}
	}

	protected abstract void write(IMonitoringRecord monitoringRecord) throws IOException;

	protected abstract void prepareFile(final String filename) throws IOException;

	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(128);
		sb.append(super.toString());
		sb.append("; Writing to Directory: '");
		sb.append(this.path);
		sb.append('\'');
		return sb.toString();
	}

	private static final class FileNameSize {
		public final String name; // NOCS
		public long size; // NOCS

		public FileNameSize(final String name) {
			this.name = name;
		}
	}
}
