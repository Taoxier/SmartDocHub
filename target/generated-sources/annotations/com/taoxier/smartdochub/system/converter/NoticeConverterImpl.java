package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.bo.NoticeBO;
import com.taoxier.smartdochub.system.model.entity.Notice;
import com.taoxier.smartdochub.system.model.form.NoticeForm;
import com.taoxier.smartdochub.system.model.vo.NoticeDetailVO;
import com.taoxier.smartdochub.system.model.vo.NoticePageVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T00:57:59+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class NoticeConverterImpl implements NoticeConverter {

    @Override
    public NoticeForm toForm(Notice entity) {
        if ( entity == null ) {
            return null;
        }

        NoticeForm noticeForm = new NoticeForm();

        noticeForm.setContent( entity.getContent() );
        noticeForm.setId( entity.getId() );
        noticeForm.setLevel( entity.getLevel() );
        noticeForm.setTargetType( entity.getTargetType() );
        noticeForm.setTitle( entity.getTitle() );
        noticeForm.setType( entity.getType() );

        noticeForm.setTargetUserIds( cn.hutool.core.util.StrUtil.split(entity.getTargetUserIds(),",") );

        return noticeForm;
    }

    @Override
    public Notice toEntity(NoticeForm formData) {
        if ( formData == null ) {
            return null;
        }

        Notice notice = new Notice();

        notice.setId( formData.getId() );
        notice.setContent( formData.getContent() );
        notice.setLevel( formData.getLevel() );
        notice.setTargetType( formData.getTargetType() );
        notice.setTitle( formData.getTitle() );
        notice.setType( formData.getType() );

        notice.setTargetUserIds( cn.hutool.core.collection.CollUtil.join(formData.getTargetUserIds(),",") );

        return notice;
    }

    @Override
    public NoticePageVO toPageVo(NoticeBO bo) {
        if ( bo == null ) {
            return null;
        }

        NoticePageVO noticePageVO = new NoticePageVO();

        noticePageVO.setCreateTime( bo.getCreateTime() );
        noticePageVO.setId( bo.getId() );
        noticePageVO.setLevel( bo.getLevel() );
        noticePageVO.setPublishStatus( bo.getPublishStatus() );
        noticePageVO.setPublishTime( bo.getPublishTime() );
        noticePageVO.setPublisherName( bo.getPublisherName() );
        noticePageVO.setRevokeTime( bo.getRevokeTime() );
        noticePageVO.setTargetType( bo.getTargetType() );
        noticePageVO.setTitle( bo.getTitle() );
        noticePageVO.setType( bo.getType() );

        return noticePageVO;
    }

    @Override
    public Page<NoticePageVO> toPageVo(Page<NoticeBO> noticePage) {
        if ( noticePage == null ) {
            return null;
        }

        Page<NoticePageVO> page = new Page<NoticePageVO>();

        page.setPages( noticePage.getPages() );
        page.setRecords( noticeBOListToNoticePageVOList( noticePage.getRecords() ) );
        page.setTotal( noticePage.getTotal() );
        page.setSize( noticePage.getSize() );
        page.setCurrent( noticePage.getCurrent() );

        return page;
    }

    @Override
    public NoticeDetailVO toDetailVO(NoticeBO noticeBO) {
        if ( noticeBO == null ) {
            return null;
        }

        NoticeDetailVO noticeDetailVO = new NoticeDetailVO();

        noticeDetailVO.setContent( noticeBO.getContent() );
        noticeDetailVO.setId( noticeBO.getId() );
        noticeDetailVO.setLevel( noticeBO.getLevel() );
        noticeDetailVO.setPublishStatus( noticeBO.getPublishStatus() );
        noticeDetailVO.setPublishTime( noticeBO.getPublishTime() );
        noticeDetailVO.setPublisherName( noticeBO.getPublisherName() );
        noticeDetailVO.setTitle( noticeBO.getTitle() );
        noticeDetailVO.setType( noticeBO.getType() );

        return noticeDetailVO;
    }

    protected List<NoticePageVO> noticeBOListToNoticePageVOList(List<NoticeBO> list) {
        if ( list == null ) {
            return null;
        }

        List<NoticePageVO> list1 = new ArrayList<NoticePageVO>( list.size() );
        for ( NoticeBO noticeBO : list ) {
            list1.add( toPageVo( noticeBO ) );
        }

        return list1;
    }
}
