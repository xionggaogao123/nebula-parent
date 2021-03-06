/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved
 */
package com.olymtech.nebula.service.action;

import com.olymtech.nebula.core.action.AbstractAction;
import com.olymtech.nebula.core.salt.ISaltStackService;
import com.olymtech.nebula.dao.INebulaPublishModuleDao;
import com.olymtech.nebula.entity.NebulaPublishEvent;
import com.olymtech.nebula.entity.NebulaPublishHost;
import com.olymtech.nebula.entity.NebulaPublishModule;
import com.olymtech.nebula.entity.enums.HostPublishStatus;
import com.olymtech.nebula.entity.enums.PublishAction;
import com.olymtech.nebula.entity.enums.PublishActionGroup;
import com.olymtech.nebula.service.IPublishHostService;
import com.olymtech.nebula.service.IPublishScheduleService;
import com.suse.saltstack.netapi.results.ResultInfo;
import com.suse.saltstack.netapi.results.ResultInfoSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author taoshanchang 15/11/6
 */
@Service
public class ChangeLnAction extends AbstractAction {

    @Autowired
    private ISaltStackService saltStackService;
    @Autowired
    private IPublishScheduleService publishScheduleService;
    @Autowired
    private IPublishHostService publishHostService;
    @Autowired
    private INebulaPublishModuleDao publishModuleDao;

    @Value("${base_war_dir}")
    private String BaseWarDir;
    @Value("${base_etc_dir}")
    private String BaseEtcDir;
    @Value("${etc_link}")
    private String EtcLink;
    @Value("${war_link}")
    private String WarLink;


    @Override
    public boolean doAction(NebulaPublishEvent event) throws Exception {
        publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CHANGE_LN, event.getPublishActionGroup(), null, "");

        List<NebulaPublishModule> publishModules = event.getPublishModules();

        for (NebulaPublishModule publishModule : publishModules) {
            if(event.getNowBatchTag()<=publishModule.getBatchTotal()){
                /** 更新当前批次 */
                publishModule.setNowBatchTag(event.getNowBatchTag());
                publishModuleDao.updateByIdSelective(publishModule);
            }else {
                continue;
            }

            List<NebulaPublishHost> publishHosts = publishModule.getPublishHosts();
            List<String> targets = new ArrayList<String>();
            for (NebulaPublishHost nebulaPublishHost : publishHosts) {
                if(nebulaPublishHost.getBatchTag().equals(event.getNowBatchTag())){
                    targets.add(nebulaPublishHost.getPassPublishHostIp());
                }
            }

            HashMap<String, String> lnMap = new HashMap<String, String>();
            lnMap.put(BaseWarDir + publishModule.getPublishModuleKey(), WarLink);
            lnMap.put(BaseEtcDir + publishModule.getPublishModuleKey() + "/etc", EtcLink);
            ResultInfoSet result = saltStackService.makeLn(targets, lnMap);

            ResultInfo resultInfo = result.get(0);
            Map<String, Object> results = resultInfo.getResults();
            int i = 0;
            int successCount = 0;
            for (Map.Entry<String, Object> entry : results.entrySet()) {
                NebulaPublishHost nebulaPublishHost = publishHosts.get(i++);
                nebulaPublishHost.setActionGroup(PublishActionGroup.PUBLISH_REAL);
                nebulaPublishHost.setActionName(PublishAction.CHANGE_LN);
                if (entry.getValue().equals("")) {
                    nebulaPublishHost.setActionResult("do change ln action success");
                    nebulaPublishHost.setIsSuccessAction(true);
                    nebulaPublishHost.setHostPublishStatus(HostPublishStatus.PUBLISHING);
                    publishHostService.updatePublishHost(nebulaPublishHost);
                    successCount++;
                } else {
                    nebulaPublishHost.setActionResult(entry.getValue().toString());
                    nebulaPublishHost.setIsSuccessAction(false);
                    nebulaPublishHost.setHostPublishStatus(HostPublishStatus.FAILED);
                    publishHostService.updatePublishHost(nebulaPublishHost);
                }
            }
            if (successCount!= targets.size()){
                publishScheduleService.logScheduleByAction(
                        event.getId(),
                        PublishAction.CHANGE_LN,
                        event.getPublishActionGroup(),
                        false,
                        "实际成功'更改链接'主机数:" + successCount + ",  目标'更改链接'主机数:" + targets.size()
                );
                return false;
            }

        }
        publishScheduleService.logScheduleByAction(event.getId(), PublishAction.CHANGE_LN, event.getPublishActionGroup(), true, "All models and sub targes 'execute' success");
        return true;
    }

    @Override
    public void doFailure(NebulaPublishEvent event) {

    }

    @Override
    public boolean doCheck(NebulaPublishEvent event) throws Exception {
        return true;
    }
}
