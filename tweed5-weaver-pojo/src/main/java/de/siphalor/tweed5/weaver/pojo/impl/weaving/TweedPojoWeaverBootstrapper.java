package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.patchwork.api.PatchworkClassCreator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClass;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassGenerator;
import de.siphalor.tweed5.patchwork.impl.PatchworkClassPart;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.WeavingContext;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A class that sets up and handles all the bits and bobs for weaving a {@link ConfigContainer} out of a POJO.
 * The POJO must be annotated with {@link PojoWeaving}.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TweedPojoWeaverBootstrapper<T> {
	private final Class<T> pojoClass;
	private final ConfigContainer<T> configContainer;
	private final Collection<TweedPojoWeaver> weavers;
	private PatchworkClass<WeavingContext.ExtensionsData> contextExtensionsDataClass;

	public static <T> TweedPojoWeaverBootstrapper<T> create(Class<T> pojoClass) {
		PojoWeaving rootWeavingConfig = expectAnnotation(pojoClass, PojoWeaving.class);

		//noinspection unchecked
		ConfigContainer<T> configContainer = (ConfigContainer<T>) createConfigContainer((Class<? extends ConfigContainer<?>>) rootWeavingConfig.container());

		Collection<TweedExtension> extensions = loadExtensions(Arrays.asList(rootWeavingConfig.extensions()));
		configContainer.registerExtensions(extensions.toArray(new TweedExtension[0]));
		configContainer.finishExtensionSetup();

		return new TweedPojoWeaverBootstrapper<>(pojoClass, configContainer, loadWeavers(Arrays.asList(rootWeavingConfig.weavers())));
	}

	private static Collection<TweedExtension> loadExtensions(Collection<Class<? extends TweedExtension>> extensionClasses) {
		try {
			return loadSingleServices(extensionClasses);
		} catch (Exception e) {
			throw new PojoWeavingException("Failed to load Tweed extensions", e);
		}
	}

	private static Collection<TweedPojoWeaver> loadWeavers(Collection<Class<? extends TweedPojoWeaver>> weaverClasses) {
		List<TweedPojoWeaver> weavers = new ArrayList<>();
		for (Class<? extends TweedPojoWeaver> weaverClass : weaverClasses) {
			weavers.add(checkImplementsAndInstantiate(TweedPojoWeaver.class, weaverClass));
		}
		return weavers;
	}

	private static ConfigContainer<?> createConfigContainer(Class<? extends ConfigContainer<?>> containerClass) {
		try {
			return checkImplementsAndInstantiate(ConfigContainer.class, containerClass);
		} catch (Exception e) {
			throw new PojoWeavingException("Failed to instantiate config container");
		}
	}


	private static <S> Collection<S> loadSingleServices(Collection<Class<? extends S>> serviceClasses) {
		Collection<S> services = new ArrayList<>(serviceClasses.size());
		for (Class<? extends S> serviceClass : serviceClasses) {
			try {
				services.add(loadSingleService(serviceClass));
			} catch (Exception e) {
				throw new PojoWeavingException("Failed to instantiate single service " + serviceClass.getName(), e);
			}
		}
		return services;
	}

	private static <S> S loadSingleService(Class<S> serviceClass) {
		try {
			ServiceLoader<S> loader = ServiceLoader.load(serviceClass);
			Iterator<S> iterator = loader.iterator();
			if (!iterator.hasNext()) {
				throw new PojoWeavingException("Could not find any service for class " + serviceClass.getName());
			}
			S service = iterator.next();

			if (iterator.hasNext()) {
				throw new PojoWeavingException(
						"Found multiple services for class " + serviceClass.getName() + ": " +
								createInstanceDebugStringFromIterator(loader.iterator())
				);
			}

			return service;
		} catch (ServiceConfigurationError e) {
			throw new PojoWeavingException("Failed to load service " + serviceClass.getName(), e);
		}
	}

	private static String createInstanceDebugStringFromIterator(Iterator<?> iterator) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[ ");
		while (iterator.hasNext()) {
			stringBuilder.append(createInstanceDebugDescriptor(iterator.next()));
			stringBuilder.append(", ");
		}
		stringBuilder.append(" ]");
		return stringBuilder.toString();
	}

	private static String createInstanceDebugDescriptor(@Nullable Object object) {
		if (object == null) {
			return "null";
		} else {
			return object.getClass().getName() + "@" + System.identityHashCode(object);
		}
	}

	private static <A extends Annotation> A expectAnnotation(Class<?> clazz, Class<A> annotationClass) {
		A annotation = clazz.getAnnotation(annotationClass);
		if (annotation == null) {
			throw new PojoWeavingException("Annotation " + annotationClass.getName() + " must be defined on class " + clazz);
		} else {
			return annotation;
		}
	}

	private static <T> T checkImplementsAndInstantiate(Class<T> superClass, Class<? extends T> clazz) {
		if (!superClass.isAssignableFrom(clazz)) {
			throw new PojoWeavingException("Class " + clazz.getName() + " must extend/implement " + superClass.getName());
		}
		return instantiate(clazz);
	}

	private static <T> T instantiate(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getConstructor();
			return constructor.newInstance();
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
				 IllegalAccessException e) {
			throw new PojoWeavingException("Failed to instantiate class " + clazz.getName(), e);
		}
	}

	public ConfigContainer<T> weave() {
		setupWeavers();
		WeavingContext weavingContext = createWeavingContext();

		ConfigEntry<T> rootEntry = this.weaveEntry(pojoClass, weavingContext);
		configContainer.attachAndSealTree(rootEntry);

		return configContainer;
	}

	private void setupWeavers() {
		Map<Class<?>, RegisteredExtensionDataImpl<?>> registeredExtensions = new HashMap<>();

		TweedPojoWeaver.SetupContext setupContext = new TweedPojoWeaver.SetupContext() {
			@Override
			public <E> RegisteredExtensionData<WeavingContext.ExtensionsData, E> registerWeavingContextExtensionData(
					Class<E> dataClass
			) {
				RegisteredExtensionDataImpl<E> registeredExtension = new RegisteredExtensionDataImpl<>();
				registeredExtensions.put(dataClass, registeredExtension);
				return registeredExtension;
			}
		};

		for (TweedPojoWeaver weaver : weavers) {
			weaver.setup(setupContext);
		}

		PatchworkClassCreator<WeavingContext.ExtensionsData> weavingContextCreator = PatchworkClassCreator.<WeavingContext.ExtensionsData>builder()
				.classPackage(this.getClass().getPackage().getName() + ".generated")
				.classPrefix("WeavingContext$")
				.patchworkInterface(WeavingContext.ExtensionsData.class)
				.build();

		try {
			this.contextExtensionsDataClass = weavingContextCreator.createClass(registeredExtensions.keySet());

			for (PatchworkClassPart part : this.contextExtensionsDataClass.parts()) {
				RegisteredExtensionDataImpl<?> registeredExtension = registeredExtensions.get(part.partInterface());
				registeredExtension.setter(part.fieldSetter());
			}
		} catch (PatchworkClassGenerator.GenerationException e) {
			throw new PojoWeavingException("Failed to create weaving context extensions data");
		}
	}

	private WeavingContext createWeavingContext() {
		try {
			WeavingContext.ExtensionsData extensionsData = (WeavingContext.ExtensionsData) contextExtensionsDataClass.constructor().invoke();
			return WeavingContext.builder()
					.extensionsData(extensionsData)
					.weavingFunction(this::weaveEntry)
					.build();
		} catch (Throwable e) {
			throw new PojoWeavingException("Failed to create weaving context's extension data");
		}
	}

	private <U> ConfigEntry<U> weaveEntry(Class<U> dataClass, WeavingContext context) {
		for (TweedPojoWeaver weaver : weavers) {
			ConfigEntry<U> configEntry = weaver.weaveEntry(dataClass, context);
			if (configEntry != null) {
				configEntry.seal(configContainer);
				return configEntry;
			}
		}

		throw new PojoWeavingException("Failed to weave " + dataClass.getName() + ": No matching weavers found");
	}

	@Setter
	private static class RegisteredExtensionDataImpl<E> implements RegisteredExtensionData<WeavingContext.ExtensionsData, E> {
		private MethodHandle setter;

		@Override
		public void set(WeavingContext.ExtensionsData patchwork, E extension) {
			try {
				setter.invokeWithArguments(patchwork, extension);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
