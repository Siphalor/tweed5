package de.siphalor.tweed5.fabric.helper.testmod;

import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.data.hjson.HjsonSerde;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigCommentLoader;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigContainerHelper;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import lombok.CustomLog;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

@CustomLog
public class FabricHelperTestMod implements ModInitializer {
	public static final String MOD_ID = "tweed5_fabric_helper_testmod";

	private TestModConfig config;
	private ConfigContainer<TestModConfig> configContainer;
	private FabricConfigContainerHelper<TestModConfig> configContainerHelper;
	private AttributesReadWriteFilterExtension configFilterExtension;

	@Override
	public void onInitialize() {
		configContainer = TweedPojoWeaverBootstrapper.create(TestModConfig.class).weave();
		configContainer.extension(AttributesReadWriteFilterExtension.class)
				.orElseThrow(() -> new IllegalStateException("AttributesReadWriteFilterExtension not found"))
				.markAttributeForFiltering("reload");
		configFilterExtension = configContainer.extension(AttributesReadWriteFilterExtension.class)
				.orElseThrow(() -> new IllegalStateException("AttributesReadWriteFilterExtension not found"));

		configContainer.initialize();

		configContainerHelper = FabricConfigContainerHelper.create(
				configContainer,
				new HjsonSerde(new HjsonWriter.Options()),
				MOD_ID
		);
		FabricConfigCommentLoader.builder()
				.configContainer(configContainer)
				.modId(MOD_ID)
				.prefix(MOD_ID + ".config")
				.build()
				.loadCommentsFromLanguageFile("en_us");

		config = configContainerHelper.loadAndUpdateInConfigDirectory(TestModConfig::new);

		log.info("Hello " + config.helloStart() + config.helloEnd());

		ServerLifecycleEvents.SERVER_STARTED.register(_server -> onServerStarted());
	}

	private void onServerStarted() {
		configContainerHelper.readPartialConfigInConfigDirectory(config, patchwork ->
			configFilterExtension.addFilter(patchwork, "scope", "game")
		);

		log.info("Hello " + config.helloInGame() + config.helloEnd());
	}
}
