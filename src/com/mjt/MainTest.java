package com.mjt;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MainTest {
    @Test
    public  void validateTest(){

        //GOOD INPUT PATTERNS
        String goodAddOrder1="28800538 A b S 44.26 100";
        String goodAddOrder2="55606874 A mukhb B 44.44 250";
        String goodReduceOrder="28800744 R b 100";

        assertTrue(Main.validateOrder(goodAddOrder1));
        assertTrue(Main.validateOrder(goodAddOrder2));
        assertTrue(Main.validateOrder(goodReduceOrder));

        //BAD INPUT PATTERNS
        String badAddOrder1="28800538 r b S 44.26 100";;
        String badAddOrder2="55606926 A svkhb Q 44.45 100";
        String badReduceOrder="55607544 R wh lhb 100";

        assertFalse(Main.validateOrder(badAddOrder1));
        assertFalse(Main.validateOrder(badAddOrder2));
        assertFalse(Main.validateOrder(badReduceOrder));

        //EMPTY
        String empty="";
        assertFalse(Main.validateOrder(empty));
    }
    @Test
    public void parseAddTest() throws Exception {
        String goodAddOrder1="28800538 A b S 44.26 100";
        AddOrder controll = new AddOrder(28800538,"A","b","S",(float)44.26,100,0);
        AddOrder testOrder = (AddOrder) Main.parseOrder(goodAddOrder1,0);

        assertEquals("Controll id should match parsed addOrder",controll.id,Main.parseOrder(goodAddOrder1,0).id);
        assertEquals("Controll numStocks should match parsed addOrder",controll.numStocks,Main.parseOrder(goodAddOrder1,0).numStocks);
        assertEquals("Controll timeStamp should match parsed addOrder",controll.timestamp,Main.parseOrder(goodAddOrder1,0).timestamp);
        assertEquals("Controll side should match parsed addOrder",controll.side,testOrder.side);
        assertTrue(controll.price== testOrder.price);

    }
    @Test
    public void parseReduceTest() throws Exception {
        String goodAddOrder1="28800744 R b 100";
        ReduceOrder controll = new ReduceOrder(28800744,"R","b",100,0);

        assertEquals("Controll id should match parsed addOrder",controll.id,Main.parseOrder(goodAddOrder1,0).id);
        assertEquals("Controll numStocks should match parsed addOrder",controll.numStocks,Main.parseOrder(goodAddOrder1,0).numStocks);
        assertEquals("Controll timeStamp should match parsed addOrder",controll.timestamp,Main.parseOrder(goodAddOrder1,0).timestamp);

    }
}
