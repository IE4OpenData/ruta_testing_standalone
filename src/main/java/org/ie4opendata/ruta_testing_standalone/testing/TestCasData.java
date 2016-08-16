/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

package org.ie4opendata.ruta_testing_standalone.testing;

import java.util.HashMap;
import java.io.File;


public class TestCasData implements Cloneable {

  public static final String RESULT_FOLDER = "results";

  private boolean wasEvaluated = false;

  private File resultPath;

  private File testFilePath;

  private int falsePositiveCount = 0;

  private int falseNegativeCount = 0;

  private int truePositiveCount = 0;

  @SuppressWarnings("rawtypes")
  private HashMap typeEvalData = null;

  @SuppressWarnings("rawtypes")
  public TestCasData(File testFilePath) {
    this.testFilePath = testFilePath;
    this.typeEvalData = new HashMap();
  }

  public File getPath() {
    return testFilePath;
  }

  public void setResultPath(File resultPath) {
    this.resultPath = resultPath;
  }

  public File getResultPath() {
    return resultPath;
  }

  public int getFalsePositiveCount() {
    return this.falsePositiveCount;
  }

  public int getFalseNegativeCount() {
    return this.falseNegativeCount;
  }

  public int getTruePositiveCount() {
    return this.truePositiveCount;
  }

  public void setFalsePositiveCount(int falsePositiveCount) {
    this.falsePositiveCount = falsePositiveCount;
  }

  public void setFalseNegativeCount(int falseNegativeCount) {
    this.falseNegativeCount = falseNegativeCount;
  }

  public void setTruePositiveCount(int truePositiveCount) {
    this.truePositiveCount = truePositiveCount;
  }

  public boolean wasEvaluated() {
    return this.wasEvaluated;
  }

  public void setEvaluationStatus(boolean wasEvaluated) {
    this.wasEvaluated = wasEvaluated;
  }

  @SuppressWarnings("rawtypes")
  public HashMap getTypeEvalData() {
    return typeEvalData;
  }

  @SuppressWarnings("rawtypes")
  public void setTypeEvalData(HashMap typeEvalData) {
    this.typeEvalData = typeEvalData;
  }

}
