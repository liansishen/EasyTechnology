package com.hepdd.easytech.common;

import com.hepdd.easytech.api.enums.ETHPacketTypes;

import gregtech.api.net.GTPacket;
import gregtech.common.GTNetwork;
import io.netty.channel.ChannelHandler;

@ChannelHandler.Sharable
public class ETHNetwork extends GTNetwork {

    public ETHNetwork() {
        this("EasyTech", ETHPacketTypes.referencePackets());
    }

    public ETHNetwork(String channelName, GTPacket... packetTypes) {
        super(channelName, packetTypes);
    }
}
