package org.jfree.chart;


//Hold code for now to be used in run-time analysis
public class TimeTest1 {
   public static void main(String[] args) {

      long startTime = System.currentTimeMillis();

      long total = 0;
      for (int i = 0; i < 10000000; i++) {
         total += i;
      }

      long stopTime = System.currentTimeMillis();
      long elapsedTime = stopTime - startTime;
      System.out.println(elapsedTime);
   }
}

