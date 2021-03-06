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

package org.ie4opendata.ruta_testing_standalone.testing.evaluator;

public class WordTemplateCasEvaluatorFactory implements ICasEvaluatorFactory {

  public ICasEvaluator createEvaluator() {
    return new WordTemplateCasEvaluator();
  }

  public String getDescription() {
    return "Complex feature structures that provide at least one annotation as a feature value are compared. A template feature structure is a true positive if all all feature values are correct. Here, the word level evaluator is applied for the comparisionof the feature values.";
  }

}
