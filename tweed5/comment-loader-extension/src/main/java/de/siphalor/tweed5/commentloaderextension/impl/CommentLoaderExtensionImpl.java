package de.siphalor.tweed5.commentloaderextension.impl;

import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentPathProcessor;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.dataapi.api.IntuitiveVisitingTweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingConfigEntryVisitor;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.Getter;
import lombok.Value;
import lombok.extern.apachecommons.CommonsLog;
import org.jspecify.annotations.Nullable;

import java.util.*;

@CommonsLog
public class CommentLoaderExtensionImpl implements CommentLoaderExtension, CommentModifyingExtension {
	private final ConfigContainer<?> configContainer;
	private final PatchworkPartAccess<String> loadedCommentAccess;
	private @Nullable CommentExtension commentExtension;

	public CommentLoaderExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext setupContext) {
		this.configContainer = configContainer;
		setupContext.registerExtension(CommentExtension.class);

		loadedCommentAccess = setupContext.registerEntryExtensionData(String.class);
	}

	@Override
	public void extensionsFinalized() {
		commentExtension = configContainer.extension(CommentExtension.class)
				.orElseThrow(() -> new IllegalStateException("CommentExtension not found"));
	}

	@Override
	public Middleware<CommentProducer> commentMiddleware() {
		return new Middleware<CommentProducer>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public Set<String> mustComeBefore() {
				return Collections.emptySet();
			}

			@Override
			public Set<String> mustComeAfter() {
				return Collections.singleton(Middleware.DEFAULT_END);
			}

			@Override
			public CommentProducer process(CommentProducer inner) {
				return entry -> {
					String loadedComment = entry.extensionsData().get(loadedCommentAccess);
					String innerComment = inner.createComment(entry);
					if (loadedComment != null) {
						if (innerComment.isEmpty()) {
							return loadedComment;
						} else {
							return innerComment + loadedComment;
						}
					}
					return innerComment;
				};
			}
		};
	}

	@Override
	public void loadComments(TweedDataReader reader, CommentPathProcessor pathProcessor) {
		if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.EXTENSIONS_SETUP) <= 0) {
			throw new IllegalStateException("Comments cannot be loaded before the extensions are finalized");
		}

		CollectingCommentsVisitor collectingCommentsVisitor = new CollectingCommentsVisitor(pathProcessor);
		try {
			new IntuitiveVisitingTweedDataReader(collectingCommentsVisitor).readMap(reader);
		} catch (TweedDataReadException e) {
			log.error("Failed to load comments", e);
		}

		Map<String, String> commentsByKey = collectingCommentsVisitor.commentsByKey();
		PathTracking pathTracking = PathTracking.create();
		configContainer.rootEntry().visitInOrder(new PathTrackingConfigEntryVisitor(
				entry -> {
					String key = pathTracking.currentPath();
					if (!key.isEmpty() && key.charAt(0) == '.') {
						key = key.substring(1);
					}
					entry.extensionsData().set(loadedCommentAccess, commentsByKey.get(key));
				},
				pathTracking
		));

		if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.INITIALIZED) >= 0) {
			assert commentExtension != null;
			commentExtension.recomputeFullComments();
		}
	}

	private static class CollectingCommentsVisitor implements TweedDataVisitor {
		private final CommentPathProcessor pathProcessor;
		@Getter
		private final Map<String, String> commentsByKey = new HashMap<>();
		private final Deque<State> stateStack = new ArrayDeque<>();
		private State currentState = new State(CommentPathProcessor.MatchStatus.MAYBE_DEEPER, "");

		public CollectingCommentsVisitor(CommentPathProcessor pathProcessor) {
			this.pathProcessor = pathProcessor;
			stateStack.push(currentState);
		}

		@Override
		public void visitNull() {}

		@Override
		public void visitBoolean(boolean value) {}

		@Override
		public void visitByte(byte value) {}

		@Override
		public void visitShort(short value) {}

		@Override
		public void visitInt(int value) {}

		@Override
		public void visitLong(long value) {}

		@Override
		public void visitFloat(float value) {}

		@Override
		public void visitDouble(double value) {}

		@Override
		public void visitString(String value) {
			if (currentState.matchStatus() == CommentPathProcessor.MatchStatus.YES) {
				commentsByKey.put(pathProcessor.process(currentState.key()), value);
			}
		}

		@Override
		public void visitListStart() {
			stateStack.push(State.IGNORED);
		}

		@Override
		public void visitListEnd() {
			stateStack.pop();
		}

		@Override
		public void visitMapStart() {
			stateStack.push(currentState);
			currentState = State.IGNORED;
		}

		@Override
		public void visitMapEntryKey(String key) {
			State state = stateStack.peek();
			assert state != null;
			if (state.matchStatus() == CommentPathProcessor.MatchStatus.NO) {
				return;
			}

			String fullPath;
			if (state.key().isEmpty()) {
				fullPath = key;
			} else {
				fullPath = state.key() + "." + key;
			}

			CommentPathProcessor.MatchStatus matchStatus = pathProcessor.matches(fullPath);
			if (matchStatus == CommentPathProcessor.MatchStatus.NO) {
				currentState = State.IGNORED;
			} else {
				currentState = new State(matchStatus, fullPath);
			}
		}

		@Override
		public void visitMapEnd() {
			currentState = stateStack.pop();
		}

		@Override
		public void visitDecoration(TweedDataDecoration decoration) {}

		@Value
		private static class State {
			private static final State IGNORED = new State(CommentPathProcessor.MatchStatus.NO, "");

			CommentPathProcessor.MatchStatus matchStatus;
			String key;
		}
	}
}
