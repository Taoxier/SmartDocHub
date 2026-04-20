package com.taoxier.smartdochub.document.mapper;

import com.taoxier.smartdochub.document.model.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    @Update("UPDATE doc_document SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(Long id);

    @Update("UPDATE doc_document SET download_count = download_count + 1 WHERE id = #{id}")
    void incrementDownloadCount(Long id);
}
