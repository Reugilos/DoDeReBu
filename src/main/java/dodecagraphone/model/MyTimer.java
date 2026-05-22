/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model;

/**
 * [CA] Temporitzador de precisió basat en {@code System.nanoTime()} per a la reproducció.
 * Conté el camp {@code start} (volatile) per mesurar el temps transcorregut.
 * La implementació activa dels mètodes està comentada; la classe actua com a
 * marcador de posició per a futures extensions.
 * <p>
 * [EN] Precision timer based on {@code System.nanoTime()} for playback.
 * Contains the volatile {@code start} field to measure elapsed time.
 * The active implementation of the methods is commented out; the class acts as
 * a placeholder for future extensions.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
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
