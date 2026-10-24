package io.github.op7418.guizangtv;

import org.junit.Test;
import static org.junit.Assert.*;

public class HomeLayoutPolicyTest {
    @Test public void sevenRowsFillTheMeasuredViewportExactly() {
        int[] gaps = gaps(7, 7*52, 450, 8);
        assertEquals(450, 7*52+sum(gaps));
        for (int gap : gaps) assertTrue(gap == 14 || gap == 15);
        assertEquals(0, HomeLayoutPolicy.gapAfter(6,7,7*52,450,8));
    }

    @Test public void changedViewportRedistributesWithoutAccumulatingOldGaps() {
        assertEquals(540, 7*52+sum(gaps(7,7*52,540,8)));
        assertEquals(450, 7*52+sum(gaps(7,7*52,450,8)));
    }

    @Test public void largeFontNineRowsOverflowForScrollingInsteadOfShrinking() {
        int[] gaps = gaps(9,9*68,450,8);
        for (int gap : gaps) assertEquals(8,gap);
        assertTrue(9*68+sum(gaps)>450);
    }

    @Test public void aSingleRowHasNoArtificialTrailingGap() {
        assertEquals(0,HomeLayoutPolicy.gapAfter(0,1,52,450,8));
        assertEquals(0,HomeLayoutPolicy.gapAfter(0,0,0,450,8));
    }

    @Test public void distributionIsEvenAcrossCountsAndRemainders() {
        for (int count=2;count<=9;count++) for (int extra=0;extra<40;extra++) {
            int rows=count*50,viewport=rows+(count-1)*8+extra;
            int[] values=gaps(count,rows,viewport,8);
            assertEquals(viewport,rows+sum(values));
            assertTrue(values[0]-values[values.length-1]<=1);
        }
    }

    private static int[] gaps(int count,int rows,int viewport,int minimum) {
        int[] result=new int[count-1];
        for(int i=0;i<result.length;i++) result[i]=HomeLayoutPolicy.gapAfter(i,count,rows,viewport,minimum);
        return result;
    }
    private static int sum(int[] values) { int result=0;for(int value:values)result+=value;return result; }
}
