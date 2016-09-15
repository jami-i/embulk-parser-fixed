package org.embulk.parser.fixed;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ExtractorImplTest
{
    @Test(expected = FieldNotFoundException.class)
    public void extract_empty_line() throws Exception
    {
        Extractor extractor = new ExtractorImpl(0, 1);
        extractor.extract("");
        fail("illegal state");
    }

    @Test
    public void extract_empty_line_optional() throws Exception
    {
        Extractor extractor = new ExtractorImpl(0, 1, true, true);
        assertNull(extractor.extract(""));
    }

    @Test(expected = FieldNotFoundException.class)
    public void extract_line_too_short() throws Exception
    {
        Extractor extractor = new ExtractorImpl(3, 1);
        extractor.extract("AB");
        fail("illegal state");
    }

    @Test(expected = FieldNotFoundException.class)
    public void extract_column_length_too_short() throws Exception
    {
        Extractor extractor = new ExtractorImpl(3, 2);
        extractor.extract("ABC");
        fail("illegal state");
    }

    @Test(expected = FieldNotFoundException.class)
    public void extract_OK() throws Exception
    {
        Extractor extractor = new ExtractorImpl(3, 3);
        assertThat(extractor.extract("ABCDE"), is("CDE"));
        assertThat(extractor.extract("ABCDEF"), is("CDE"));
    }
}
