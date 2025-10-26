package de.siphalor.tweed5.data.hjson;

import lombok.*;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
class HjsonReadPosition {
	private int line = 1;
	private int index;

	public void nextCodepoint() {
		index++;
	}

	public void nextLine() {
		line++;
		index = 0;
	}

	public HjsonReadPosition copy() {
		return new HjsonReadPosition(line, index);
	}

	@Override
	public String toString() {
		return line + ":" + index;
	}
}
