package de.siphalor.tweed5.fabric.helper.api;

import com.google.gson.stream.JsonReader;
import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentPathProcessor;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.data.gson.GsonReader;
import lombok.Builder;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;

@CommonsLog
@Builder
public class FabricConfigCommentLoader {
	private final ConfigContainer<?> configContainer;
	private final String modId;
	/**
	 * The prefix of the language keys, <b>without the trailing dot!</b>
	 */
	private final String prefix;
	/**
	 * An optional suffix of the language keys
	 */
	private final @Nullable String suffix;

	public void loadCommentsFromLanguageFile(String languageCode) {
		CommentLoaderExtension commentLoaderExtension = configContainer.extension(CommentLoaderExtension.class)
				.orElseThrow(() -> new IllegalStateException("CommentLoaderExtension not declared on config"));

		String langFilePath = "assets/" + modId + "/lang/" + languageCode + ".json";

		InputStream langInputStream = getClass().getClassLoader().getResourceAsStream(langFilePath);
		if (langInputStream == null) {
			log.warn("Failed to find language file " + langFilePath + " for loading config comments");
			return;
		}

		try (TweedDataReader reader = new GsonReader(new JsonReader(new InputStreamReader(langInputStream)))) {
			commentLoaderExtension.loadComments(
					reader, new CommentPathProcessor() {
						@Override
						public MatchStatus matches(String path) {
							if (!path.startsWith(prefix)) {
								return MatchStatus.NO;
							} else if (path.length() == prefix.length()) {
								if (suffix != null && !path.endsWith(suffix)) {
									return MatchStatus.NO;
								}
								return MatchStatus.YES;
							} else if (path.charAt(prefix.length()) != '.') {
								return MatchStatus.NO;
							} else if (suffix != null && !path.endsWith(suffix)) {
								return MatchStatus.MAYBE_DEEPER;
							} else {
								return MatchStatus.YES;
							}
						}

						@Override
						public String process(String path) {
							if (path.equals(prefix)) {
								return "";
							}
							path = path.substring(prefix.length() + 1);
							if (suffix != null) {
								path = path.substring(0, path.length() - suffix.length());
							}
							return path;
						}
					}
			);
		} catch (Exception e) {
			log.warn("Failed to load comments from language file", e);
		}
	}
}
