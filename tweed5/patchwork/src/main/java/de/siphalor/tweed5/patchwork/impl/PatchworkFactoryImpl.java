package de.siphalor.tweed5.patchwork.impl;

import de.siphalor.tweed5.patchwork.api.InvalidPatchworkAccessException;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PatchworkFactoryImpl implements PatchworkFactory {
	private final UUID factoryUuid;
	private final Class<?>[] partClasses;

	@Override
	public Patchwork create() {
		return new PatchworkImpl(new Object[partClasses.length]);
	}

	public static class Builder implements PatchworkFactory.Builder {
		private final UUID factoryUuid = UUID.randomUUID();
		private final List<Class<?>> partClasses = new ArrayList<>();
		private boolean built;

		@Override
		public <T> PatchworkPartAccess<T> registerPart(Class<T> partClass) {
			requireFresh();
			partClasses.add(partClass);
			return new PartAccessImpl<>(factoryUuid, partClasses.size() - 1);
		}

		@Override
		public PatchworkFactory build() {
			requireFresh();
			built = true;
			return new PatchworkFactoryImpl(factoryUuid, partClasses.toArray(new Class<?>[0]));
		}

		private void requireFresh() {
			if (built) {
				throw new IllegalStateException("Builder has already been used.");
			}
		}
	}

	@RequiredArgsConstructor
	private static class PartAccessImpl<T extends @Nullable Object> implements PatchworkPartAccess<T> {
		private final UUID factoryUuid;
		private final int partIndex;
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private class PatchworkImpl implements Patchwork {
		private final @Nullable Object[] partValues;

		@Override
		public <T extends @Nullable Object> T get(PatchworkPartAccess<T> access) throws InvalidPatchworkAccessException {
			PartAccessImpl<T> castedAccess = validatePartAccess(access);
			//noinspection unchecked
			return (T) partValues[castedAccess.partIndex];
		}

		@Override
		public <T extends @Nullable Object> void set(PatchworkPartAccess<T> access, T value) throws InvalidPatchworkAccessException {
			PartAccessImpl<T> castedAccess = validatePartAccess(access);

			if (value != null && !partClasses[castedAccess.partIndex].isInstance(value)) {
				throw new IllegalArgumentException(
						"value " + value + " of type " + value.getClass().getName() +
								" doesn't match registered value class " + partClasses[castedAccess.partIndex].getName()
				);
			}

			partValues[castedAccess.partIndex] = value;
		}

		private <T extends @Nullable Object> PartAccessImpl<T> validatePartAccess(PatchworkPartAccess<T> access)
				throws InvalidPatchworkAccessException {
			if (!(access instanceof PartAccessImpl<?>)) {
				throw new InvalidPatchworkAccessException("Part access is of incorrect class.");
			} else if (((PartAccessImpl<?>) access).factoryUuid != factoryUuid) {
				throw new InvalidPatchworkAccessException("Part access does not belong to the same patchwork factory.");
			}
			return (PartAccessImpl<T>) access;
		}

		@Override
		public Patchwork copy() {
			return new PatchworkImpl(Arrays.copyOf(partValues, partValues.length));
		}
	}
}
