import java.math.BigInteger;
import java.security.SecureRandom;

public class DigitalSignatureTest {
    public static void main(String[] args) {
        int n = 256;
        BigInteger p =  new BigInteger("1332417598677447461893313500475363476151903748659325311025356350620691477624842555612653603");
        String msg = "Nobody respects the spammish repetition";
        BigInteger g = new BigInteger("2");
        BigInteger hashmsg = new BigInteger(msg.getBytes());
        BigInteger[] keys;
        keys = genKeys(256, p, g);
        BigInteger sign = sign(keys[0], hashmsg, p, g);
        System.out.println("n: " + n);
        System.out.println("p: " + p);
        System.out.println("g: " + g);
        System.out.println("\nInput Msg: " + msg);
        System.out.println("Hash Msg: " + hashmsg);
        System.out.println("\nSk: " + keys[0]);
        System.out.println("Pk: " + keys[1]);
        System.out.println("\nSignature: " + sign+"\n");
        System.out.println(verifysign(keys[1], sign, hashmsg, p, g) + " should be True");
        System.out.println(verifysign(keys[1], sign, hashmsg.add(new BigInteger("1")), p, g) + " should be False");
    }

    public static BigInteger[] genKeys(int n, BigInteger p, BigInteger g) {
        BigInteger[] key = new BigInteger[2];
        key[0] = new BigInteger(n, new SecureRandom()); //secret key
        key[1] = g.modPow(key[0], p);  //g raised to sk mod p
        return key;
    }

    public static BigInteger sign(BigInteger sk, BigInteger hmsg, BigInteger p, BigInteger g) {

        BigInteger sign = g.modPow(hmsg.subtract(sk), p); //g raised to (hash msg - secret key) mod p
        return sign;
    }

    public static Boolean verifysign(BigInteger pk, BigInteger sign, BigInteger hmsg, BigInteger p, BigInteger g) {
        BigInteger gm = g.modPow(hmsg, p); // g raised to hashmsg mod p
        BigInteger sp = (sign.multiply(pk)).mod(p); //
        if (gm.equals(sp))
            return true;
        else
            return false;
    }

}
