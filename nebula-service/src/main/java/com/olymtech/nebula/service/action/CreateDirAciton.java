/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved
 */
package com.olymtech.nebula.service.action;

import com.olymtech.nebula.core.action.AbstractAction;
import com.olymtech.nebula.core.salt.ISaltStackService;
import com.olymtech.nebula.core.salt.core.SaltTarget;
import com.olymtech.nebula.entity.NebulaPublishEvent;
import com.olymtech.nebula.entity.NebulaPublishHost;
import com.olymtech.nebula.entity.NebulaPublishModule;
import com.olymtech.nebula.entity.enums.PublishAction;
import com.olymtech.nebula.entity.enums.PublishActionGroup;
import com.olymtech.nebula.service.IPublishHostService;
import com.olymtech.nebula.service.IPublishScheduleService;
import com.suse.saltstack.netapi.exception.SaltStackException;
import com.suse.saltstack.netapi.results.ResultInfo;
import com.suse.saltstack.netapi.results.ResultInfoSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author taoshanchang 15/11/4
 */

@Service
public class CreateDirAciton extends AbstractAction {

    @Autowired
    private ISaltStackService saltStackService;

    @Autowired
    private IPublishScheduleService publishScheduleService;

    @Autowired
    private IPublishHostService publishHostService;

    @Value("${base_war_dir}")
    private String BaseWarDir;
    @Value("${base_etc_dir}")
    private String BaseEtcDir;

    public CreateDirAciton() {

    }

    @Override
    public boolean doAction(NebulaPublishEvent event) throws Exception {
        publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CREATE_PUBLISH_DIR, PublishActionGroup.PRE_MINION, null ,"");

        List<NebulaPublishModule> publishModules = event.getPublishModules();

        for (NebulaPublishModule publishModule : publishModules) {
            List<NebulaPublishHost> publishHosts = publishModule.getPublishHosts();
            List<String> targes = new ArrayList<String>();
            for (NebulaPublishHost nebulaPublishHost : publishHosts) {
                targes.add(nebulaPublishHost.getPassPublishHostIp());
            }

            List<String> pathList = new ArrayList<String >();
            pathList.add(BaseWarDir + publishModule.getPublishModuleKey());
            pathList.add(BaseEtcDir + publishModule.getPublishModuleKey());

            ResultInfoSet result = saltStackService.mkDir(new SaltTarget(targes), pathList, true);

            if (result.getInfoList().size() == 1) {
                ResultInfo resultInfo = result.get(0);
                Map<String, Object> results = resultInfo.getResults();
                int i = 0;
                for (Map.Entry<String, Object> entry : results.entrySet()) {
                    NebulaPublishHost hostinfo = new NebulaPublishHost();
                    hostinfo.setActionGroup(PublishActionGroup.PRE_MINION);
                    hostinfo.setActionName(PublishAction.CREATE_PUBLISH_DIR);
                    hostinfo.setPassPublishHostName(publishHosts.get(i++).getPassPublishHostName());
                    hostinfo.setPublishModuleId(publishModule.getId());
                    hostinfo.setPassPublishHostIp(entry.getKey());
                    hostinfo.setPublishEventId(event.getId());
                    if (entry.getValue().equals("")) {
                        hostinfo.setActionResult("success");
                        hostinfo.setIsSuccessAction(true);
                        publishHostService.createPublishHost(hostinfo);
                    } else {
                        hostinfo.setActionResult("failure");
                        hostinfo.setIsSuccessAction(false);
                        publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CREATE_PUBLISH_DIR, PublishActionGroup.PRE_MINION, false ,"error message");
                        throw new SaltStackException(entry.getValue().toString());
                    }
                }

            } else {
                publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CREATE_PUBLISH_DIR, PublishActionGroup.PRE_MINION, false ,"error message");
                return false;
            }
        }
        publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CREATE_PUBLISH_DIR, PublishActionGroup.PRE_MINION, true ,"");
        return true;
    }

    @Override
    public void doFailure(NebulaPublishEvent event) {

    }
}
