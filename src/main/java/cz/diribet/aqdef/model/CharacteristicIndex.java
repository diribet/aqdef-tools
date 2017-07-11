package cz.diribet.aqdef.model;

/**
 * @author Vlastimil Dolejs
 *
 */
public class CharacteristicIndex implements Comparable<CharacteristicIndex> {
	private final PartIndex partIndex;
	private final Integer characteristicIndex;

	private CharacteristicIndex(PartIndex partIndex, Integer characteristicIndex) {
		this.partIndex = partIndex;
		this.characteristicIndex = characteristicIndex;
	}

	public PartIndex getPartIndex() {
		return partIndex;
	}

	public Integer getCharacteristicIndex() {
		return characteristicIndex;
	}

	public boolean platiProVsechnyZnakyDilu() {
		return Integer.valueOf(0).equals(characteristicIndex);
	}

	public static CharacteristicIndex of(PartIndex partIndex, Integer characteristicIndex) {
		return new CharacteristicIndex(partIndex, characteristicIndex);
	}

	public static CharacteristicIndex of(Integer partIndex, Integer characteristicIndex) {
		return of(PartIndex.of(partIndex), characteristicIndex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
		result = prime * result + ((characteristicIndex == null) ? 0 : characteristicIndex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CharacteristicIndex)) {
			return false;
		}
		CharacteristicIndex other = (CharacteristicIndex) obj;
		if (partIndex == null) {
			if (other.partIndex != null) {
				return false;
			}
		} else if (!partIndex.equals(other.partIndex)) {
			return false;
		}
		if (characteristicIndex == null) {
			if (other.characteristicIndex != null) {
				return false;
			}
		} else if (!characteristicIndex.equals(other.characteristicIndex)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(CharacteristicIndex o) {
        if(this == o) { return 0; }

		int compareResultPart = this.partIndex.compareTo(o.partIndex);

		if (compareResultPart != 0) {
			return compareResultPart;
		} else {
	        if(this.characteristicIndex == o.characteristicIndex) { return 0; }
	        if(this.characteristicIndex == null) { return -1; }
	        if(o.characteristicIndex == null) { return 1; }

	        return this.characteristicIndex.compareTo(o.characteristicIndex);
		}
	}

	@Override
	public String toString() {
		String result = "";
		if (partIndex != null) {
			result += partIndex.toString();
		}

		result += "/";

		if (characteristicIndex != null) {
			result += characteristicIndex;
		}

		return result;
	}

}