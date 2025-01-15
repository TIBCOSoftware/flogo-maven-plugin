package com.tibco.flogo.maven.coverage.dto; 
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class App {
    public ArrayList<String> imports;
    public String name;
    public String description;
    public String version;
    public String type;
    public String appModel;
    public List<Resource> resources;
    public String contrib;
}
