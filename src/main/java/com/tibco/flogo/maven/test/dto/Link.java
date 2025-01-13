package com.tibco.flogo.maven.test.dto; 
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Link{
    public String linkName;
    public String from;
    @JsonProperty("to") 
    public String myto;
}
