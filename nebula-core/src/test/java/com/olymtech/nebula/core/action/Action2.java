/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved
 */
package com.olymtech.nebula.core.action;

import com.olymtech.nebula.core.action.exception.ActionException;
import com.olymtech.nebula.entity.NebulaPublishEvent;

/**
 * @author taoshanchang 15/11/4
 */
public class Action2 extends AbstractAction {

    public Action2(String actionName) {
        super.setActionName(actionName);
    }

    @Override
    public boolean doAction(NebulaPublishEvent event) throws ActionException {
        try {
            System.out.println("svn动作");
            this.nodifyObservers("svn动作执行完成");
            //int a = 12/0;
        } catch (Exception e) {

            System.out.println("Action2 Exception");
            throw new ActionException(this.getActionName(),"xxx");

        }


        return true;
    }

    @Override
    public void doFailure(NebulaPublishEvent event) {
        System.out.println("回滚svn动作");
    }

    @Override
    public boolean doCheck(NebulaPublishEvent event) throws Exception {
        return true;
    }
}
