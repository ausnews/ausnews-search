// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

package org.ausnews.search;

import org.ausnews.search.view.SimpleHtmlBuilder;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class SimpleHtmlBuilderTest {

    @Test
    public void testHtmlBuilding() {
        SimpleHtmlBuilder html = new SimpleHtmlBuilder();
        html.element("body", Map.of("class", "main"), (level_1) -> {
            html.div("lvl1", (level_2) -> {
                html.text("Hello");
            });
            html.div("lvl1", (level_2) -> {
                html.a("https://vespa.ai", (level_3) -> {
                    html.text("vespa.ai");
                });
            });
        });
        String source = html.build();
        assertEquals("<body class=\"main\" >\n" +
                "<div class=\"lvl1\" >\n" +
                "Hello</div>\n" +
                "<div class=\"lvl1\" >\n" +
                "<a href=\"https://vespa.ai\" >\n" +
                "vespa.ai</a>\n" +
                "</div>\n" +
                "</body>\n", source);
    }

}