# Plug-in Code for Apache Maven and TIBCO Flogo® Enterprise

This plug-in is subject to the license shared as part of the repository. Kindly review the license before using or downloading this plug-in.

It is provided as a sample plug-in to support use-cases of integrating TIBCO Flogo® Enterprise with Apache Maven.

## Prerequisites

1. Maven should be installed on the Machine. M2_HOME should be set. The Maven Executable should be available in the Path.
This can be confirmed by running the command mvn -version from Terminal/Command Prompt.
2. TIBCO Flogo® Extension for Visual Studio Code should be installed or A VSIX package (.vsix) file for the TIBCO Flogo® Extension should be available.

## Installation

### From Maven Central (Recommended way)
1. The flogo maven plugin is also hosted on the Maven central. Running maven build on the Flogo app will download the plugin directly from Maven central

### From source
1. Goto https://github.com/TIBCOSoftware/flogo-maven-plugin
2. Download the Flogo Maven Plugin repository. You can either download it by cloning the repository or downloading it as a zip file and extract it to a folder.
3. Run the command from Terminal/Command Prompt.
````
mvn install
````
4. This will install the flogo maven plugin to the local repository



## Contributing to the Plug-in
If you'd like to contribute to this plug-in you can fork the repository and submit your pull request. The repository maintainers will review your PR, and they may request changes before merging it. Please reach out to integration-pm@tibco.com if you have any further questions.
