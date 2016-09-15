package org.embulk.parser.fixed;

public class ExtractorImpl implements Extractor
{
    private boolean optional = false;

    private boolean trim = true;

    private final int start;

    private final int length;

    public ExtractorImpl(int start, int length)
    {
        this.start = start;
        this.length = length;
    }

    public ExtractorImpl(int start, int length, boolean optional, boolean trim)
    {
        this.start = start;
        this.length = length;
        this.optional = optional;
        this.trim = trim;
    }

    @Override
    public String extract(String line) throws FieldNotFoundException
    {
        int lineLength = line.length();

        if (lineLength < start || lineLength < (start + length - 1)) {
            if (optional) {
                return null;
            }
            else {
                throw new FieldNotFoundException(line, start, length);
            }
        }

        int startIndex = start - 1;
        int endIndex = startIndex + length;

        String value = line.substring(startIndex, endIndex);

        return trim ? value.trim() : value;
    }
}
