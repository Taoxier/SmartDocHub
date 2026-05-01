package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.system.model.entity.Dict;
import com.taoxier.smartdochub.system.model.form.DictForm;
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
public class DictConverterImpl implements DictConverter {

    @Override
    public Page<DictPageVO> toPageVo(Page<Dict> page) {
        if ( page == null ) {
            return null;
        }

        Page<DictPageVO> page1 = new Page<DictPageVO>();

        page1.setPages( page.getPages() );
        page1.setRecords( dictListToDictPageVOList( page.getRecords() ) );
        page1.setTotal( page.getTotal() );
        page1.setSize( page.getSize() );
        page1.setCurrent( page.getCurrent() );

        return page1;
    }

    @Override
    public DictForm toForm(Dict entity) {
        if ( entity == null ) {
            return null;
        }

        DictForm dictForm = new DictForm();

        dictForm.setDictCode( entity.getDictCode() );
        dictForm.setId( entity.getId() );
        dictForm.setName( entity.getName() );
        dictForm.setRemark( entity.getRemark() );
        dictForm.setStatus( entity.getStatus() );

        return dictForm;
    }

    @Override
    public Dict toEntity(DictForm entity) {
        if ( entity == null ) {
            return null;
        }

        Dict dict = new Dict();

        dict.setId( entity.getId() );
        dict.setDictCode( entity.getDictCode() );
        dict.setName( entity.getName() );
        dict.setRemark( entity.getRemark() );
        dict.setStatus( entity.getStatus() );

        return dict;
    }

    protected DictPageVO dictToDictPageVO(Dict dict) {
        if ( dict == null ) {
            return null;
        }

        DictPageVO dictPageVO = new DictPageVO();

        dictPageVO.setDictCode( dict.getDictCode() );
        dictPageVO.setId( dict.getId() );
        dictPageVO.setName( dict.getName() );
        dictPageVO.setStatus( dict.getStatus() );

        return dictPageVO;
    }

    protected List<DictPageVO> dictListToDictPageVOList(List<Dict> list) {
        if ( list == null ) {
            return null;
        }

        List<DictPageVO> list1 = new ArrayList<DictPageVO>( list.size() );
        for ( Dict dict : list ) {
            list1.add( dictToDictPageVO( dict ) );
        }

        return list1;
    }
}
