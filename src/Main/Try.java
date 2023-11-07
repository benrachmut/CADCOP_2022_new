package Main;

import java.util.Random;

public class Try {
    public static void main(String[] args) {

        Random randomCost = new Random();

        for (int i = 0; i <10000 ; i++) {
            double Z = randomCost.nextGaussian();

            int ans  = (int) (Z*10+(100));
            if (ans<0){
                ans = 0;
            }
            System.out.println(ans);

        }
    }
}
