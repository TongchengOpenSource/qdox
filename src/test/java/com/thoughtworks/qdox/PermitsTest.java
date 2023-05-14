package com.thoughtworks.qdox;


import org.junit.Test;

import java.io.StringReader;

public class PermitsTest {
    private JavaProjectBuilder builder = new JavaProjectBuilder();

    @Test
    public void permitsAsTypeAndIdentifiers() {
        String source = "package permits.permits.permits;\n"
                + "\n"
                + "public class permits\n"
                + "{\n"
                + "    private Object permits;\n"
                + "    \n"
                + "    public permits() {\n"
                + "    }\n"
                + "    \n"
                + "    private Object permits(Object permits) {\n"
                + "        return permits;\n"
                + "    }\n"
                + "}";
        builder.addSource( new StringReader(source) );
    }
}
