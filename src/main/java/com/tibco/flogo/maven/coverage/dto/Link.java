package com.tibco.flogo.maven.coverage.dto; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Link{
    public int id;
    public String from;
    @JsonProperty("to") 
    public String myto;
    public String type;
    public String label;
}
