package com.taoxier.smartdochub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.bo.NoticeBO;
import com.taoxier.smartdochub.system.model.entity.Notice;
import com.taoxier.smartdochub.system.model.query.NoticePageQuery;
import com.taoxier.smartdochub.system.model.vo.NoticePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知公告Mapper接口
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 获取通知公告分页数据
     *
     * @param page 分页对象
     * @param queryParams 查询参数
     * @return 通知公告分页数据
     */
    Page<NoticeBO> getNoticePage(Page<NoticePageVO> page, NoticePageQuery queryParams);

    /**
     * 获取阅读时通知公告详情
     *
     * @param id 通知公告ID
     * @return 通知公告详情
     */
    NoticeBO getNoticeDetail(@Param("id") Long id);
}
