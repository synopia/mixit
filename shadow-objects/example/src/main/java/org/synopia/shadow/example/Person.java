package org.synopia.shadow.example;

import org.synopia.shadow.Parameter;

/**
 * Created by synopia on 04.10.2015.
 */
public class Person implements SPerson{
    @Parameter
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
