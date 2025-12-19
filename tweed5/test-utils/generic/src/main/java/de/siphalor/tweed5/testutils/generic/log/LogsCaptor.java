package de.siphalor.tweed5.testutils.generic.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LogsCaptor<T> extends AppenderBase<ILoggingEvent> {
	private final Logger logger;
	private final List<ILoggingEvent> logs = new ArrayList<>();

	public List<ILoggingEvent> getLogsForLevel(Level level) {
		return logs.stream().filter(log -> log.getLevel().equals(level)).toList();
	}

	@Override
	protected void append(ILoggingEvent event) {
		logs.add(event);
	}

	public void clear() {
		logs.clear();
	}

	public void detach() {
		logger.detachAppender(this);
	}
}
