# LoonySQL
I got bored once, made a quick, but bad SQL library. I disliked it and now I'm making this rewrite.

### Maven
```
<repositories>
    <repository>
        <id>loonyrules-repo</id>
        <url>http://repo.loonyrules.co.uk/repository/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>uk.co.loonyrules</groupId>
        <artifactId>sql</artifactId>
        <version>1.6.6-SNAPSHOT</version>

        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Download (Don't use maven? You can just download the jar!)
[LoonyRules Repo](http://repo.loonyrules.co.uk/#browse/browse/components:maven-snapshots:4b378653591c67229a851cdf6b8c670a)

### Features
 * **Codecs**
   * StringCodec (`String.class`)
   * IntegerCodec (`int.class`, `Integer.class`)
   * BooleanCodec (`boolean.class`, `Boolean.class`)
   * DoubleCodec (`double.class`. `Double.class`)
   * FloatCodec (`float.class`, `Float.class`)
   * UUIDCodec (`UUID.class`)
   * LongCodec (`long.class`, `Long.class`)
   * EnumCodec (`Enum.class`) See [RankCodec](https://github.com/LoonyRules/LoonySQL/blob/master/src/test/java/uk/co/loonyrules/sql/codecs/RankCodec.java) for usage example.
   * ListCodec (`List.class`, `ArrayList.class`) **Currently only supports String as a Generic Type.**
 * **@Table**  
   * Name
   * Create if not exists
   * Altering settings (None, Add, Remove, Add and Remove)
 * **@Column**
   * Custom column name
   * Default name to the Field name
 * **@Primary**
   * Non-AutoIncrement support
   * Integer AutoIncrement support
 * **Tables**
   * Creation
   * Modification to structure (Adding/Removing columns)
 * **Queries**
   * SELECT (find, findFirst, reload)
   * DELETE (delete all rows, delete a specified row)
   * INSERT [...] ON DUPLICATE KEY [...] (save)
   * EXPLAIN / DESCRIBE
   * COUNT (the number of rows matching your Query)
 * **Other**
   * Delete all table contents
   * Configuration support
     * Maximum Pool Size
     * Encoding
     * Collation
   * Unicode support

### TODO
 * Support an `@Embeddable` system that'll pull data from more than 1 Table from inside of an Object.