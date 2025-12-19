package de.siphalor.tweed5.testutils.generic.log;

import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.extension.*;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class LogCaptureMockitoExtension implements Extension, ParameterResolver, AfterEachCallback {
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
			ParameterResolutionException {
		return parameterContext.getParameter().getType() == LogsCaptor.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
			ParameterResolutionException {
		Type logsCaptorType = parameterContext.getParameter().getParameterizedType();
		if (logsCaptorType instanceof ParameterizedType logsCaptorParameterizedType) {
			Type targetType = logsCaptorParameterizedType.getActualTypeArguments()[0];
			Class<?> targetClass = (Class<?>) targetType;
			Logger logger = (Logger) LoggerFactory.getLogger(targetClass);

			logger.info("Resolved logger to {}", Objects.toIdentityString(logger));

			LogsCaptor<?> appender = new LogsCaptor<>(logger);
			appender.setName("test log appender/" + targetClass.getName());
			appender.start();

			getStoreData(extensionContext).appenders.add(appender);

			logger.addAppender(appender);

			return appender;
		}

		throw new ParameterResolutionException("Failed to resolve parameter " + parameterContext.getParameter());
	}

	@Override
	public void afterEach(ExtensionContext context) {
		for (LogsCaptor<?> appender : getStoreData(context).appenders) {
			appender.detach();
		}
	}

	private StoreData getStoreData(ExtensionContext extensionContext) {
		return extensionContext.getStore(ExtensionContext.Namespace.create(getClass(), extensionContext.getRequiredTestMethod()))
				.getOrComputeIfAbsent(StoreData.class, k -> new StoreData(), StoreData.class);
	}

	private static class StoreData {
		private final Collection<LogsCaptor<?>> appenders = new ArrayList<>();
	}
}
