package de.siphalor.tweed5.annotationinheritance.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@RepeatableAnnotationContainerHelperTest.R(1)
@RepeatableAnnotationContainerHelperTest.R(2)
@RepeatableAnnotationContainerHelperTest.R(3)
class RepeatableAnnotationContainerHelperTest {

	@Test
	@Disabled("Dumping the class is only for debugging purposes")
	@SneakyThrows
	void dumpClass() {
		byte[] bytes = RepeatableAnnotationContainerHelper.createContainerClassBytes(
				"de.siphalor.tweed5.annotationinheritance.impl.generated.DumpIt",
				Rs.class,
				R.class
		);
		Path path = Path.of(getClass().getSimpleName() + ".class");
		Files.write(path, bytes);
		System.out.println("Dumped to " + path.toAbsolutePath());
	}

	@Test
	void test() {
		R[] elements = {new RImpl(1), new RImpl(2), new RImpl(3)};
		Annotation result = RepeatableAnnotationContainerHelper.createContainer(elements);
		assertThat(result).asInstanceOf(type(Rs.class))
				.extracting(Rs::value)
				.isEqualTo(elements);
		assertThat(result.annotationType()).isEqualTo(Rs.class);

		assertThat(result.toString()).containsSubsequence("Rs(value=", "1", "2", "3");

		Rs ref = RepeatableAnnotationContainerHelperTest.class.getAnnotation(Rs.class);
		assertThat(result.equals(ref)).isTrue();
		assertThat(result.hashCode()).isEqualTo(ref.hashCode());
	}

	@RequiredArgsConstructor
	@Getter
	public static class RImpl implements R {
		private final int value;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (obj instanceof R) {
				return ((R) obj).value() == this.value();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (127 * "value".hashCode()) ^ Integer.valueOf(value).hashCode();
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return R.class;
		}
	}

	@Repeatable(Rs.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface R {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Rs {
		R[] value();
	}
}
