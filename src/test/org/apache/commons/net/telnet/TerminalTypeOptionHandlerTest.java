package org.apache.commons.net.telnet;

/***
 * JUnit test class for TerminalTypeOptionHandler
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class TerminalTypeOptionHandlerTest extends TelnetOptionHandlerTestAbstract
{
    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(TerminalTypeOptionHandlerTest.class);
    }

    /***
     * setUp for the test.
     ***/
    protected void setUp()
    {
        opthand1 = new TerminalTypeOptionHandler("VT100");
        opthand2 = new TerminalTypeOptionHandler("ANSI", true, true, true, true);
        opthand3 = new TerminalTypeOptionHandler("ANSI", false, false, false, false);
    }

    /***
     * test of the constructors.
     ***/
    public void testConstructors()
    {
        assertEquals(opthand1.getOptionCode(), TelnetOption.TERMINAL_TYPE);
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
     * test of client-driven subnegotiation.
     * Checks that the terminal type is sent
     ***/
    public void testAnswerSubnegotiation()
    {
        int subn[] =
        {
            TelnetOption.TERMINAL_TYPE, 1
        };

        int expected1[] =
        {
            TelnetOption.TERMINAL_TYPE, 0, 'V', 'T', '1', '0', '0'
        };

        int expected2[] =
        {
            TelnetOption.TERMINAL_TYPE, 0, 'A', 'N', 'S', 'I'
        };

        int resp1[] = opthand1.answerSubnegotiation(subn, subn.length);
        int resp2[] = opthand2.answerSubnegotiation(subn, subn.length);

        assertTrue(equalInts(resp1, expected1));
        assertTrue(equalInts(resp2, expected2));
    }


    /***
     * compares two arrays of int
     ***/
    protected boolean equalInts(int a1[], int a2[])
    {
        if(a1.length != a2.length)
        {
            return(false);
        }
        else
        {
            boolean result = true;
            for(int ii=0; ii<a1.length; ii++)
            {
                if(a1[ii]!= a2[ii])
                    result = false;
            }
            return(result);
        }
    }
}