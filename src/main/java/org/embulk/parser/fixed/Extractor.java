package org.embulk.parser.fixed;

public interface Extractor
{
    String extract(String line) throws FieldNotFoundException;
}
