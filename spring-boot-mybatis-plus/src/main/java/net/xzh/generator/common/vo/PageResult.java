package net.xzh.generator.common.vo;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.xzh.generator.common.utils.Convert;


/**
 * 通用分页结果类，用于封装分页查询后的数据
 *
 * @param <T> 泛型类型，表示分页数据中元素的类型
 * @author xzh
 * @date 2021年10月05日
 */
public class PageResult<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPage;
    private Long total;
    private List<T> list;

    public static <T> PageResult<T> restPage(Page<T> pageResult) {
    	PageResult<T> result = new PageResult<>();
        result.setPageNum(Convert.toInt(pageResult.getCurrent()));
        result.setPageSize(Convert.toInt(pageResult.getSize()));
        result.setTotal(pageResult.getTotal());
        result.setTotalPage(Convert.toInt(pageResult.getTotal() / pageResult.getSize() + 1));
        result.setList(pageResult.getRecords());
        return result;
    }

    public static <T> PageResult<T> restPage(IPage<?> pageResult, List<T> records) {
    	PageResult<T> result = createPageResult(pageResult);
        result.setList(records);
        return result;
    }

    public static <T> PageResult<T> createPageResult(IPage<?> page) {
    	PageResult<T> result = new PageResult<>();
        result.setPageNum(Convert.toInt(page.getCurrent()));
        result.setPageSize(Convert.toInt(page.getSize()));
        result.setTotal(page.getTotal());
        result.setTotalPage(Convert.toInt(page.getTotal() / page.getSize() + 1));
        return result;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}