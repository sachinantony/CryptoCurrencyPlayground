import java.io.FileNotFoundException;
import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        System.out.println("Running DigitalSignature Class:");
        DigitalSignatureTest.main(null);
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Running TestTXHandler Class:");
        try
        {
            TestTxHandler.main(null);
        }
        catch (FileNotFoundException f ) {
            System.out.println(f);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Running TestMaxFeeTXHandler Class:");
        try
        {
            TestMaxFeeTxHandler.main(null);
        }
        catch (FileNotFoundException f ) {
            System.out.println(f);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("All Tests Passed!!!");
    }


}
