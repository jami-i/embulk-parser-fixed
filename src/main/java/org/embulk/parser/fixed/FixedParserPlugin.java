package org.embulk.parser.fixed;

import com.google.common.collect.Lists;
import org.embulk.config.Config;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskSource;

import org.embulk.spi.ColumnConfig;
import org.embulk.spi.Exec;
import org.embulk.spi.FileInput;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.ParserPlugin;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;
import org.embulk.spi.time.TimestampFormatter;
import org.embulk.spi.time.TimestampParser;
import org.embulk.spi.util.LineDecoder;
import org.embulk.spi.util.Timestamps;

import java.util.List;

public class FixedParserPlugin
        implements ParserPlugin
{
    public interface PluginTask
            extends Task, LineDecoder.DecoderTask, TimestampParser.Task
    {
        // if you get schema from config or data source
        @Config("columns")
        public SchemaConfig getColumns();
    }

    public interface PluginTaskFormatter
            extends Task, TimestampFormatter.Task
    {
    }

    public interface TimestampColumnOption
            extends Task, TimestampFormatter.TimestampColumnOption
    {
    }

    @Override
    public void transaction(ConfigSource config, ParserPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        Schema schema = task.getColumns().toSchema();

        control.run(task.dump(), schema);
    }

    @Override
    public void run(TaskSource taskSource, Schema schema,
            FileInput input, PageOutput output)
    {
        List<ColumnMapper> hoges = Lists.newArrayList();
        PluginTask task = taskSource.loadTask(PluginTask.class);
        LineDecoder lineDecoder = new LineDecoder(input, task);
        PageBuilder pageBuilder = new PageBuilder(Exec.getBufferAllocator(), schema, output);
        TimestampParser[] timestampParsers = Timestamps.newTimestampColumnParsers(task, task.getColumns());

        ColumnMapperFactory factory = new ColumnMapperFactoryImpl(pageBuilder, timestampParsers, taskSource.loadTask(PluginTaskFormatter.class));

        int index = -1;
        for (ColumnConfig columnConfig : task.getColumns().getColumns()) {
            index++;
            hoges.add(factory.create(columnConfig, index));
        }

        while (input.nextFile()) {
            while (true) {
                String line = lineDecoder.poll();

                if (line == null) {
                    break;
                }

                for (ColumnMapper hoge : hoges) {
                    String v = hoge.getExtractor().extract(line);
                    if (v == null) {
                        hoge.getSetter().setNull();
                    }
                    else {
                        hoge.getSetter().set(v);
                    }
                }

                pageBuilder.addRecord();
            }
        }

        pageBuilder.finish();
    }
}
