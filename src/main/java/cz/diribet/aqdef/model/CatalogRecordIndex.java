package cz.diribet.aqdef.model;

import lombok.Data;

/**
 * @author Vlastimil Dolejs
 *
 */
@Data
public class CatalogRecordIndex implements Comparable<CatalogRecordIndex> {
	private final Integer index;

	public static CatalogRecordIndex of(Integer index) {
		return new CatalogRecordIndex(index);
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
	public int compareTo(CatalogRecordIndex o) {
        if(this == o || this.index == o.index) { return 0; }
        if(this.index == null) { return -1; }
        if(o.index == null) { return 1; }

        return this.index.compareTo(o.index);
	}
}