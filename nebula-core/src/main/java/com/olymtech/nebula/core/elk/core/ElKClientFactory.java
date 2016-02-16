/**
 * Olymtech.com Inc.
 * Copyright (c) 2002-2015 All Rights Reserved.
 */
package com.olymtech.nebula.core.elk.core;

import com.olymtech.nebula.common.utils.DateUtils;
import com.olymtech.nebula.common.utils.EsUtils;
import com.olymtech.nebula.entity.ElkSearchData;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by Gavin on 2016-01-28 15:45.
 */
@Service
public class ElKClientFactory {

    private static Logger logger = LoggerFactory.getLogger(ElKClientFactory.class);

    private static TransportClient client = null;

    public static TransportClient getClient(String elkServer){
        if(client == null){
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", "StageElkCluster")
                    .build();

            client = new TransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(elkServer, 9300));
        }
        return client;
    }


}
