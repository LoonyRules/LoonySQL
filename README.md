# LoonySQL
I got bored once, made a quick, but bad SQL library. I disliked it and now I'm making this rewrite.

### Features
 * **Codecs**
   * StringCodec (`String.class`)
   * IntCodec (`int.class`)
   * IntegerCodec (`Integer.class`)
   * BooleanCodec (`boolean.class`)
   * DoubleCodec (`double.class`)
   * FloatCodec (`float.class`)
   * UUIDCodec (`UUID.class`)
 * **@Table**
   * Name
   * Create if not exists
   * Altering settings (None, Add, Remove, Add and Remove)
 * **@Column**
   * Custom column name
   * Default name to the Field name
 * **@Primary**
   * Non-AI support (AI support coming soon!)
 * **Tables**
   * Creation
 * **Queries**
   * SELECT (find, findFirst, reload)
   * DELETE (delete all rows, delete a specified row)
   * INSERT [...] ON DUPLICATE KEY [...] (save)
 * **Other**
   * Delete all table contents

### TODO
 * Table alterations, deleting
 * AutoIncrement @Primary support