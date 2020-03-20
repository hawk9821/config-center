package com.hawk.spring.configCenterTest.entity;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 * 初始化redis: hmset redis_config_center xxx.name Hawk xxx.age 29 xxx.sex 1
 */
public class User {

    private String name;
    private String age;
    private String sex;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
