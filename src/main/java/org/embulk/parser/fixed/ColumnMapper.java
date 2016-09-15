package org.embulk.parser.fixed;

import org.embulk.spi.util.DynamicColumnSetter;

public class ColumnMapper
{
    DynamicColumnSetter setter;
    Extractor extractor;

    public ColumnMapper(DynamicColumnSetter setter, Extractor extractor)
    {
        this.setter = setter;
        this.extractor = extractor;
    }

    public DynamicColumnSetter getSetter()
    {
        return setter;
    }

    public Extractor getExtractor()
    {
        return extractor;
    }
}
