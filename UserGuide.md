# AQDEF tools User Guide

1. [Using AQDEF tools with Maven](#using-aqdef-tools-with-maven)
2. [Reading AQDEF content](#reading-aqdef-content)
3. [Writing AQDEF content](#writing-aqdef-content)
4. [Creating AQDEF content](#creating-aqdef-content)
5. [Manipulating AQDEF content](#manipulating-aqdef-content)
    * [getXXX](#get)
    * [putXXX](#put)
    * [filterXXX](#filter)
    * [forEachXXX](#for-each)

### Using AQDEF tools with Maven
To use AQDEF tools with Maven2/3, you can use the library version available in Maven Central by adding the following dependency:

```xml
<dependencies>
    <dependency>
        <groupId>cz.diribet</groupId>
        <artifactId>aqdef-tools</artifactId>
        <version>1.0.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Reading AQDEF content
You can use `AqdefParser` to read the AQDEF content. The content will be usually provided as a .DFQ file or as a String in some API endpoint.

```java
AqdefParser parser = new AqdefParser();
AqdefObjectModel objectModel = parser.parse(Paths.get("path_to_dfq"), "utf-8");
objectModel.forEachValue({ part, characteristic, value ->
	// do some stuff 
});
```

### Writing AQDEF content
You can use `AqdefWriter` to write AQDEF content (`AqdefObjectModel`) to its textual representation. The given AQDEF Object Model could be newly created content from `AqdefObjectModelBuilder` or content read using `AqdefParser`.

```java
AqdefObjectModel objectModel = ...;
AqdefWriter writer = new AqdefWriter();

// get AQDEF content as a String
String aqdefContent = writer.writeToString(objectModel);

// write to file	
try (Writer writer = Files.newBufferedWriter(Paths.get("path_to_dfq"), StandardCharsets.UTF_8)) {
	new AqdefWriter().writeTo(objectModel, writer);
}
```

### Creating AQDEF content
If you want to create AQDEF content, for example from your domain objects, you can do it using `AqdefObjectModelBuilder`.

```java
AqdefObjectModelBuilder builder = new AqdefObjectModelBuilder();

builder.createPartEntry("K1001", "part number");
builder.createPartEntry("K1002", "part title");

for (MyCharacteristic characteristic : myCharacteristics) {
	builder.createCharacteristicEntry("K2001", characteristic.getCode());

	for (MyValue value : characteristic.getValues()) {
		builder.createValueEntry("K0001", value.getMeasurement());
		builder.createValueEntry("K0004", value.getMeasurementDate());
		builder.nextValue();
	}

	builder.nextCharacteristic();
}

AqdefObjectModel objectModel = builder.build();
```

It's important to call `nextPart()`, `nextCharacteristic()` and `nextValue()` before new entity is written! 

It's also important that the values of the K-keys has to be of correct data type. You can find data types of K-keys in `KKeyRepository` class or in documentation of AQDEF format. Mapping between data types in documentation and Java data types is following:

| AQDEF type | Java type      |
| ---------- | -------------- |
| A          | String         |
| D          | java.util.Date |
| F          | BigDecimal     |
| I3, I5, I10| Integer        |
| S          | misc.          |

### Manipulating AQDEF content
There are some useful methods to manipulate with `AqdefObjectModel`. Each of these methods has variants for part / characteristic / value entries. 

* [getXXX](#get)
* [putXXX](#put)
* [filterXXX](#filter)
* [forEachXXX](#for-each)

#### Get
This method is useful when you want to get values of K-keys of the specific entry.

For example if you want to get value of K1001 from the first part:
```java
AqdefObjectModel objectModel = ...;
PartEntries part = objectModel.getPartEntries(1);
String partNumber = part.getValue("K1001");
```

#### Put
This method will allow you to put new K-key value or to replace the existing one.

For example if you want to add K2142 (unit) to a single characteristic:
```java
AqdefObjectModel objectModel = ...;
CharacteristicIndex characteristicIndex = CharacteristicIndex.of(1, 2); // second characteristic of first part
objectModel.putCharacteristicEntry("K2142", characteristicIndex, "mm");
```

#### Filter
This method will allow you to remove parts / characteristics / values from object model based on the result of the given predicate. Filtering affects whole structure e.g. when you remove a part it will also remove all characteristics and values of this part.

Filter method works similar to Java Stream `filter()` method. Entries that match the predicate (predicate returns true) will be retained in the object model.

For example is you want to remove all characteristics named "*temperature*":
```java
AqdefObjectModel objectModel = ...;
objectModel.filterCharacteristics(part, characteristic -> {
	String name = characteristic.getValue("K2002");
	return !name.contains("temperature");
});
```

#### For Each
This method will allow you to easily iterate through part / characteristic / value entries.

For example if you want to print all values with their specification limits for each part:
```java
AqdefObjectModel objectModel = ...;
objectModel.forEachPart(part -> {
	System.out.println("Part " + part.getValue("K1001"));
	
	objectModel.forEachCharacteristic(part, (characteristic, value) -> {
		BigDecimal lsl = characteristic.getValue("K2110");
		BigDecimal usl = characteristic.getValue("K2111");
		BigDecimal measuredValue = value.getValue("K0001");
		
		System.out.println("Value: " + measuredValue + " (" + lsl + " - " + usl + ")");
	});
});
```