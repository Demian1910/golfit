package org.golfit.client.handlers;

import org.golfit.client.ClientApi;
import org.golfit.shared.packetParser.PacketParser;

import java.nio.ByteBuffer;

public class SystemHandler {
    private final ClientApi clientApi;

    public SystemHandler(ClientApi clientApi) {
        this.clientApi = clientApi;
    }
    public void onConnected(PacketParser.Packet packet) {
        int assignedId = ByteBuffer.wrap(packet.payload).getInt();
        clientApi.setId(assignedId);
    }
}
