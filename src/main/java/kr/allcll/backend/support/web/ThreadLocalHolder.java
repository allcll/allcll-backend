package kr.allcll.backend.support.web;

public class ThreadLocalHolder {

    public static final ThreadLocal<String> SHARED_TOKEN = new ThreadLocal<>();

}
