package org.mayocat.configuration;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MultitenancySettings
{
    @Valid
    @JsonProperty
    private boolean activated = false;

    @Valid
    @JsonProperty
    private String rootDomain = null;
    
    @Valid
    @JsonProperty
    private String defaultTenant = "default";

    @Valid
    @JsonProperty
    private String resolver = "subdomain";
    
    public boolean isActivated()
    {
        return activated;
    }

    public String getRootDomain()
    {
        return rootDomain;
    }

    public String getDefaultTenantSlug()
    {
        return defaultTenant;
    }

    public String getResolver()
    {
        return resolver;
    }
}
