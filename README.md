# LoonySQL
I got bored once, made a quick, but bad SQL library. I disliked it and now I'm making this rewrite.

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
 * **Queries**
   * SELECT (find, findFirst, reload)
   * DELETE (delete all rows, delete a specified row)
   * INSERT [...] ON DUPLICATE KEY [...] (save)
 * **Other**
   * Delete all table contents

### TODO
 * AutoIncrement @Primary support (including Table alterations)