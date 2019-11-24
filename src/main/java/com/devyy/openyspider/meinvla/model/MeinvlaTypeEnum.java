package com.devyy.openyspider.meinvla.model;

public enum MeinvlaTypeEnum {

    TYPE_88("type88",44,"国产美女"),

    TYPE_180("type180",32,"网红福利美女"),

    TYPE_184("type184",9,"大西瓜美女"),

    TYPE_186("type186",5,"纳丝摄影"),

    TYPE_191("type191", 5, "战前女神"),

    ;


    private String type;
    private Integer pageNum;
    private String desc;

    MeinvlaTypeEnum(String type, Integer pageNum, String desc) {
        this.type = type;
        this.pageNum = pageNum;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public String getDesc() {
        return desc;
    }
}
