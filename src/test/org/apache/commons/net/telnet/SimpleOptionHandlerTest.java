package org.apache.commons.net.telnet;

/***
 * JUnit test class for SimpleOptionHandler
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class SimpleOptionHandlerTest extends TelnetOptionHandlerTestAbstract
{
    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(SimpleOptionHandlerTest.class);
    }

    /***
     * setUp for the test.
     ***/
    protected void setUp()
    {
        opthand1 = new SimpleOptionHandler(4);
        opthand2 = new SimpleOptionHandler(8, true, true, true, true);
        opthand3 = new SimpleOptionHandler(30, false, false, false, false);
    }

    /***
     * test of the constructors.
     ***/
    public void testConstructors()
    {
        assertEquals(opthand1.getOptionCode(), 4);
        assertEquals(opthand2.getOptionCode(), 8);
        assertEquals(opthand3.getOptionCode(), 30);
        super.testConstructors();
    }

    /***
     * test of client-driven subnegotiation.
     * Checks that no subnegotiation is made.
     ***/
    public void testStartSubnegotiation()
    {

        int resp1[] = opthand1.startSubnegotiationLocal();
        int resp2[] = opthand1.startSubnegotiationRemote();

        assertEquals(resp1, null);
        assertEquals(resp2, null);
    }

    /***
     * test of server-driven subnegotiation.
     * Checks that no subnegotiation is made.
     ***/
    public void testAnswerSubnegotiation()
    {
        int subn[] =
        {
            TelnetCommand.IAC, TelnetCommand.SB, 4,
            1, TelnetCommand.IAC, TelnetCommand.SE,
        };

        int resp1[] = opthand1.answerSubnegotiation(subn, subn.length);

        assertEquals(resp1, null);
    }
}