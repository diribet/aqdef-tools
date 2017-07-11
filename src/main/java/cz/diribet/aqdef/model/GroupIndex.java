package cz.diribet.aqdef.model;

/**
 * @author Vlastimil Dolejs
 *
 */
public class GroupIndex implements Comparable<GroupIndex> {
	private final PartIndex partIndex;
	private final Integer groupIndex;

	private GroupIndex(PartIndex partIndex, Integer groupIndex) {
		this.partIndex = partIndex;
		this.groupIndex = groupIndex;
	}

	public PartIndex getPartIndex() {
		return partIndex;
	}

	public Integer getGroupIndex() {
		return groupIndex;
	}

	public static GroupIndex of(PartIndex partIndex, Integer groupIndex) {
		return new GroupIndex(partIndex, groupIndex);
	}

	public static GroupIndex of(Integer partIndex, Integer groupIndex) {
		return of(PartIndex.of(partIndex), groupIndex);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((partIndex == null) ? 0 : partIndex.hashCode());
		result = prime * result + ((groupIndex == null) ? 0 : groupIndex.hashCode());
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
		if (!(obj instanceof GroupIndex)) {
			return false;
		}
		GroupIndex other = (GroupIndex) obj;
		if (partIndex == null) {
			if (other.partIndex != null) {
				return false;
			}
		} else if (!partIndex.equals(other.partIndex)) {
			return false;
		}
		if (groupIndex == null) {
			if (other.groupIndex != null) {
				return false;
			}
		} else if (!groupIndex.equals(other.groupIndex)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(GroupIndex o) {
        if(this == o) { return 0; }

		int compareResultPart = this.partIndex.compareTo(o.partIndex);

		if (compareResultPart != 0) {
			return compareResultPart;
		} else {
	        if(this.groupIndex == o.groupIndex) { return 0; }
	        if(this.groupIndex == null) { return -1; }
	        if(o.groupIndex == null) { return 1; }

	        return this.groupIndex.compareTo(o.groupIndex);
		}
	}

	@Override
	public String toString() {
		String result = "";
		if (partIndex != null) {
			result += partIndex.toString();
		}

		result += "/";

		if (groupIndex != null) {
			result += groupIndex;
		}

		return result;
	}

}