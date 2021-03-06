/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved.
 */
package com.olymtech.nebula.dao.impl;

import com.olymtech.nebula.dao.INebulaPublishBaseDao;
import com.olymtech.nebula.entity.NebulaPublishBase;
import org.springframework.stereotype.Repository;

/**
 * Created by Gavin on 2015-11-03 15:44.
 */
@Repository("nebulaPublishBaseDao")
public class NebulaPublishBaseDaoImpl extends BaseDaoImpl<NebulaPublishBase,Integer> implements INebulaPublishBaseDao {

    @Override
    public void cleanBaseByEventId(Integer eventId) {
        getSqlSession().delete(CLASS_NAME + "-Delete-By-EventId", eventId);
    }

}
