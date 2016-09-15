package org.embulk.parser.fixed;

public class FieldNotFoundException extends RuntimeException
{
    private final String line;
    private final int start;
    private final int length;

    public FieldNotFoundException(String line, int start, int length)
    {
        this.line = line;
        this.start = start;
        this.length = length;
    }

    @Override
    public String getMessage()
    {
        return "Field(" + start + "-" + (start + length) + ") not found\n" + line + "(length: " + line.length() + ")";
    }
}
