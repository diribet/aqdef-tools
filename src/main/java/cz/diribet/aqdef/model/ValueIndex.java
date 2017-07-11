package cz.diribet.aqdef.model;

/**
 * @author Vlastimil Dolejs
 *
 */
public class ValueIndex implements Comparable<ValueIndex> {
	private final PartIndex partIndex;
	private final CharacteristicIndex characteristicIndex;
	private final Integer valueIndex;

	private ValueIndex(PartIndex partIndex, CharacteristicIndex characteristicIndex, Integer valueIndex) {
		this.partIndex = partIndex;
		this.characteristicIndex = characteristicIndex;
		this.valueIndex = valueIndex;
	}

	public PartIndex getPartIndex() {
		return partIndex;
	}

	public CharacteristicIndex getCharacteristicIndex() {
		return characteristicIndex;
	}

	public Integer getValueIndex() {
		return valueIndex;
	}

	public boolean platiProVsechnyHodnotyDilu() {
		return Integer.valueOf(0).equals(characteristicIndex.getCharacteristicIndex());
	}

	public static ValueIndex of(CharacteristicIndex characteristicIndex, Integer valueIndex) {
		return new ValueIndex(characteristicIndex.getPartIndex(), characteristicIndex, valueIndex);
	}

	public static ValueIndex of(PartIndex partIndex, CharacteristicIndex characteristicIndex, Integer valueIndex) {
		return new ValueIndex(partIndex, characteristicIndex, valueIndex);
	}

	public static ValueIndex of(Integer partIndex, Integer characteristicIndex, Integer valueIndex) {
		return new ValueIndex(PartIndex.of(partIndex), CharacteristicIndex.of(partIndex, characteristicIndex) , valueIndex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((characteristicIndex == null) ? 0 : characteristicIndex.hashCode());
		result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
		result = prime * result + ((valueIndex == null) ? 0 : valueIndex.hashCode());
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
		if (!(obj instanceof ValueIndex)) {
			return false;
		}
		ValueIndex other = (ValueIndex) obj;
		if (characteristicIndex == null) {
			if (other.characteristicIndex != null) {
				return false;
			}
		} else if (!characteristicIndex.equals(other.characteristicIndex)) {
			return false;
		}
		if (partIndex == null) {
			if (other.partIndex != null) {
				return false;
			}
		} else if (!partIndex.equals(other.partIndex)) {
			return false;
		}
		if (valueIndex == null) {
			if (other.valueIndex != null) {
				return false;
			}
		} else if (!valueIndex.equals(other.valueIndex)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ValueIndex o) {
        if(this == o) { return 0; }

		int compareResultPart = this.partIndex.compareTo(o.partIndex);

		if (compareResultPart != 0) {
			return compareResultPart;
		} else {
			int compareResultCharacteristic = this.characteristicIndex.compareTo(o.characteristicIndex);

			if (compareResultCharacteristic != 0) {
				return compareResultCharacteristic;
			} else {
		        if(this.valueIndex == o.valueIndex) { return 0; }
		        if(this.valueIndex == null) { return -1; }
		        if(o.valueIndex == null) { return 1; }

		        return this.valueIndex.compareTo(o.valueIndex);
			}
		}
	}

	@Override
	public String toString() {
		String result = "";
		if (characteristicIndex != null) {
			result += characteristicIndex.toString();
		}

		result += "/";

		if (valueIndex != null) {
			result += valueIndex;
		}

		return result;
	}

}