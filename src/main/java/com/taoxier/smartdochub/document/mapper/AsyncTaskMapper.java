package com.taoxier.smartdochub.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taoxier.smartdochub.document.model.entity.AsyncTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 异步任务表 Mapper 接口
 * </p>
 *
 * @author taoxier
 * @since 2026-04-11
 */
@Mapper
public interface AsyncTaskMapper extends BaseMapper<AsyncTask> {

}
