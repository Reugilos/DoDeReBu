///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
package dodecagraphone.model;
//
///**
// * Elapsed time.
// * @author upcnet
// */
//public class MyTimer {
//    private volatile long yesterday;
//    public MyTimer(){
//        this.yesterday = System.nanoTime();
//    }
//    public void reset(){
//        this.yesterday = System.nanoTime();
//    }
//    public long elapsed(){
//        long elapsed = System.nanoTime()-yesterday;
//        // if (elapsed>100) System.out.println("MyTimer::elapsed, elapsed = "+elapsed);
//        return elapsed;
//    }
//    public static void pause(long millis){
//        long now = System.nanoTime();
//        while ((System.nanoTime()-now)<millis);
//    }
//}
// 
public class MyTimer {
    private volatile long start;

//    public MyTimer() {
//        reset();
//    }
//
//    public void reset() {
//        this.start = System.nanoTime();
//    }
//
//    public long elapsedNanos() {
//        return System.nanoTime() - start;
//    }
//
////    public long elapsedMillis() {
////        return elapsedNanos() / 1_000_000;
////    }
////
//    public static void pauseUntil(long targetTimeNanos) {
//        long now = System.nanoTime();
//        long waitTime = targetTimeNanos - now;
//        if (waitTime > 0) {
//            try {
//                Thread.sleep(waitTime / 1_000_000, (int) (waitTime % 1_000_000));
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt(); // Best practice
//            }
//        }
//    }
}
