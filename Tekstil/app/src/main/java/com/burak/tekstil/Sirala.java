package com.burak.tekstil;

import java.util.Comparator;

public class Sirala implements Comparator<oy_adapter> {

    public int compare(oy_adapter one, oy_adapter another){
        int returnVal = 0;

        int cuval_miktar_one = Integer.parseInt(one.getmCuvalMiktar());
        int cuval_secimi_one = Integer.parseInt(one.getmCuvalSecimi());
        int oy_one = Integer.parseInt(one.getmKacOy());

        int katSa_one = 1;

        if (one.getmUnvani().equals("Singer")) {
            katSa_one = 1;
        } else if (one.getmUnvani().equals("Overlok")){
            katSa_one = 2;
        } else if (one.getmUnvani().equals("Orta")){
            katSa_one = 2;
        } else if (one.getmUnvani().equals("Reçme")){
            katSa_one = 3;
        }

        int puan_one = cuval_miktar_one * cuval_secimi_one * katSa_one;


        oy_one *= 10;

        puan_one += oy_one;

        int cuval_miktar_another = Integer.parseInt(another.getmCuvalMiktar());
        int cuval_secimi_another = Integer.parseInt(another.getmCuvalSecimi());
        int oy_another = Integer.parseInt(another.getmKacOy());

        int katSa_another = 1;

        if (another.getmUnvani().equals("Singer")) {
            katSa_another = 1;
        } else if (another.getmUnvani().equals("Overlok")){
            katSa_another = 2;
        } else if (another.getmUnvani().equals("Orta")){
            katSa_another = 2;
        } else if (another.getmUnvani().equals("Reçme")){
            katSa_another = 3;
        }

        int puan_another = cuval_miktar_another * cuval_secimi_another * katSa_another;


        oy_another *= 10;

        puan_another += oy_another;
// puan sıralamalarına bakıyorum hangisi hangisinde küçük büyük falan
        if(puan_one < puan_another){
            returnVal =  1;
        }else if(puan_one > puan_another){
            returnVal =  -1;
        }else if(puan_one == puan_another){
            returnVal =  0;
        }
        return returnVal;
    }
}