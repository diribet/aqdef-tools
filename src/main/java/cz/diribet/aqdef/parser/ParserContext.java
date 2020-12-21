package cz.diribet.aqdef.parser;

import cz.diribet.aqdef.KKey;
import cz.diribet.aqdef.model.CharacteristicIndex;
import cz.diribet.aqdef.model.PartIndex;
import cz.diribet.aqdef.model.ValueIndex;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Vlastimil Dolejs
 */
@Getter
@Setter
public class ParserContext {

    private int currentLine;
    private PartIndex currentPartIndex;
    private final ValueIndexCounter valueIndexCounter = new ValueIndexCounter();

    public static String lineLogContext(ParserContext context) {
        return "Line " + context.getCurrentLine() + ":";
    }

    /**
     * @author Vlastimil Dolejs
     */
    public static class ValueIndexCounter {

        private final Map<CharacteristicIndex, Set<KKey>> keys = new HashMap<>();
        private final Map<CharacteristicIndex, Integer> indexes = new HashMap<>();

        public ValueIndex getIndex(CharacteristicIndex characteristicIndex, KKey key) {
            Set<KKey> keysOfCurrentValue = keys.computeIfAbsent(characteristicIndex, k -> new HashSet<>());

            Integer currentIndex = indexes.get(characteristicIndex);
            currentIndex = currentIndex == null ? 1 : currentIndex;

            if (keysOfCurrentValue.contains(key)) {
                // this key was already parsed, so it is new value
                keysOfCurrentValue.clear();
                currentIndex++;
                indexes.put(characteristicIndex, currentIndex);
            }
            keysOfCurrentValue.add(key);

            return ValueIndex.of(characteristicIndex, currentIndex);
        }
    }

}
