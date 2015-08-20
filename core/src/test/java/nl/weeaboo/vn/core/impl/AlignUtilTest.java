package nl.weeaboo.vn.core.impl;

import org.junit.Assert;
import org.junit.Test;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.LvnTestUtil;
import nl.weeaboo.vn.NvlTestUtil;
import nl.weeaboo.vn.math.Vec2;

public class AlignUtilTest {

    private static final double EPSILON = NvlTestUtil.EPSILON;

    @Test
    public void alignOffset() {
        Assert.assertEquals(0, AlignUtil.getAlignOffset(100, 0), EPSILON);
        Assert.assertEquals(-50, AlignUtil.getAlignOffset(100, .5), EPSILON);
        Assert.assertEquals(-100, AlignUtil.getAlignOffset(100, 1), EPSILON);
    }

    @Test
    public void alignSubRect() {
        Vec2 v = AlignUtil.alignSubRect(Rect2D.of(10, 10, 10, 10), 50, 40, 3);
        LvnTestUtil.assertEquals(30.0 / 50.0, 20.0 / 40.0, v, EPSILON);

        v = AlignUtil.alignSubRect(Rect2D.of(20, 5, 0, 15), 50, 40, 3);
        LvnTestUtil.assertEquals(30.0 / 50.0, 20.0 / 40.0, v, EPSILON);

        // Test all valid anchor positions
        Rect2D r = Rect2D.of(0, 0, 10, 10);
        LvnTestUtil.assertEquals(.00, .50, AlignUtil.alignSubRect(r, 20, 20, 1), EPSILON);
        LvnTestUtil.assertEquals(.25, .50, AlignUtil.alignSubRect(r, 20, 20, 2), EPSILON);
        LvnTestUtil.assertEquals(.50, .50, AlignUtil.alignSubRect(r, 20, 20, 3), EPSILON);
        LvnTestUtil.assertEquals(.00, .25, AlignUtil.alignSubRect(r, 20, 20, 4), EPSILON);
        LvnTestUtil.assertEquals(.25, .25, AlignUtil.alignSubRect(r, 20, 20, 5), EPSILON);
        LvnTestUtil.assertEquals(.50, .25, AlignUtil.alignSubRect(r, 20, 20, 6), EPSILON);
        LvnTestUtil.assertEquals(.00, .00, AlignUtil.alignSubRect(r, 20, 20, 7), EPSILON);
        LvnTestUtil.assertEquals(.25, .00, AlignUtil.alignSubRect(r, 20, 20, 8), EPSILON);
        LvnTestUtil.assertEquals(.50, .00, AlignUtil.alignSubRect(r, 20, 20, 9), EPSILON);
    }

}