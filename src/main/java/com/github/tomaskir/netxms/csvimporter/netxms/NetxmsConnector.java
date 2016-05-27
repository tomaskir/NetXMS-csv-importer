package com.github.tomaskir.netxms.csvimporter.netxms;

import com.github.tomaskir.netxms.csvimporter.csv.CsvNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.netxms.client.NXCException;
import org.netxms.client.NXCObjectCreationData;
import org.netxms.client.NXCSession;
import org.netxms.client.objects.AbstractObject;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NetxmsConnector {
    @Getter
    private final static NetxmsConnector instance = new NetxmsConnector();

    private NXCSession nxcSession;

    public void connect(String address, String port, String username, String password) throws IOException, NXCException {
        nxcSession = new NXCSession(address, Integer.parseInt(port));
        nxcSession.connect();
        nxcSession.login(username, password);
        nxcSession.syncObjects();
    }

    public void addNodes(List<CsvNode> nodeList) throws IOException, NXCException {
        for (CsvNode node : nodeList) {
            System.out.print(".");

            AbstractObject object = nxcSession.findObjectByName(node.getName());
            if (object != null) {
                System.out.println();
                System.out.println("Warning - object with name '" + node.getName() + "' already exists, not creating the node.");
                continue;
            }

            AbstractObject container = nxcSession.findObjectByName(node.getContainer());
            if (container == null) {
                System.out.println();
                System.out.println("Warning - container '" + node.getContainer() + "' not found for node '" + node.getName() + "', not creating node.");
                continue;
            }

            // object creation data
            NXCObjectCreationData objectCreationData = new NXCObjectCreationData(AbstractObject.OBJECT_NODE, node.getName(), container.getObjectId());
            objectCreationData.setPrimaryName(node.getAddress());

            // create the object
            nxcSession.createObject(objectCreationData);
        }

        System.out.println();
    }

    public void disconnect() {
        nxcSession.disconnect();
    }
}
