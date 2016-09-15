package org.embulk.parser.fixed;

import org.embulk.spi.ColumnConfig;

public interface ColumnMapperFactory
{
    ColumnMapper create(ColumnConfig config, int columnIndex);
}
