package uk.co.loonyrules.sql.storage;

import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

public class CaseInsensitiveMap<V> extends TCustomHashMap<String, V>
{

    public CaseInsensitiveMap()
    {
        super(new HashingStrategy<String>()
        {
            @Override
            public int computeHashCode(String s)
            {
                return s.toLowerCase().hashCode();
            }

            @Override
            public boolean equals(String s, String t1)
            {
                return s.equalsIgnoreCase(t1);
            }

        });
    }

}