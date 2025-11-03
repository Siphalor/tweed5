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
import lombok.CustomLog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static de.siphalor.tweed5.coat.bridge.api.TweedCoatMappingUtils.translatableComponent;

@CustomLog
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

		KeyBindingHelper.registerKeyBinding(new ScreenKeyBinding(
				MOD_ID + ".config",
				GLFW.GLFW_KEY_T,
				//# if MC_VERSION_NUMBER >= 12109
				KeyMapping.Category.MISC
				//# else
				//- "key.categories.misc"
				//# end
		));

		log.info("Current config: " + config);
	}

	private class ScreenKeyBinding extends KeyMapping implements PriorityKeyBinding {
		//# if MC_VERSION_NUMBER >= 12109
		public ScreenKeyBinding(String name, int key, Category category) {
		//# else
		//- public ScreenKeyBinding(String name, int key, String category) {
		//# end
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
							.title(translatableComponent(MOD_ID + ".title"))
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
