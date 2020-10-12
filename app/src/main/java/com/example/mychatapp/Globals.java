package com.example.mychatapp;

public class Globals{
    private static Globals instance;

    // Global variable
    private String data;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public void setData(String d){
        this.data=d;
    }
    public String getData(){
        return this.data;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }


}


