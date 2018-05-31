package com.tech.gigabyte.navigation;

/*
 * Created by GIGABYTE on 7/26/2017.
 *
 */


class Distance {
    public String text;
    private int value;

    Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
