/*******************************************************************************
 * Copyright 2017 Samsung Electronics All Rights Reserved.
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
package org.edgexfoundry.support.dataprocessing.runtime.util;

import org.edgexfoundry.support.dataprocessing.runtime.task.TaskModel;

import java.lang.reflect.Modifier;

public class TaskModelLoader extends JarLoader {

  public TaskModelLoader(String jarPath, ClassLoader classLoader) throws Exception {
    super(jarPath, classLoader);
  }

  public TaskModel newInstance(String modelName) throws Exception {
    TaskModel model = null;
    Class<TaskModel> cls = getClassInstance(modelName);

    if (Modifier.isAbstract(cls.getModifiers())) {
      // if abstract class, we cannot instantiate
      return null;
    } else {
      Object obj = cls.newInstance();
      if (obj instanceof TaskModel) {
        return (TaskModel) obj;
      } else {
        return null;
      }
    }
  }
}
