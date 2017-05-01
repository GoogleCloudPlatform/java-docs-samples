# Samples Builder

Creates a single deployable Flex application from Flex sample applications under 
[samples-source](../samples-source).

# Run
`mvn package` creates the single Flex application under [samples](..).
Packages included in the application can be configured using `flexible.samples` property 
in the parent [pom.xml](../pom.xml)
