package kr.allcll.backend.support.web;

import java.util.UUID;

public class TokenProvider {

    public static String create() {
        return UUID.randomUUID().toString();
    }

}
