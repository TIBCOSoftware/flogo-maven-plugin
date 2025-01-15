package com.tibco.flogo.maven.coverage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Task{
    public String id;
    public String name;
    public String description;
}
