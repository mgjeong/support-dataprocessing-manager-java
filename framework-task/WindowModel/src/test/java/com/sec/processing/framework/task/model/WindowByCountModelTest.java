package com.sec.processing.framework.task.model;

import com.sec.processing.framework.task.TaskModelParam;
import org.junit.Assert;
import org.junit.Test;

public class WindowByCountModelTest {
    @Test
    public void testGetNameAndType() {
        WindowByCountModel model = new WindowByCountModel();
        Assert.assertNotNull(model.getName());
        Assert.assertNotNull(model.getType());
    }

    @Test
    public void testWindowCount() {
        int countLimit = 3;
        WindowByCountModel model = new WindowByCountModel();
        TaskModelParam param = model.getDefaultParam();
        param.put("countLimit", countLimit);
        model.setParam(param);

        /*
        StreamData[] streamDataSet = new StreamData[countLimit * 5];
        for (int i = 0; i < streamDataSet.length; i++) {
            streamDataSet[i] = new StreamData(String.valueOf(i), String.valueOf(i).getBytes());
        }

        StreamData answer = null;
        for (int i = 0; i < streamDataSet.length; i++) {
            answer = model.calculate(streamDataSet[i]);
            if (i % countLimit == countLimit - 1) {
                Assert.assertNotNull(answer);
                Assert.assertEquals(countLimit, answer.count());
                String s = "";
                for (StreamData.Record sde : answer.getBatchData()) {
                    s += sde.getValue() + "\t";
                }
                System.out.println(s);
            } else {
                Assert.assertNull(answer);
            }
        }

        throw new UnsupportedOperationException("Could you implement this for me, please?");
        */
    }
}
