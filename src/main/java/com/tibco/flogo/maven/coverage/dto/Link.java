package com.tibco.flogo.maven.coverage.dto; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link{
    public int id;
    public String from;
    @JsonProperty("to") 
    public String myto;
    public String type;
    public String label;
}
