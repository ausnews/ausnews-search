// Copyright 2019 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

package org.ausnews.search.view;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public class HomeRenderer {

    public static SimpleTemplate render(Map<String, JsonNode> data, Map<String, String> properties) {
        SimpleTemplate template = new SimpleTemplate("home.html.template");
        template.set("page-title", "Home");
        return template;
    }

}
