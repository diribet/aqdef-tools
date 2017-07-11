package cz.diribet.aqdef.model;

/**
 * @author Vlastimil Dolejs
 *
 */
public class NodeIndex implements Comparable<NodeIndex> {
	private final Integer index;

	private NodeIndex(Integer index) {
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}

	public static NodeIndex of(Integer index) {
		return new NodeIndex(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((index == null) ? 0 : index.hashCode());
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
		if (!(obj instanceof NodeIndex)) {
			return false;
		}
		NodeIndex other = (NodeIndex) obj;
		if (index == null) {
			if (other.index != null) {
				return false;
			}
		} else if (!index.equals(other.index)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (index == null) {
			return "";
		} else {
			return index.toString();
		}
	}

	@Override
	public int compareTo(NodeIndex o) {
        if(this == o || this.index == o.index) { return 0; }
        if(this.index == null) { return -1; }
        if(o.index == null) { return 1; }

        return this.index.compareTo(o.index);
	}
}