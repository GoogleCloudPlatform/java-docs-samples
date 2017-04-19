:
: Demonstrates how to run the Analyze sample.
:#########################################################################

: Copyright 2016 Google Inc.
:
: Licensed under the Apache License, Version 2.0 (the "License"); 
: you may not use this file except in compliance with the License.  
: You may obtain a copy of the License at 
:   http://www.apache.org/licenses/LICENSE-2.0
:
: Unless required by applicable law or agreed to in writing, software 
: distributed under the License is distributed on an "AS IS" BASIS, 
: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
: See the License for the specific language governing permissions and
: limitations under the License.
:#########################################################################


:call:run_nl entities "The quick brown fox jumped over the lazy dog."
:call:run_nl sentiment "The quick brown fox jumped over the lazy dog." 
:call:run_nl syntax "The quick brown fox jumped over the lazy dog." 
call:run_nl_all

:###################################### 
: Performs a language operation on the given text or GCS object.
: Globals:
:   None 
: Arguments:
:   $1 The operation to perform, either entities, sentiment, or syntax.
:   $2 The text or GCS object to operate on.
: Returns:
:   None
:###################################### 
:run_nl 
set main_class=com.google.cloud.language.samples.Analyze 
set jar_file=target/language-entities-1.0-jar-with-dependencies.jar 
java -cp %jar_file% %main_class% %~1 "%~2" 
EXIT /B 

:###################################### 
: Exercises the sample code on various example text and GCS objects.  
: Globals:
 
:   None 
: Arguments: 
:   None 
: Returns: 
:   None 
:###################################### 
:run_nl_all 
setlocal EnableDelayedExpansion
set quote=Larry Page, Google's co-founder, once described the 'perfect ^
search engine' as something that 'understands exactly what you mean and ^
gives you back exactly what you want.' Since he spoke those words Google ^
has grown to offer products beyond search, but the spirit of what he said ^
remains.^


echo "%quote%"
set gs_path="gs://bucket/file.txt" 
call:run_nl entities "%quote%"
call:run_nl entities %gs_path% 
call:run_nl sentiment "%quote%"
call:run_nl sentiment %gs_path%
call:run_nl syntax "%quote%"
call:run_nl syntax %gs_path%
EXIT /B

