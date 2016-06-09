package com.namaraka.recon.utilities;

public enum TestEnum {

    A("text1"),
    B("text2"),
    C("text3"),
    D("text4");

    private final String text;

    TestEnum(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static TestEnum fromString(String text) {
        
        if (text != null) {
            for (TestEnum b : TestEnum.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
        //return null;
    }
}
