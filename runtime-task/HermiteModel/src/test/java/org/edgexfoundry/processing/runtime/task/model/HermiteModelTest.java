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
package org.edgexfoundry.processing.runtime.task.model;

import org.edgexfoundry.processing.runtime.task.DataSet;
import org.edgexfoundry.processing.runtime.task.TaskModelParam;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HermiteModelTest {
    @Test
    public void testGetNameAndType() {
        HermiteModel model = new HermiteModel();
        Assert.assertNotNull(model.getName());
        Assert.assertNotNull(model.getType());
    }

    @Test
    public void testInterpolationCalculation() {

        String[] keys = {"BC", "BL", "BR", "TL", "TR"};
        double[] records = {
                618.0,
                615.0,
                617.0,
                619.0,
                618.0,
                616.0,
                617.0,
                619.0,
        };

        HermiteModel model = new HermiteModel();
        TaskModelParam params = new TaskModelParam();
        params.put("pointKey", "/Time");
        List<String> target = new ArrayList<>();
        target.add("/BC");
        target.add("/BL");
        target.add("/BR");
        target.add("/TL");
        target.add("/TR");
        params.put("target", target);
        params.put("period", 5);
        List<Double> tangents = new ArrayList<>();
        tangents.add(0.02);
        tangents.add(0.01);
        params.put("tangents", tangents);
        List<String> output = new ArrayList<>();
        output.add("/Interpolation_BC");
        output.add("/Interpolation_BL");
        output.add("/Interpolation_BR");
        output.add("/Interpolation_TL");
        output.add("/Interpolation_TR");
        params.put("outputKey", output);
        model.setParam(params);

        DataSet dataSet = DataSet.create();
        Long time = 1502323593L;
        for (int i = 0; i < records.length; i++) {
            DataSet.Record record = DataSet.Record.create();
            record.put("Start Tim", 1502323568L);
            record.put("Time", time + Long.valueOf(i * 10));
            for (int iter = 0; iter < keys.length; iter++) {
                record.put(keys[iter], records[i]);
            }

            dataSet.addRecord(record);
            //System.out.println("Stream : "+dataSet.toString()) ;
        }


        dataSet = model.calculate(dataSet, null, null);
        System.out.println("Return : " + dataSet.toString());

    }
}
