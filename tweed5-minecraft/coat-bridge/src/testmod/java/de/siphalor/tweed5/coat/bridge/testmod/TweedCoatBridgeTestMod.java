package de.siphalor.tweed5.coat.bridge.testmod;

import de.siphalor.amecs.api.PriorityKeyBinding;
import de.siphalor.coat.screen.ConfigScreen;
import de.siphalor.tweed5.coat.bridge.api.ConfigScreenCreateParams;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatBridgeExtension;
import de.siphalor.tweed5.coat.bridge.api.TweedCoatMappers;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.data.hjson.HjsonSerde;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.fabric.helper.api.FabricConfigContainerHelper;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverBootstrapper;
import lombok.extern.apachecommons.CommonsLog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

@CommonsLog
public class TweedCoatBridgeTestMod implements ClientModInitializer {
	public static final String MOD_ID = "tweed5_coat_bridge_testmod";

	private static final TweedCoatBridgeTestModConfig DEFAULT_CONFIG_VALUE = new TweedCoatBridgeTestModConfig();

	private ConfigContainer<TweedCoatBridgeTestModConfig> configContainer;
	private TweedCoatBridgeExtension configCoatBridgeExtension;
	private FabricConfigContainerHelper<TweedCoatBridgeTestModConfig> configContainerHelper;
	private TweedCoatBridgeTestModConfig config;

	@Override
	public void onInitializeClient() {
		configContainer = TweedPojoWeaverBootstrapper.create(TweedCoatBridgeTestModConfig.class).weave();
		configCoatBridgeExtension = configContainer.extension(TweedCoatBridgeExtension.class)
				.orElseThrow(() -> new IllegalStateException("TweedCoatBridgeExtension not found"));
		Arrays.asList(
				TweedCoatMappers.compoundCategoryMapper(),
				TweedCoatMappers.stringTextMapper(),
				TweedCoatMappers.integerTextMapper()
		).forEach(configCoatBridgeExtension::addMapper);

		configContainer.initialize();

		configContainerHelper = FabricConfigContainerHelper.create(
				configContainer,
				new HjsonSerde(new HjsonWriter.Options()),
				MOD_ID
		);

		config = configContainerHelper.loadAndUpdateInConfigDirectory(() -> DEFAULT_CONFIG_VALUE);

		KeyBindingHelper.registerKeyBinding(new ScreenKeyBinding(MOD_ID + ".config", 84, KeyMapping.Category.MISC));

		log.info("Current config: " + config);
	}

	private class ScreenKeyBinding extends KeyMapping implements PriorityKeyBinding {
		public ScreenKeyBinding(String name, int key, Category category) {
			super(name, key, category);
		}

		@Override
		public boolean onPressedPriority() {
			if (!(Minecraft.getInstance().screen instanceof TitleScreen)) {
				return false;
			}

			ConfigScreen configScreen = configCoatBridgeExtension.createConfigScreen(
					ConfigScreenCreateParams.<TweedCoatBridgeTestModConfig>builder()
							.translationKeyPrefix(MOD_ID + ".config")
							.title(Component.translatable(MOD_ID + ".title"))
							.rootEntry(configContainer.rootEntry())
							.currentValue(config)
							.defaultValue(DEFAULT_CONFIG_VALUE)
							.saveHandler(value -> {
								config = value;
								log.info("Updated config: " + config);
								configContainerHelper.writeConfigInConfigDirectory(config);
							})
							.build()
			);
			Minecraft.getInstance().setScreen(configScreen);
			return true;
		}
	}
}
