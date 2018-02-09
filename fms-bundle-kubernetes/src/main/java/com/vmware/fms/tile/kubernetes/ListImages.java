/*
Copyright (c) 2017-2018 VMware, Inc. All Rights Reserved.
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.vmware.fms.tile.kubernetes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vmware.fms.tile.common.TileExecutable;
import com.vmware.fms.tile.common.TileExecutableRequest;
import com.vmware.fms.tile.common.TileExecutableResponse;
import com.vmware.fms.tile.common.TileProperties;
import com.vmware.fms.tile.kubernetes.util.ResponseK8;
import service.HttpResponse;
import service.KubernetesMasterConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ListImages implements TileExecutable {
    private static final Logger logger = Logger.getLogger(ListImages.class.getName());

    public void handleExecute(TileExecutableRequest request, TileExecutableResponse response) {
        //All the images which are present in the pods created by the given deployment are list
        logger.info("\nEntered ListImages\n");
        TileProperties inputProps =
                request.getInputProperties().getAsProperties("kubernetesMaster");
        KubernetesMasterConfig config = new KubernetesMasterConfig(inputProps);
        String hostUrl = config.getMaster();
        String user_name = config.getUser_name();
        String password = config.getPassword();
        String name = request.getInputProperties().getAsString("depName");
        String namespace = request.getInputProperties().getAsString("nameSpaceVal");
        String createUrl =
                "apis/extensions/v1beta1/namespaces/" + namespace + "/deployments/" + name;
        String reply=null;
        String url = hostUrl + createUrl;
        HttpResponse client = new HttpResponse(user_name,password);
        try {
            reply = client.get(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        ResponseK8 replyJson = gson.fromJson(reply, ResponseK8.class);
        Integer number_Img =
                Integer.valueOf(gson.toJson(replyJson.spec.template.spec.containers.size()));
        ArrayList<String> images = new ArrayList<String >();
        for (Integer i = 0;i < number_Img;i++) {
            String imageName = gson.toJson(replyJson.spec.template.spec.containers.get(i).image);
            imageName = imageName.replace("\"", "");
            images.add(i,imageName);
        }
        logger.info(String.valueOf(images));
        response.getOutputProperties().setInteger("imageNum",number_Img);
        response.getOutputProperties().setStringArray("listImages",images);
    }
}