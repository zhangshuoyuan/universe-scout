package cn.yuan.scout.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页返回体。
 *
 * @param <T> 分页记录类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;

    private Long total;

    private Long pageNum;

    private Long pageSize;

    private Long pages;

    public static <T> PageResult<T> of(List<T> records, Long total, Long pageNum, Long pageSize) {
        long safeTotal = total == null ? 0L : total;
        long safePageSize = pageSize == null ? 0L : pageSize;
        long pages = safePageSize == 0L ? 0L : (safeTotal + safePageSize - 1) / safePageSize;

        return new PageResult<>(
                records == null ? Collections.emptyList() : records,
                safeTotal,
                pageNum == null ? 1L : pageNum,
                safePageSize,
                pages
        );
    }

    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return of(Collections.emptyList(), 0L, pageNum, pageSize);
    }
}
