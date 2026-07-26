package net.xzh.generator.common.model;

/**
 * 枚举了一些常用API操作码
 * @author xzh
 *
 */
public enum CodeEnum {
    SUCCESS(200),
    ERROR(500);

    private Integer code;
    
    CodeEnum(Integer code){
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
