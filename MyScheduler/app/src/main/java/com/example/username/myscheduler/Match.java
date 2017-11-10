package com.example.username.myscheduler;


import java.util.ArrayList;

public class Match {
    public String[] isMatch(String str1) {
        String work;
        ArrayList<String> monDay = new ArrayList<String>();
        for(int month = 12;month >= 1; month--){
            for(int day = 1;day <= 31;day++){

                work = month + "月"+ day +"日";
                if(str1.indexOf(work) != -1){
                    monDay.add(work);
                    str1 = str1.replace(work, " ");
                }
            }
        }
        String[] ret = new String[monDay.size()];
        int ind = 0;
        for(String md : monDay){
            ret[ind] = md;
            ind++;
        }
        return ret;
    }
}
