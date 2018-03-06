/*******************************************************************************
 * Copyright 2018 Samsung Electronics All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow;

import java.util.ArrayList;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.ComponentUISpecification;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.UIField;
import org.edgexfoundry.support.dataprocessing.runtime.data.model.workflow.WorkflowComponentBundle.WorkflowComponentBundleType;
import org.edgexfoundry.support.dataprocessing.runtime.task.TaskParam.UiFieldType;
import org.junit.Assert;
import org.junit.Test;

public class WorkflowComponentBundleTest {

  @Test
  public void testSetterAndGetter() {
    WorkflowComponentBundle bundle = new WorkflowComponentBundle();

    // Setter
    bundle.setId(1L);
    bundle.setName("sample");
    bundle.setType(WorkflowComponentBundleType.toWorkflowComponentBundleType("SOURCE"));
    bundle.setStreamingEngine("FLINK");
    bundle.setSubType("subType");
    bundle.setBundleJar("bundleJar");
    bundle.setTransformationClass("transformationClass");
    bundle.setBuiltin(true);
    ComponentUISpecification uiSpecification = new ComponentUISpecification();
    UIField fieldName = makeSampleUIField();
    uiSpecification.addUIField(fieldName);
    bundle.setWorkflowComponentUISpecification(uiSpecification);

    // Getter
    Assert.assertEquals(1L, bundle.getId().longValue());
    Assert.assertEquals("sample", bundle.getName());
    Assert.assertEquals(WorkflowComponentBundleType.SOURCE, bundle.getType());
    Assert.assertEquals("FLINK", bundle.getStreamingEngine());
    Assert.assertEquals("subType", bundle.getSubType());
    Assert.assertEquals("bundleJar", bundle.getBundleJar());
    Assert.assertEquals("transformationClass", bundle.getTransformationClass());
    Assert.assertEquals(true, bundle.isBuiltin());
    Assert.assertEquals(1,
        bundle.getWorkflowComponentUISpecification().getFields().size());
    UIField uiField = uiSpecification.getFields().get(0);
    Assert.assertTrue(!uiField.getOptions().isEmpty());

    uiSpecification.setFields(new ArrayList<>());
    Assert.assertEquals(0,
        bundle.getWorkflowComponentUISpecification().getFields().size());

  }

  @Test
  public void testUIField() {
    Assert.assertEquals("string", UiFieldType.STRING.getUiFieldTypeText());

    UIField fieldName = makeSampleUIField();

    Assert.assertEquals(UiFieldType.STRING, fieldName.getType());
    Assert.assertEquals("name", fieldName.getFieldName());
    Assert.assertEquals(false, fieldName.getOptional());
    Assert.assertEquals("Enter name", fieldName.getTooltip());
    Assert.assertEquals("Name", fieldName.getUiName());
    Assert.assertEquals(true, fieldName.getUserInput());
    Assert.assertEquals("Joey", fieldName.getDefaultValue());
  }

  @Test
  public void testComponentUISpecification() {
    ComponentUISpecification uiSpecification = new ComponentUISpecification();

    try {
      uiSpecification.setFields(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }

    uiSpecification.addUIField(makeSampleUIField());
    uiSpecification.addUIField(makeSampleUIField());
    uiSpecification.addUIField(makeSampleUIField());
    Assert.assertEquals(3, uiSpecification.getFields().size());

    try {
      uiSpecification.addUIField(null);
      Assert.fail("Should not reach here.");
    } catch (RuntimeException e) {
      // success
    }
  }

  @Test
  public void testWorkflowComponentBundleType() {
    WorkflowComponentBundleType sink =
        WorkflowComponentBundleType.toWorkflowComponentBundleType("sink");
    Assert.assertEquals(WorkflowComponentBundleType.SINK, sink);

    WorkflowComponentBundleType invalid = WorkflowComponentBundleType
        .toWorkflowComponentBundleType("invalid");
    Assert.assertNull(invalid);
  }

  private UIField makeSampleUIField() {
    UIField fieldName = new UIField();
    fieldName.setType(UiFieldType.STRING);
    fieldName.setFieldName("name");
    fieldName.setOptional(false);
    fieldName.setTooltip("Enter name");
    fieldName.setUiName("Name");
    fieldName.setUserInput(true);
    fieldName.setDefaultValue("Joey");
    fieldName.setOptions(new ArrayList<>());
    fieldName.addOption("3");
    return fieldName;
  }

}
