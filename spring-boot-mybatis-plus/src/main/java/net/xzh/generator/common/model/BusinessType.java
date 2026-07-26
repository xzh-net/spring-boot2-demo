package net.xzh.generator.common.model;


/**
 * 审计日志操作类型
 *
 */
public enum BusinessType
{
    /**
     * 其他
     */
    OTHER(0),

    /**
     * 查询
     */
    SELECT(1),

    /**
     * 新增
     */
    INSERT(2),

    /**
     * 修改
     */
    UPDATE(3),

    /**
     * 删除
     */
    DELETE(4),

    /**
     * 上传
     */
    UPLOAD(5),

    /**
     * 下载
     */

    DOWNLOAD(6),

    /**
     * 导出
     */
    EXPORT(7),

    /**
     * 导入
     */
    IMPORT(8),

    /**
     * 强退
     */
    FORCE(9);

    private Integer type;

    private BusinessType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }

}
