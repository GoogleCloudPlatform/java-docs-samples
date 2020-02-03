FROM gcr.io/dataflow-templates-base/java11-template-launcher-base:latest

ARG WORKDIR=/template
WORKDIR ${WORKDIR}

# Define the Java command options required by Dataflow Flex Templates.
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="org.apache.beam.samples.KafkaToBigQuery"
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="${WORKDIR}"

# Make sure to package as a "fat JAR", including all dependencies.
COPY target/kafka-to-bigquery-1.0.jar kafka-to-bigquery.jar
