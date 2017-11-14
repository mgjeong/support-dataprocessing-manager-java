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

public class LinearInterpolationModelTest {
    @Test
    public void testGetNameAndType() {
        LinearInterpolationModel model = new LinearInterpolationModel();
        Assert.assertNotNull(model.getName());
        Assert.assertNotNull(model.getType());
    }

    @Test
    public void testErrorCalculation() {

        String[] keys = {"Residue_BC", "Residue_BL", "Residue_BR", "Residue_TL", "Residue_TR"};


        LinearInterpolationModel model = new LinearInterpolationModel();
        TaskModelParam params = new TaskModelParam();
        params.put("pointKey", "/Time");
        List<String> target = new ArrayList<>();
        target.add("/BC");
        target.add("/BL");
        target.add("/BR");
        target.add("/TL");
        target.add("/TR");
        params.put("target", target);
        params.put("period", 30);
        List<String> output = new ArrayList<>();
        output.add("/Interpolation_BC");
        output.add("/Interpolation_BL");
        output.add("/Interpolation_BR");
        output.add("/Interpolation_TL");
        output.add("/Interpolation_TR");
        params.put("outputKey", output);

        TaskModelParam interval = new TaskModelParam();
        interval.put("data", 2);
        params.put("interval", interval);

        model.setParam(params);


        String[] records = {
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323405, \"BC\": 661, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235003\", \"Time\": 1502323563}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323568, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235246\", \"Time\": 1502323593}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323568, \"BC\": 664, \"BL\": 713, \"BR\": 709, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235246\", \"Time\": 1502323623}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323568, \"BC\": 665, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235246\", \"Time\": 1502323653}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323568, \"BC\": 661, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235246\", \"Time\": 1502323683}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323568, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235246\", \"Time\": 1502323713}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323743}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 668, \"BL\": 713, \"BR\": 711, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323773}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 669, \"BL\": 711, \"BR\": 709, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323803}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 666, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323833}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 663, \"BL\": 708, \"BR\": 708, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323863}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323732, \"BC\": 657, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235529\", \"Time\": 1502323893}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323894, \"BC\": 657, \"BL\": 710, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235813\", \"Time\": 1502323923}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323894, \"BC\": 666, \"BL\": 714, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235813\", \"Time\": 1502323953}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323894, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235813\", \"Time\": 1502323983}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323894, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235813\", \"Time\": 1502324013}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502323894, \"BC\": 655, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170809-235813\", \"Time\": 1502324043}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 654, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324073}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 662, \"BL\": 713, \"BR\": 709, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324103}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 667, \"BL\": 714, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324133}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 662, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324163}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 659, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324193}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324058, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000055\", \"Time\": 1502324223}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324253}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 664, \"BL\": 715, \"BR\": 712, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324283}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 666, \"BL\": 712, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324313}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324343}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324373}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324244, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000339\", \"Time\": 1502324403}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324433}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 663, \"BL\": 714, \"BR\": 710, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324463}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 668, \"BL\": 713, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324493}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 666, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324523}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 660, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324553}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324421, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000622\", \"Time\": 1502324583}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324613}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 660, \"BL\": 711, \"BR\": 708, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324643}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 666, \"BL\": 709, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324673}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324703}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 659, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324733}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324597, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-000905\", \"Time\": 1502324763}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 655, \"BL\": 709, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324793}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 667, \"BL\": 715, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324823}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 666, \"BL\": 710, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324853}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 663, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324883}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 660, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324913}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324773, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001148\", \"Time\": 1502324943}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 654, \"BL\": 708, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502324973}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 665, \"BL\": 715, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502325003}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 666, \"BL\": 711, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502325033}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 660, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502325063}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502325093}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502324949, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001431\", \"Time\": 1502325123}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325126, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001737\", \"Time\": 1502325153}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325126, \"BC\": 666, \"BL\": 713, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001737\", \"Time\": 1502325183}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325126, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001737\", \"Time\": 1502325213}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325126, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001737\", \"Time\": 1502325243}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325126, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-001737\", \"Time\": 1502325273}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325303}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 659, \"BL\": 712, \"BR\": 709, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 705, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325333}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 666, \"BL\": 714, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325363}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325393}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325423}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325302, \"BC\": 656, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002034\", \"Time\": 1502325453}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 654, \"BL\": 708, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325483}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 655, \"BL\": 709, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325513}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 667, \"BL\": 715, \"BR\": 712, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325543}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325573}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325603}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325479, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002330\", \"Time\": 1502325633}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325663}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 654, \"BL\": 708, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325693}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 666, \"BL\": 711, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325723}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 663, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325753}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 660, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325783}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325655, \"BC\": 657, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002627\", \"Time\": 1502325813}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325818, \"BC\": 657, \"BL\": 710, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002923\", \"Time\": 1502325843}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325818, \"BC\": 667, \"BL\": 714, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002923\", \"Time\": 1502325873}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325818, \"BC\": 666, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002923\", \"Time\": 1502325903}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325818, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002923\", \"Time\": 1502325933}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325818, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-002923\", \"Time\": 1502325963}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502325993}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 662, \"BL\": 713, \"BR\": 709, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502326023}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 667, \"BL\": 713, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502326053}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502326083}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 658, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502326113}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502325981, \"BC\": 655, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003219\", \"Time\": 1502326143}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326145, \"BC\": 654, \"BL\": 708, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003516\", \"Time\": 1502326173}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326145, \"BC\": 664, \"BL\": 715, \"BR\": 710, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003516\", \"Time\": 1502326203}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326145, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003516\", \"Time\": 1502326233}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326145, \"BC\": 659, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003516\", \"Time\": 1502326263}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326145, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003516\", \"Time\": 1502326293}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326308, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003813\", \"Time\": 1502326323}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326308, \"BC\": 666, \"BL\": 715, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003813\", \"Time\": 1502326353}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326308, \"BC\": 666, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003813\", \"Time\": 1502326383}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326308, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003813\", \"Time\": 1502326413}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326308, \"BC\": 659, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-003813\", \"Time\": 1502326443}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326473}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 661, \"BL\": 713, \"BR\": 709, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326503}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 667, \"BL\": 714, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326533}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326563}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326593}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326471, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004109\", \"Time\": 1502326623}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326634, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004352\", \"Time\": 1502326653}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326634, \"BC\": 664, \"BL\": 714, \"BR\": 710, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004352\", \"Time\": 1502326683}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326634, \"BC\": 667, \"BL\": 712, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004352\", \"Time\": 1502326713}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326634, \"BC\": 660, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004352\", \"Time\": 1502326743}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326634, \"BC\": 658, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004352\", \"Time\": 1502326773}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 655, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326803}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 655, \"BL\": 709, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326833}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 666, \"BL\": 710, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326863}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326893}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 659, \"BL\": 708, \"BR\": 708, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326923}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326797, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004635\", \"Time\": 1502326953}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326960, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004918\", \"Time\": 1502326983}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326960, \"BC\": 666, \"BL\": 714, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004918\", \"Time\": 1502327013}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326960, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004918\", \"Time\": 1502327043}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326960, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004918\", \"Time\": 1502327073}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502326960, \"BC\": 658, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-004918\", \"Time\": 1502327103}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 654, \"BL\": 708, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327133}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 664, \"BL\": 713, \"BR\": 709, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327163}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 667, \"BL\": 712, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327193}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327223}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 660, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327253}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327123, \"BC\": 655, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005201\", \"Time\": 1502327283}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327286, \"BC\": 654, \"BL\": 708, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005448\", \"Time\": 1502327313}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327286, \"BC\": 665, \"BL\": 715, \"BR\": 711, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005448\", \"Time\": 1502327343}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327286, \"BC\": 666, \"BL\": 711, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005448\", \"Time\": 1502327373}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327286, \"BC\": 660, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005448\", \"Time\": 1502327403}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327286, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005448\", \"Time\": 1502327433}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 655, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327463}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 654, \"BL\": 708, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327493}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 667, \"BL\": 712, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327523}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327553}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 660, \"BL\": 708, \"BR\": 708, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327583}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327460, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-005743\", \"Time\": 1502327613}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327643}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 648, \"BL\": 706, \"BR\": 706, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 705, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327673}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 648, \"BL\": 710, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327703}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 653, \"BL\": 710, \"BR\": 710, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327733}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 654, \"BL\": 709, \"BR\": 710, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327763}",
                "{\"Status\": \"FALSE\", \"StartTime\": 0, \"BC\": 653, \"BL\": 709, \"BR\": 710, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"NA\", \"TrackingID\": \"NA\", \"Time\": 1502327793}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 653, \"BL\": 708, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327823}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 658, \"BL\": 711, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327853}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 671, \"BL\": 716, \"BR\": 712, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327883}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 670, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327913}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 667, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327943}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327813, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010253\", \"Time\": 1502327973}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 661, \"BL\": 707, \"BR\": 708, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328003}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 668, \"BL\": 714, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328033}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 668, \"BL\": 710, \"BR\": 709, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 712, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328063}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 665, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328093}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 661, \"BL\": 708, \"BR\": 708, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328123}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502327990, \"BC\": 658, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010537\", \"Time\": 1502328153}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 654, \"BL\": 707, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328183}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 666, \"BL\": 715, \"BR\": 710, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328213}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 668, \"BL\": 712, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328243}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 665, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328273}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 659, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328303}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328166, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-010820\", \"Time\": 1502328333}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328343, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011114\", \"Time\": 1502328363}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328343, \"BC\": 662, \"BL\": 713, \"BR\": 709, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011114\", \"Time\": 1502328393}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328343, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 711, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011114\", \"Time\": 1502328423}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328343, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011114\", \"Time\": 1502328453}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328343, \"BC\": 659, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011114\", \"Time\": 1502328483}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328513}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 664, \"BL\": 715, \"BR\": 711, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328543}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 666, \"BL\": 712, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328573}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 663, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328603}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 660, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328633}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328506, \"BC\": 657, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011411\", \"Time\": 1502328663}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328669, \"BC\": 655, \"BL\": 709, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011707\", \"Time\": 1502328693}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328669, \"BC\": 666, \"BL\": 713, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011707\", \"Time\": 1502328723}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328669, \"BC\": 665, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011707\", \"Time\": 1502328753}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328669, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011707\", \"Time\": 1502328783}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328669, \"BC\": 656, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-011707\", \"Time\": 1502328813}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 654, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328843}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 656, \"BL\": 711, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328873}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 665, \"BL\": 715, \"BR\": 711, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328903}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 661, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328933}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 658, \"BL\": 708, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328963}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502328837, \"BC\": 656, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012003\", \"Time\": 1502328993}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329000, \"BC\": 654, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012300\", \"Time\": 1502329023}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329000, \"BC\": 660, \"BL\": 713, \"BR\": 709, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012300\", \"Time\": 1502329053}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329000, \"BC\": 665, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012300\", \"Time\": 1502329083}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329000, \"BC\": 662, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012300\", \"Time\": 1502329113}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329000, \"BC\": 658, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012300\", \"Time\": 1502329143}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 656, \"BL\": 707, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329173}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 666, \"BL\": 713, \"BR\": 711, \"TR\": 708, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329203}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 667, \"BL\": 711, \"BR\": 710, \"TR\": 711, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329233}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 664, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329263}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 661, \"BL\": 708, \"BR\": 708, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329293}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329163, \"BC\": 656, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012556\", \"Time\": 1502329323}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329327, \"BC\": 656, \"BL\": 709, \"BR\": 707, \"TR\": 707, \"Equipment\": \"KFF-41R\", \"TL\": 707, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012839\", \"Time\": 1502329353}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329327, \"BC\": 667, \"BL\": 714, \"BR\": 711, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 708, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012839\", \"Time\": 1502329383}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329327, \"BC\": 666, \"BL\": 709, \"BR\": 709, \"TR\": 710, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012839\", \"Time\": 1502329413}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329327, \"BC\": 662, \"BL\": 708, \"BR\": 709, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 710, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012839\", \"Time\": 1502329443}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329327, \"BC\": 657, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-012839\", \"Time\": 1502329473}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329500, \"BC\": 655, \"BL\": 708, \"BR\": 708, \"TR\": 709, \"Equipment\": \"KFF-41R\", \"TL\": 709, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-013123\", \"Time\": 1502329503}",
                "{\"Status\": \"TRUE\", \"StartTime\": 1502329500, \"BC\": 654, \"BL\": 709, \"BR\": 707, \"TR\": 706, \"Equipment\": \"KFF-41R\", \"TL\": 706, \"RecipeName\": \"D1F_4C_R01\", \"TrackingID\": \"041R-170810-013123\", \"Time\": 1502329533}"
        };

        for (int i = 0; i < records.length; i++) {
        //for (int i = 0; i < 2; i++) {
            DataSet dataSet = DataSet.create();
            dataSet.addRecord(records[i]);
            dataSet = model.calculate(dataSet, null, null);
            if (dataSet == null) {
                System.out.println("ignored input " + i);
            } else {

                System.out.println(dataSet.toString());
            }
        }
        //System.out.println("Return : " + dataSet.toString());
    }
}
