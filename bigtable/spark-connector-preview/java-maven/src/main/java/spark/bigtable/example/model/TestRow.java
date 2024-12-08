/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.bigtable.example.model;

/** A JavaBean class for using as a DataFrame row in tests. */
public class TestRow {
  private String word;
  private int count;

  public TestRow() {
  }

  public TestRow(
      String word,
      int count) {
    this.word = word;
    this.count = count;
  }

  public void setWord(String word) {
    this.word = word;
  }

  public String getWord() {
    return word;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getCount() {
    return count;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((word == null) ? 0 : word.hashCode());
    result = prime * result + count;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TestRow other = (TestRow) obj;
    if (word == null) {
      if (other.word != null)
        return false;
    } else if (!word.equals(other.word))
      return false;
    if (count != other.count)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TestRow [word="
        + word
        + ", count="
        + count
        + "]";
  }
}
