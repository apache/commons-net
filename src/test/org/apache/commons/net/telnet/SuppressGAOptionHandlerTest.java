package org.apache.commons.net.telnet;

/***
 * JUnit test class for SuppressGAOptionHandler
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class SuppressGAOptionHandlerTest extends TelnetOptionHandlerTestAbstract
{
    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(SuppressGAOptionHandlerTest.class);
    }

    /***
     * setUp for the test.
     ***/
    protected void setUp()
    {
        opthand1 = new SuppressGAOptionHandler();
        opthand2 = new SuppressGAOptionHandler(true, true, true, true);
        opthand3 = new SuppressGAOptionHandler(false, false, false, false);
    }

    /***
     * test of the constructors.
     ***/
    public void testConstructors()
    {
        assertEquals(opthand1.getOptionCode(), TelnetOption.SUPPRESS_GO_AHEAD);
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
            TelnetCommand.IAC, TelnetCommand.SB, TelnetOption.SUPPRESS_GO_AHEAD,
            1, TelnetCommand.IAC, TelnetCommand.SE,
        };

        int resp1[] = opthand1.answerSubnegotiation(subn, subn.length);

        assertEquals(resp1, null);
    }
}