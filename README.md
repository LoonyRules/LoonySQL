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
        <version>1.3-SNAPSHOT</version>

        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Download (Don't use maven? You can just download the jar!)
[LoonyRules Repo](http://repo.loonyrules.co.uk/#browse/browse/components:maven-snapshots:36e3dec8de528c9bd2bd752a2cf45891)

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
 * **@Table**  
   * Name
   * Create if not exists
   * Altering settings (None, Add, Remove, Add and Remove)
 * **@Column**
   * Custom column name
   * Default name to the Field name
 * **@Primary**
   * Non-AutoIncrement support (AutoIncrement support coming soon!)
 * **Tables**
   * Creation
   * Modification to structure (Adding/Removing columns)
 * **Queries**
   * SELECT (find, findFirst, reload)
   * DELETE (delete all rows, delete a specified row)
   * INSERT [...] ON DUPLICATE KEY [...] (save)
   * EXPLAIN / DESCRIBE
 * **Other**
   * Delete all table contents

### TODO
 * Unknown