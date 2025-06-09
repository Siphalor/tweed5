package de.siphalor.tweed5.core.impl;

import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultConfigContainerTest {

	@Test
	void extensionSetup() {
		var configContainer = new DefaultConfigContainer<>();
		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.EXTENSIONS_SETUP);

		configContainer.registerExtension(ExtensionA.class);
		configContainer.finishExtensionSetup();

		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.TREE_SETUP);
		assertThat(configContainer.extensions())
				.satisfiesExactlyInAnyOrder(
						extension -> assertThat(extension).isInstanceOf(ExtensionA.class),
						extension -> assertThat(extension).isInstanceOf(ExtensionBImpl.class)
				);
	}

	@Test
	void extensionSetupDefaultOverride() {
		var configContainer = new DefaultConfigContainer<>();
		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.EXTENSIONS_SETUP);

		configContainer.registerExtensions(ExtensionA.class, ExtensionBNonDefaultImpl.class);
		configContainer.finishExtensionSetup();

		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.TREE_SETUP);
		assertThat(configContainer.extensions())
				.satisfiesExactlyInAnyOrder(
						extension -> assertThat(extension).isInstanceOf(ExtensionA.class),
						extension -> assertThat(extension).isInstanceOf(ExtensionBNonDefaultImpl.class)
				);
	}

	@Test
	void extensionSetupMissingDefault() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionMissingDefault.class);
		assertThatThrownBy(configContainer::finishExtensionSetup).hasMessageContaining("ExtensionMissingDefault");
	}

	@Test
	void extensionSetupAbstractDefault() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionSelfReferencingDefault.class);
		assertThatThrownBy(configContainer::finishExtensionSetup).hasMessageContaining("ExtensionSelfReferencingDefault");
	}

	@Test
	void extensionSetupWrongDefault() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionWrongDefault.class);
		assertThatThrownBy(configContainer::finishExtensionSetup)
				.hasMessageContaining("ExtensionWrongDefault", "ExtensionBImpl");
	}

	@Test
	void extensionSetupNonStaticDefault() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionNonStaticDefault.class);
		assertThatThrownBy(configContainer::finishExtensionSetup)
				.hasMessageContaining("ExtensionNonStaticDefault", "static");
	}

	@Test
	void extensionSetupNonPublicDefault() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionNonPublicDefault.class);
		assertThatThrownBy(configContainer::finishExtensionSetup)
				.hasMessageContaining("ExtensionNonPublicDefault", "public");
	}

	@Test
	void extension() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtensions(ExtensionA.class, ExtensionB.class);
		configContainer.finishExtensionSetup();

		assertThat(configContainer.extension(ExtensionA.class)).containsInstanceOf(ExtensionA.class);
		assertThat(configContainer.extension(ExtensionB.class)).containsInstanceOf(ExtensionBImpl.class);
		assertThat(configContainer.extension(ExtensionBImpl.class)).containsInstanceOf(ExtensionBImpl.class);
		assertThat(configContainer.extension(ExtensionBNonDefaultImpl.class)).isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	void attachTree() {
		var configContainer = new DefaultConfigContainer<Map<String, Object>>();
		configContainer.registerExtension(ExtensionInitTracker.class);
		configContainer.finishExtensionSetup();

		var subEntry = new SimpleConfigEntryImpl<>(configContainer, String.class);
		var compoundEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>)(Class<?>) Map.class,
				(capacity) -> new HashMap<>(capacity * 2, 0.5F),
				Map.of("test", subEntry)
		);

		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.TREE_SETUP);
		configContainer.attachTree(compoundEntry);

		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.TREE_ATTACHED);
		assertThat(configContainer.rootEntry()).isSameAs(compoundEntry);
	}

	@Test
	void createExtensionsData() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionB.class);
		configContainer.finishExtensionSetup();
		var extensionData = configContainer.createExtensionsData();
		assertThat(extensionData).isNotNull()
				.satisfies(
						data -> assertThat(data.isPatchworkPartDefined(ExtensionBData.class)).isTrue(),
						data -> assertThat(data.isPatchworkPartDefined(String.class)).isFalse()
				);
	}

	@Test
	void entryDataExtensions() {
		var configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ExtensionB.class);
		configContainer.finishExtensionSetup();

		assertThat(configContainer.entryDataExtensions()).containsOnlyKeys(ExtensionBData.class);
		//noinspection unchecked
		var registeredExtension = (RegisteredExtensionData<EntryExtensionsData, ExtensionBData>)
				configContainer.entryDataExtensions().get(ExtensionBData.class);

		var extensionsData = configContainer.createExtensionsData();
		registeredExtension.set(extensionsData, new ExtensionBDataImpl("blub"));

		assertThat(((ExtensionBData) extensionsData).test()).isEqualTo("blub");
	}

	@SuppressWarnings("unchecked")
	@Test
	void initialize() {
		var configContainer = new DefaultConfigContainer<Map<String, Object>>();
		configContainer.registerExtension(ExtensionInitTracker.class);
		configContainer.finishExtensionSetup();

		var subEntry = new SimpleConfigEntryImpl<>(configContainer, String.class);
		var compoundEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>)(Class<?>) Map.class,
				(capacity) -> new HashMap<>(capacity * 2, 0.5F),
				Map.of("test", subEntry)
		);
		configContainer.attachTree(compoundEntry);

		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.TREE_ATTACHED);
		configContainer.initialize();
		assertThat(configContainer.setupPhase()).isEqualTo(ConfigContainerSetupPhase.READY);

		var initTracker = configContainer.extension(ExtensionInitTracker.class).orElseThrow();
		assertThat(initTracker.initializedEntries()).containsExactlyInAnyOrder(compoundEntry, subEntry);
	}

	public static class ExtensionA implements TweedExtension {
		public ExtensionA(TweedExtensionSetupContext context) {
			context.registerExtension(ExtensionB.class);
		}

		@Override
		public String getId() {
			return "a";
		}
	}

	public interface ExtensionB extends TweedExtension {
		Class<? extends ExtensionB> DEFAULT = ExtensionBImpl.class;
	}
	public static class ExtensionBImpl implements ExtensionB {
		public ExtensionBImpl(TweedExtensionSetupContext context) {
			context.registerEntryExtensionData(ExtensionBData.class);
		}

		@Override
		public String getId() {
			return "b";
		}
	}
	public static class ExtensionBNonDefaultImpl implements ExtensionB {
		@Override
		public String getId() {
			return "b-non-default";
		}
	}
	public interface ExtensionBData {
		String test();
	}
	record ExtensionBDataImpl(String test) implements ExtensionBData { }

	@Getter
	public static class ExtensionInitTracker implements TweedExtension {
		private final Collection<ConfigEntry<?>> initializedEntries = new ArrayList<>();

		@Override
		public String getId() {
			return "init-tracker";
		}

		@Override
		public void initEntry(ConfigEntry<?> configEntry) {
			initializedEntries.add(configEntry);
		}
	}

	public interface ExtensionMissingDefault extends TweedExtension {}

	public interface ExtensionSelfReferencingDefault extends TweedExtension {
		Class<ExtensionSelfReferencingDefault> DEFAULT = ExtensionSelfReferencingDefault.class;
	}

	public interface ExtensionWrongDefault extends TweedExtension {
		Class<?> DEFAULT = ExtensionBImpl.class;
	}

	public static abstract class ExtensionNonStaticDefault implements TweedExtension {
		public final Class<?> DEFAULT = ExtensionNonStaticDefault.class;
	}

	public static abstract class ExtensionNonPublicDefault implements TweedExtension {
		protected static final Class<?> DEFAULT = ExtensionNonPublicDefault.class;
	}
}
