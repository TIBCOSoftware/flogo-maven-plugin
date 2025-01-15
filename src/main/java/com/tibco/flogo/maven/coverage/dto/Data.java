package com.tibco.flogo.maven.coverage.dto; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data{
    public String name;
    public String description;
    public List<Link> links;
    public List<Task> tasks;
    public ErrorHandler errorHandler;

}
