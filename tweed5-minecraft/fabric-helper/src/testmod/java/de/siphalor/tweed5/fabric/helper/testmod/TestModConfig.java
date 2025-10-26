package de.siphalor.tweed5.fabric.helper.testmod;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.DefaultWeavingExtensions;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.AttributesPojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.AutoReadWritePojoWeavingProcessor;
import de.siphalor.tweed5.weaver.pojoext.serde.api.auto.DefaultReadWriteMappings;
import lombok.Data;

@PojoWeaving(extensions = {
		CommentLoaderExtension.class,
		ReadWriteExtension.class,
		PatchExtension.class,
		AttributesExtension.class,
		AttributesReadWriteFilterExtension.class,
})
@PojoWeavingExtension(AutoReadWritePojoWeavingProcessor.class)
@PojoWeavingExtension(AttributesPojoWeavingProcessor.class)
@DefaultWeavingExtensions
@DefaultReadWriteMappings
@CompoundWeaving(namingFormat = "kebab_case")
@Data
public class TestModConfig {
	private String helloStart = "Minecraft";
	@Attribute(key = "scope", values = "game")
	private String helloInGame = "Game";
	private String helloEnd = "!";
}
