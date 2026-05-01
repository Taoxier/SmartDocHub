package com.taoxier.smartdochub.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taoxier.smartdochub.common.model.Option;
import com.taoxier.smartdochub.system.model.bo.UserBO;
import com.taoxier.smartdochub.system.model.dto.CurrentUserDTO;
import com.taoxier.smartdochub.system.model.dto.UserImportDTO;
import com.taoxier.smartdochub.system.model.entity.User;
import com.taoxier.smartdochub.system.model.form.UserForm;
import com.taoxier.smartdochub.system.model.form.UserProfileForm;
import com.taoxier.smartdochub.system.model.vo.UserPageVO;
import com.taoxier.smartdochub.system.model.vo.UserProfileVO;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T00:57:58+0800",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserConverterImpl implements UserConverter {

    @Override
    public UserPageVO toPageVo(UserBO bo) {
        if ( bo == null ) {
            return null;
        }

        UserPageVO userPageVO = new UserPageVO();

        userPageVO.setAvatar( bo.getAvatar() );
        userPageVO.setCreateTime( bo.getCreateTime() );
        userPageVO.setDeptName( bo.getDeptName() );
        userPageVO.setEmail( bo.getEmail() );
        userPageVO.setGender( bo.getGender() );
        userPageVO.setId( bo.getId() );
        userPageVO.setMobile( bo.getMobile() );
        userPageVO.setNickname( bo.getNickname() );
        userPageVO.setRoleNames( bo.getRoleNames() );
        userPageVO.setStatus( bo.getStatus() );
        userPageVO.setUsername( bo.getUsername() );

        return userPageVO;
    }

    @Override
    public Page<UserPageVO> toPageVo(Page<UserBO> bo) {
        if ( bo == null ) {
            return null;
        }

        Page<UserPageVO> page = new Page<UserPageVO>();

        page.setPages( bo.getPages() );
        page.setRecords( userBOListToUserPageVOList( bo.getRecords() ) );
        page.setTotal( bo.getTotal() );
        page.setSize( bo.getSize() );
        page.setCurrent( bo.getCurrent() );

        return page;
    }

    @Override
    public UserForm toForm(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserForm userForm = new UserForm();

        userForm.setAvatar( entity.getAvatar() );
        userForm.setDeptId( entity.getDeptId() );
        userForm.setEmail( entity.getEmail() );
        userForm.setGender( entity.getGender() );
        userForm.setId( entity.getId() );
        userForm.setMobile( entity.getMobile() );
        userForm.setNickname( entity.getNickname() );
        userForm.setStatus( entity.getStatus() );
        userForm.setUsername( entity.getUsername() );

        return userForm;
    }

    @Override
    public User toEntity(UserForm entity) {
        if ( entity == null ) {
            return null;
        }

        User user = new User();

        user.setId( entity.getId() );
        user.setAvatar( entity.getAvatar() );
        user.setDeptId( entity.getDeptId() );
        user.setEmail( entity.getEmail() );
        user.setGender( entity.getGender() );
        user.setMobile( entity.getMobile() );
        user.setNickname( entity.getNickname() );
        user.setStatus( entity.getStatus() );
        user.setUsername( entity.getUsername() );

        return user;
    }

    @Override
    public CurrentUserDTO toCurrentUserDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        CurrentUserDTO currentUserDTO = new CurrentUserDTO();

        currentUserDTO.setUserId( entity.getId() );
        currentUserDTO.setAvatar( entity.getAvatar() );
        currentUserDTO.setNickname( entity.getNickname() );
        currentUserDTO.setUsername( entity.getUsername() );

        return currentUserDTO;
    }

    @Override
    public User toEntity(UserImportDTO vo) {
        if ( vo == null ) {
            return null;
        }

        User user = new User();

        user.setEmail( vo.getEmail() );
        user.setMobile( vo.getMobile() );
        user.setNickname( vo.getNickname() );
        user.setUsername( vo.getUsername() );

        return user;
    }

    @Override
    public UserProfileVO toProfileVo(UserBO bo) {
        if ( bo == null ) {
            return null;
        }

        UserProfileVO userProfileVO = new UserProfileVO();

        userProfileVO.setAvatar( bo.getAvatar() );
        if ( bo.getCreateTime() != null ) {
            userProfileVO.setCreateTime( Date.from( bo.getCreateTime().toInstant( ZoneOffset.UTC ) ) );
        }
        userProfileVO.setDeptName( bo.getDeptName() );
        userProfileVO.setEmail( bo.getEmail() );
        userProfileVO.setGender( bo.getGender() );
        userProfileVO.setId( bo.getId() );
        userProfileVO.setMobile( bo.getMobile() );
        userProfileVO.setNickname( bo.getNickname() );
        userProfileVO.setRoleNames( bo.getRoleNames() );
        userProfileVO.setUsername( bo.getUsername() );

        return userProfileVO;
    }

    @Override
    public User toEntity(UserProfileForm formData) {
        if ( formData == null ) {
            return null;
        }

        User user = new User();

        user.setId( formData.getId() );
        user.setAvatar( formData.getAvatar() );
        user.setEmail( formData.getEmail() );
        user.setGender( formData.getGender() );
        user.setMobile( formData.getMobile() );
        user.setNickname( formData.getNickname() );
        user.setUsername( formData.getUsername() );

        return user;
    }

    @Override
    public Option<String> toOption(User entity) {
        if ( entity == null ) {
            return null;
        }

        Option<String> option = new Option<String>();

        option.setLabel( entity.getNickname() );
        if ( entity.getId() != null ) {
            option.setValue( String.valueOf( entity.getId() ) );
        }

        return option;
    }

    @Override
    public List<Option<String>> toOptions(List<User> list) {
        if ( list == null ) {
            return null;
        }

        List<Option<String>> list1 = new ArrayList<Option<String>>( list.size() );
        for ( User user : list ) {
            list1.add( toOption( user ) );
        }

        return list1;
    }

    protected List<UserPageVO> userBOListToUserPageVOList(List<UserBO> list) {
        if ( list == null ) {
            return null;
        }

        List<UserPageVO> list1 = new ArrayList<UserPageVO>( list.size() );
        for ( UserBO userBO : list ) {
            list1.add( toPageVo( userBO ) );
        }

        return list1;
    }
}
