package org.eclipse.sensinact.gateway.nthbnd.test.jsonpath;

import org.junit.Test;

import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.Predicate.PredicateContext;
import com.jayway.jsonpath.ReadContext;

import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.using;
import static org.assertj.core.api.Assertions.assertThat;

public class PredicateTest extends BaseTestConfiguration {

    private static ReadContext reader = using(JSON_ORG_CONFIGURATION).parse(BaseTestJson.JSON_DOCUMENT, false);

    @Test
    public void predicates_filters_can_be_applied() {
        Predicate booksWithISBN = new Predicate() {
            @Override
            public boolean apply(PredicateContext ctx) {
                return ctx.item(Map.class).containsKey("isbn");
            }
        };

        assertThat(reader.read("$.store.book[?].isbn", List.class, booksWithISBN)).containsOnly("0-395-19395-8", "0-553-21311-3");
    }
}
