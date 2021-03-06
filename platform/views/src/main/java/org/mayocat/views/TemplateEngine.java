package org.mayocat.views;

import org.xwiki.component.annotation.Role;

/**
 * @version $Id$
 */
@Role
public interface TemplateEngine
{
    void register(Template template);

    String render(String templateName, String json);
}
