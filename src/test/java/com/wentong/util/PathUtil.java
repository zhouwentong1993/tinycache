package com.wentong.util;

public final class PathUtil {

    private PathUtil() {
    }

    public static String getSystemDefaultPath() {
        return System.getenv("DEFAULT-PATH") == null ? "/Users/renmai/IdeaProjects/tinycache" : System.getenv("DEFAULT-PATH");
    }

}
