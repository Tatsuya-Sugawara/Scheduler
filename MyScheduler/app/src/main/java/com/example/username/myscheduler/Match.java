package com.example.username.myscheduler;


import java.util.ArrayList;

public class Match{


    public String isMatch(String str1, String year, String onDate) {
        str1 = str1.replaceAll(" ー ","1");
        str1 = str1.replaceAll("ー ","1");
        str1 = str1.replaceAll(" ー","1");
        str1 = str1.replaceAll("ー","1");
        str1 = str1.replaceAll(" 一 ","1");
        str1 = str1.replaceAll(" 一","1");
        str1 = str1.replaceAll("一 ","1");
        str1 = str1.replaceAll("一","1");


        String work;
        ArrayList<String> monDay = new ArrayList<String>();
        for(int month = 12;month >= 1; month--){
            for(int day = 1;day <= 31;day++){

                work = month + "月"+ day + "日";
                if(str1.indexOf(work) != -1){
                    str1 = str1.replace(work, " ");
                    monDay.add("/" + month + "/" + day);
                }
            }
        }
        if(monDay.size() != 0){
            String[] ret = new String[monDay.size()];
            int ind = 0;
            String mdWork;
            System.out.println();
            for(String md : monDay){
                mdWork = md;
                ret[ind] = year + mdWork;

                ind++;
            }
            System.out.println("ret[0]" + ret[0]);
            return ret[0];

        }else {
            System.out.println("onDate" + onDate);


            return onDate;

        }
    }
}
