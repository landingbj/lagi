package ai.servlet.api;

import ai.llm.pojo.TokenStatisticsDetail;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenStatisticsApiServletTest {

    @Test
    void appliesResolvedNamesAndKeepsUserIdsAsTheFallback() {
        TokenStatisticsDetail named = TokenStatisticsDetail.builder().userId("usr_named").build();
        TokenStatisticsDetail unresolved = TokenStatisticsDetail.builder().userId("usr_unresolved").build();
        TokenStatisticsDetail blankName = TokenStatisticsDetail.builder().userId("usr_blank").build();
        TokenStatisticsDetail anonymous = TokenStatisticsDetail.builder().build();
        List<TokenStatisticsDetail> records = Arrays.asList(named, unresolved, blankName, anonymous);
        Map<String, String> displayNames = new HashMap<String, String>();
        displayNames.put("usr_named", "当前姓名");
        displayNames.put("usr_blank", "   ");

        TokenStatisticsApiServlet.applyUserDisplayNames(records, displayNames);

        assertEquals("当前姓名", named.getUserDisplayName());
        assertEquals("usr_unresolved", unresolved.getUserDisplayName());
        assertEquals("usr_blank", blankName.getUserDisplayName());
        assertNull(anonymous.getUserDisplayName());

        TokenStatisticsApiServlet.applyUserDisplayNames(records, Collections.<String, String>emptyMap());
        assertEquals("usr_named", named.getUserDisplayName());
    }
}
