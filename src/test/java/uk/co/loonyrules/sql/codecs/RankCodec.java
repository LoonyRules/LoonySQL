package uk.co.loonyrules.sql.codecs;

import uk.co.loonyrules.sql.codecs.types.EnumCodec;
import uk.co.loonyrules.sql.enums.Rank;

public class RankCodec extends EnumCodec<Rank>
{

    public RankCodec()
    {
        super(Rank.class);
    }

}