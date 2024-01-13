package com.thoughtworks.qdox;




import org.junit.jupiter.api.Test;

import java.io.StringReader;

public class PermitsTest {
    private JavaProjectBuilder builder = new JavaProjectBuilder();

    @Test
    public void permitsAsTypeAndIdentifiers() {
        String source = "public class MyPermits {\n" +
                "\n" +
                "    private Object permits;\n" +
                "\n" +
                "    public MyPermits(){}\n" +
                "\n" +
                "    public MyPermits(Object permits) {\n" +
                "        this.permits = permits;\n" +
                "    }\n" +
                "\n" +
                "    public Object getPermits() {\n" +
                "        return permits;\n" +
                "    }\n" +
                "\n" +
                "    public void setPermits(Object permits) {\n" +
                "        this.permits = permits;\n" +
                "    }\n" +
                "}";
        builder.addSource( new StringReader(source) );
    }
}
