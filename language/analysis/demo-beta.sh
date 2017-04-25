#!/bin/bash
#
# Demonstrates how to run the AnalyzeBeta sample.

##########################################################################
# Copyright 2017 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##########################################################################


#######################################
# Performs a language operation on the given text or GCS object.
# Globals:
#   None
# Arguments:
#   $1 The operation to perform, either entities, sentiment, or syntax.
#   $2 The text or GCS object to operate on.
# Returns:
#   None
#######################################
function run_nl() {
  local main_class=com.google.cloud.language.samples.AnalyzeBeta
  local jar_file=target/language-entities-1.0-jar-with-dependencies.jar
  java -cp ${jar_file} ${main_class} "$1" "$2"
}

#######################################
# Exercises the sample code on various example text and GCS objects.
# Globals:
#   None
# Arguments:
#   None
# Returns:
#   None
#######################################
function run_nl_all() {
  local quote_de="Bananen sind die köstlichsten Früchte, ich liebe sie zu
      essen. Ich mag sie so sehr wie Ananas."
  local quote="Larry Page, Google's co-founder, once described the 'perfect
      search engine' as something that 'understands exactly what you mean and
      gives you back exactly what you want.' Since he spoke those words Google
      has grown to offer products beyond search, but the spirit of what he said
      remains."
  local gs_path="gs://cloud-samples-tests/natural-language/gettysburg.txt"

  run_nl entities-sentiment "${quote}"
  run_nl entities-sentiment "${gs_path}"
  run_nl sentiment "${quote_de}" "DE"
}

run_nl_all
