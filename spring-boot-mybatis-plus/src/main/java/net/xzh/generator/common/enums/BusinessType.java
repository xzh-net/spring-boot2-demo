package net.xzh.generator.common.enums;


/**
 * 审计日志操作类型
 *
 */
public enum BusinessType
{
    OTHER(0),
    SELECT(1),
    INSERT(2),
    UPDATE(3),
    DELETE(4),
    UPLOAD(5),
    DOWNLOAD(6),
    EXPORT(7),
    IMPORT(8);

    private Integer type;

    private BusinessType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }

}