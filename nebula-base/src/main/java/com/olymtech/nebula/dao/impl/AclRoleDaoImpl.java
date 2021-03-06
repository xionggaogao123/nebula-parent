/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved.
 */
package com.olymtech.nebula.dao.impl;

import com.olymtech.nebula.dao.IAclRoleDao;
import com.olymtech.nebula.entity.AclRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Gavin on 2015-11-10 00:50.
 */
@Repository
public class AclRoleDaoImpl extends BaseDaoImpl<AclRole,Integer> implements IAclRoleDao {

    @Override
    public List<AclRole> selectByIds(List<Integer> ids) {
        return getSqlSession().selectList(CLASS_NAME + "-Select-By-Ids", ids);
    }

}
