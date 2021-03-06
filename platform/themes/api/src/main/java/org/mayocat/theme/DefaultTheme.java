package org.mayocat.theme;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;
import org.mayocat.addons.model.AddonDefinition;
import org.mayocat.configuration.thumbnails.Dimensions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;

/**
 * @version $Id$
 */
public class DefaultTheme implements Theme
{
    @Valid
    @NotBlank
    @JsonProperty
    private String name;

    @Valid
    @JsonProperty
    private String description = "";

    @Valid
    @JsonProperty
    private Map<String, Dimensions> thumbnails = Maps.newHashMap();

    @Valid
    @JsonProperty
    private List<Model> models = Collections.emptyList();

    @Valid
    @JsonProperty
    private List<AddonDefinition> addons = Collections.emptyList();

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public Map<String, Dimensions> getThumbnails()
    {
        return thumbnails;
    }

    @Override
    public List<Model> getModels()
    {
        return models;
    }

    @Override
    public List<AddonDefinition> getAddons()
    {
        return this.addons;
    }
}
