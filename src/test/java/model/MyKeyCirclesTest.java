package model;

import dodecagraphone.model.MyKeyCircles;

/**
 *
 * @author grogm
 */
public class MyKeyCirclesTest {
    public static void main(String[] args){
        String key,rel;
        System.out.println(key = MyKeyCircles.firstm());
        for (int i=1;i<20;i++){
            key = MyKeyCircles.next(key);
            rel = MyKeyCircles.relativeKey(key);
            System.out.println(key+" "+rel);
        }
    }
}
