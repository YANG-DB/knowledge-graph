package org.opensearch.graph.datagen.utilities;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class LogGeneratorTest  {
    public final String EXTENDED_APACHE_LOG_REGEX = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+-]\\d{4})\\] \"(.+?)\" " +
            "(\\d{3}) (\\d+) \"([^\"]+)\" \"(.+?)\"";

    public final String COMMON_APACHE_LOG_REGEX = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+-]\\d{4})\\] \"(.+?)\" " +
            "(\\d{3}) (\\d+)";

    @Test
    public void testRandomExtendedLogPattern() {
        // Given
        LogGenerator objectUnderTest = new LogGenerator();

        // When/Then
        assertTrue(objectUnderTest.generateRandomExtendedApacheLog().matches(EXTENDED_APACHE_LOG_REGEX));
    }

    @Test
    public void testRandomCommonLogPattern() {
        // Given
        LogGenerator objectUnderTest = new LogGenerator();

        // When/Then
        assertTrue(objectUnderTest.generateRandomCommonApacheLog().matches(COMMON_APACHE_LOG_REGEX));
    }
}