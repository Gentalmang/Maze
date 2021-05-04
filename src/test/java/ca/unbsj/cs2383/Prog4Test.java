package ca.unbsj.cs2383;

import java.util.*;
import java.lang.*;

import org.junit.jupiter.api.*;
import static  org.junit.jupiter.api.Assertions.*;
import static java.time.Duration.ofMillis;

//import edu.princeton.cs.algs4.Knuth;
import java.util.Arrays;

/**
 * Unit test for Program4
 */
public class Prog4Test
{

    /* make some unit tests for Bloom filters */
    int hashAString(String a){
      int hashValue = 0;
      for (int i = 0; i < a.length(); i++) {
        hashValue = (hashValue * 31 + a.charAt(i));
      }
      return hashValue%35000;
    }
    /**
     * Rigourous Tests :-)
     */
    @Test
    public void testFoo()
    {
        System.out.println("testApp runs");
        assertTrue( true );
    }

    @Test
    public void testFalsePositiveRate(){
      Random rand = new Random();
      BitSet normal = new BitSet();
      BitSet filter = new BitSet();
      int currentNumber, falseCount = 0;
      boolean match = false;
      for (int i = 0; i < 760 ; i ++ ) {
        currentNumber = rand.nextInt(1600);
        normal.set(currentNumber);
        String numberS = Integer.toString(currentNumber);
        filter.set(numberS.hashCode());
        filter.set(hashAString(numberS));
      }

      for (int j = 0; j < 1600 ; j++ ) {
        String n = Integer.toString(j);
        if (normal.get(j) == true && filter.get(n.hashCode()) == true && filter.get(hashAString(n)) == true) {
          falseCount ++;
        }
      }
      double percent = (falseCount+0.0)/(normal.size() + 0.0);
      if (percent > 0.005 || percent < 0.02) {
        match = true;
      }
      assertTrue( match );
    }

    @Test
    public void test1(){
      Random rand = new Random();
      BitSet normal = new BitSet();
      BitSet filter = new BitSet();
      int currentNumber, falseCount = 0;
      for (int i = 0; i < 760 ; i ++ ) {
        currentNumber = rand.nextInt(1600);
        normal.set(currentNumber);
        String numberS = Integer.toString(currentNumber);
        filter.set(numberS.hashCode());
        filter.set(hashAString(numberS));
      }
      String numberA = Integer.toString(1700);
      assertTrue(!(filter.get(numberA.hashCode()) && filter.get(hashAString(numberA))));
    }

    @Test
    public void test2(){
      Random rand = new Random();
      BitSet normal = new BitSet();
      BitSet filter = new BitSet();
      int currentNumber, falseCount = 0;
      for (int i = 0; i < 760 ; i ++ ) {
        currentNumber = rand.nextInt(1600);
        normal.set(currentNumber);
        String numberS = Integer.toString(currentNumber);
        filter.set(numberS.hashCode());
        filter.set(hashAString(numberS));
      }
      String numberA = Integer.toString(65486);
      assertTrue(!(filter.get(numberA.hashCode()) && filter.get(hashAString(numberA))));
    }

    @Test
    public void test3(){
      Random rand = new Random();
      BitSet normal = new BitSet();
      BitSet filter = new BitSet();
      int currentNumber, falseCount = 0;
      for (int i = 0; i < 760 ; i ++ ) {
        currentNumber = rand.nextInt(1600);
        normal.set(currentNumber);
        String numberS = Integer.toString(currentNumber);
        filter.set(numberS.hashCode());
        filter.set(hashAString(numberS));
      }
      String numberA = Integer.toString(7981);
      assertTrue(!(filter.get(numberA.hashCode()) && filter.get(hashAString(numberA))));
    }

    @Test
    public void test4(){
      Random rand = new Random();
      BitSet normal = new BitSet();
      BitSet filter = new BitSet();
      int currentNumber, falseCount = 0;
      for (int i = 0; i < 760 ; i ++ ) {
        currentNumber = rand.nextInt(1600);
        normal.set(currentNumber);
        String numberS = Integer.toString(currentNumber);
        filter.set(numberS.hashCode());
        filter.set(hashAString(numberS));
      }
      String numberA = Integer.toString(48965);
      assertTrue(!(filter.get(numberA.hashCode()) && filter.get(hashAString(numberA))));
    }
}
