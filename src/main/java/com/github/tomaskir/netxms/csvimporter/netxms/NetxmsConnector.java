package com.github.tomaskir.netxms.csvimporter.netxms;

import com.github.tomaskir.netxms.csvimporter.csv.CsvNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.netxms.client.NXCException;
import org.netxms.client.NXCObjectCreationData;
import org.netxms.client.NXCSession;
import org.netxms.client.ObjectFilter;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Node;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NetxmsConnector {
    @Getter
    private static final NetxmsConnector instance = new NetxmsConnector();

    private static final int INFRASTRUCTURE_SERVICES_ID = 2;

    private NXCSession nxcSession;

    public void connect(String address, String port, String username, String password) throws IOException, NXCException {
        nxcSession = new NXCSession(address, Integer.parseInt(port));
        nxcSession.connect();
        nxcSession.login(username, password);
        nxcSession.syncObjects();
    }

    public void addNodes(List<CsvNode> nodeList, boolean createContainers) throws IOException, NXCException {
        ObjectFilter filter;
        NXCObjectCreationData objectCreationData;
        long nodeParentId;

        for (final CsvNode node : nodeList) {
            System.out.print(".");

            // create object filter to check for node duplicity by name and address
            filter = new ObjectFilter() {
                @Override
                public boolean filter(AbstractObject object) {
                    return object.getObjectClass() == AbstractObject.OBJECT_NODE &&
                            (object.getObjectName().equals(node.getName()) || ((Node) object).getPrimaryName().equals(node.getAddress()));
                }
            };

            AbstractObject object = nxcSession.findObject(filter);
            if (object != null) {
                System.out.println();
                System.out.println("Warning - object with name '" + node.getName() + "' or address '" + node.getAddress() + "' already exists, not creating the node.");
                continue;
            }

            // create object filter to find the node's container
            filter = new ObjectFilter() {
                @Override
                public boolean filter(AbstractObject object) {
                    return object.getObjectClass() == AbstractObject.OBJECT_CONTAINER && object.getObjectName().equals(node.getContainer());
                }
            };

            // find node's container
            AbstractObject container = nxcSession.findObject(filter);
            if (container == null) {
                // if not found, behaviour determined by "import.create.containers" config property
                if (!createContainers) {
                    // log warning
                    System.out.println();
                    System.out.println("Warning - container '" + node.getContainer() + "' not found for node '" + node.getName() + "', not creating node.");
                    continue;
                } else {
                    // create container
                    objectCreationData = new NXCObjectCreationData(AbstractObject.OBJECT_CONTAINER, node.getContainer(), INFRASTRUCTURE_SERVICES_ID);
                    nodeParentId = nxcSession.createObject(objectCreationData);
                }
            } else {
                // if found, use it as node's parent
                nodeParentId = container.getObjectId();
            }

            // object creation data
            objectCreationData = new NXCObjectCreationData(AbstractObject.OBJECT_NODE, node.getName(), nodeParentId);
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
