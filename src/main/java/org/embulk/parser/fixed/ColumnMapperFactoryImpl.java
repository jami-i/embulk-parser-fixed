package org.embulk.parser.fixed;

import com.google.common.base.Optional;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnConfig;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.time.TimestampFormatter;
import org.embulk.spi.time.TimestampParser;
import org.embulk.spi.type.BooleanType;
import org.embulk.spi.type.DoubleType;
import org.embulk.spi.type.LongType;
import org.embulk.spi.type.StringType;
import org.embulk.spi.type.TimestampType;
import org.embulk.spi.type.Type;
import org.embulk.spi.util.DynamicColumnSetter;
import org.embulk.spi.util.dynamic.BooleanColumnSetter;
import org.embulk.spi.util.dynamic.DefaultValueSetter;
import org.embulk.spi.util.dynamic.DoubleColumnSetter;
import org.embulk.spi.util.dynamic.LongColumnSetter;
import org.embulk.spi.util.dynamic.NullDefaultValueSetter;
import org.embulk.spi.util.dynamic.StringColumnSetter;
import org.embulk.spi.util.dynamic.TimestampColumnSetter;

public class ColumnMapperFactoryImpl implements ColumnMapperFactory
{
    private PageBuilder pageBuilder;

    private TimestampParser[] timestampParsers;
    private TimestampFormatter.Task formatterTask;

    public ColumnMapperFactoryImpl(PageBuilder pageBuilder, TimestampParser[] timestampParsers, TimestampFormatter.Task formatterTask)
    {
        this.pageBuilder = pageBuilder;
        this.timestampParsers = timestampParsers;
        this.formatterTask = formatterTask;
    }

    @Override
    public ColumnMapper create(ColumnConfig config, int columnIndex)
    {
        DynamicColumnSetter setter = getDynamicColumnSetter(config, columnIndex);
        Extractor extractor = getExtractor(config);

        return new ColumnMapper(setter, extractor);
    }

    private DynamicColumnSetter getDynamicColumnSetter(ColumnConfig config, int columnIndex)
    {
        Column column = config.toColumn(columnIndex);

        Type type = config.getType();
        DefaultValueSetter defaultValue = new NullDefaultValueSetter();
        DynamicColumnSetter setter;
        if (type instanceof BooleanType) {
            setter = new BooleanColumnSetter(pageBuilder, column, defaultValue);
        }
        else if (type instanceof LongType) {
            setter = new LongColumnSetter(pageBuilder, column, defaultValue);
        }
        else if (type instanceof DoubleType) {
            setter = new DoubleColumnSetter(pageBuilder, column, defaultValue);
        }
        else if (type instanceof StringType) {
            TimestampFormatter formatter = new TimestampFormatter(formatterTask,
                    Optional.of(config.getOption().loadConfig(FixedParserPlugin.TimestampColumnOption.class)));
            setter = new StringColumnSetter(pageBuilder, column, defaultValue, formatter);
        }
        else if (type instanceof TimestampType) {
            // TODO use flexible time format like Ruby's Time.parse
            TimestampParser parser = timestampParsers[column.getIndex()];
            setter = new TimestampColumnSetter(pageBuilder, column, defaultValue, parser);
        }
        else {
            throw new ConfigException("Unknown column type: " + type);
        }
        return setter;
    }

    private Extractor getExtractor(ColumnConfig config)
    {
        ConfigSource option = config.getOption();
        Integer start = option.get(Integer.class, "start");
        Integer length = option.get(Integer.class, "length");
        boolean optional = option.get(Boolean.class, "optional", false);
        boolean trim = option.get(Boolean.class, "trim", true);

        return new ExtractorImpl(start, length, optional, trim);
    }
}
