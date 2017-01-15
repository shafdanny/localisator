package com.polytech.mathieu.localisator1.data;

import java.util.UUID;

/**
 * Created by shafiq on 14/01/2017.
 */

public class IdGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
