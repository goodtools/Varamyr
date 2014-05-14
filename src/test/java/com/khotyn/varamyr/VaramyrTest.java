package com.khotyn.varamyr;

public class VaramyrTest {

    @org.junit.Test
    public void testParse() throws Exception {

        Varamyr.parse("test.epub");


    }

    @org.junit.Test
    public void testGetCss() throws Exception {

        System.out.println( new String( Varamyr.getCss()));

    }
}