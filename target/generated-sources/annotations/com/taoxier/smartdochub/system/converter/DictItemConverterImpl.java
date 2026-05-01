package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.model.Option;
import com.taoxier.smartdochub.system.model.entity.DictItem;
import com.taoxier.smartdochub.system.model.form.DictItemForm;
import com.taoxier.smartdochub.system.model.vo.DictPageVO;
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
public class DictItemConverterImpl implements DictItemConverter {

    @Override
    public Page<DictPageVO> toPageVo(Page<DictItem> page) {
        if ( page == null ) {
            return null;
        }

        Page<DictPageVO> page1 = new Page<DictPageVO>();

        page1.setPages( page.getPages() );
        page1.setRecords( dictItemListToDictPageVOList( page.getRecords() ) );
        page1.setTotal( page.getTotal() );
        page1.setSize( page.getSize() );
        page1.setCurrent( page.getCurrent() );

        return page1;
    }

    @Override
    public DictItemForm toForm(DictItem entity) {
        if ( entity == null ) {
            return null;
        }

        DictItemForm dictItemForm = new DictItemForm();

        dictItemForm.setDictCode( entity.getDictCode() );
        dictItemForm.setId( entity.getId() );
        dictItemForm.setLabel( entity.getLabel() );
        dictItemForm.setSort( entity.getSort() );
        dictItemForm.setStatus( entity.getStatus() );
        dictItemForm.setTagType( entity.getTagType() );
        dictItemForm.setValue( entity.getValue() );

        return dictItemForm;
    }

    @Override
    public DictItem toEntity(DictItemForm formFata) {
        if ( formFata == null ) {
            return null;
        }

        DictItem dictItem = new DictItem();

        dictItem.setId( formFata.getId() );
        dictItem.setDictCode( formFata.getDictCode() );
        dictItem.setLabel( formFata.getLabel() );
        dictItem.setSort( formFata.getSort() );
        dictItem.setStatus( formFata.getStatus() );
        dictItem.setTagType( formFata.getTagType() );
        dictItem.setValue( formFata.getValue() );

        return dictItem;
    }

    @Override
    public Option<Long> toOption(DictItem dictItem) {
        if ( dictItem == null ) {
            return null;
        }

        Option<Long> option = new Option<Long>();

        option.setLabel( dictItem.getLabel() );
        if ( dictItem.getValue() != null ) {
            option.setValue( Long.parseLong( dictItem.getValue() ) );
        }

        return option;
    }

    @Override
    public List<Option<Long>> toOption(List<DictItem> dictData) {
        if ( dictData == null ) {
            return null;
        }

        List<Option<Long>> list = new ArrayList<Option<Long>>( dictData.size() );
        for ( DictItem dictItem : dictData ) {
            list.add( toOption( dictItem ) );
        }

        return list;
    }

    protected DictPageVO dictItemToDictPageVO(DictItem dictItem) {
        if ( dictItem == null ) {
            return null;
        }

        DictPageVO dictPageVO = new DictPageVO();

        dictPageVO.setDictCode( dictItem.getDictCode() );
        dictPageVO.setId( dictItem.getId() );
        dictPageVO.setStatus( dictItem.getStatus() );

        return dictPageVO;
    }

    protected List<DictPageVO> dictItemListToDictPageVOList(List<DictItem> list) {
        if ( list == null ) {
            return null;
        }

        List<DictPageVO> list1 = new ArrayList<DictPageVO>( list.size() );
        for ( DictItem dictItem : list ) {
            list1.add( dictItemToDictPageVO( dictItem ) );
        }

        return list1;
    }
}
