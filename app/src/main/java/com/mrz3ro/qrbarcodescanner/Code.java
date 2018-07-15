package com.mrz3ro.qrbarcodescanner;

public class Code {

    public Long _id;
    public String name;
    public String type;


    public Code() {
        this.name = "noName";
        this.type = "noType";
    }

    public Code(String name, String type) {
        this.name = name;
        this.type = type;
    }

}
