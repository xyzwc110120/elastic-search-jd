package priv.cyx.java.elasticsearch.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HtmlParseUtilTest {

    @Test
    void testParseJdSearchHtml() throws IOException {
        HtmlParseUtil.parseJdSearchHtml("oppo");
    }
}