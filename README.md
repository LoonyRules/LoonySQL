# LoonySQL
I got bored once, made a quick, but bad SQL library. I disliked it and now I'm making this rewrite.

### Features
 * **Codecs**
   * StringCodec (`String.class`)
   * IntCodec (`int.class`)
   * BooleanCodec (`boolean.class`)
   * DoubleCodec (`double.class`)
   * FloatCodec (`float.class`)
   * UUIDCodec (`UUID.class`  )
 * **@Table**
   * Name
   * Create if not exists
   * Altering settings (None, Add, Remove, Add and Remove)
 * **@Column**
   * Custom column name
   * Default name to the Field name
 * **@Primary**
   * (Non) AutoIncrement support

### TODO
 * Table creation, alterations, deleting
 * Primary key support
 * Insert, Update, Select, Delete