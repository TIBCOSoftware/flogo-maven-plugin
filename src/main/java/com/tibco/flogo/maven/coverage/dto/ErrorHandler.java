package com.tibco.flogo.maven.coverage.dto; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorHandler{
    public List<Task> tasks;
    public List<Link> links;
}
