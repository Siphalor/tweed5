package de.siphalor.tweed5.fabric.helper.testmod;

import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.fabric.helper.api.DefaultTweedMinecraftWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.TweedExtension;
import de.siphalor.tweed5.weaver.pojoext.attributes.api.Attribute;
import lombok.Data;

@DefaultTweedMinecraftWeaving
@TweedExtension(CommentLoaderExtension.class)
@CompoundWeaving(namingFormat = "kebab_case")
@Data
public class TestModConfig {
	private String helloStart = "Minecraft";
	@Attribute(key = "scope", values = "game")
	private String helloInGame = "Game";
	private String helloEnd = "!";
}
