package com.tibco.flogo.maven.coverage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource{
    public String id;
    public Data data;
}
