package com.github.tomaskir.netxms.csvimporter.netxms;

import com.github.tomaskir.netxms.csvimporter.csv.CsvNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.netxms.client.NXCObjectCreationData;
import org.netxms.client.NXCSession;
import org.netxms.client.ObjectFilter;
import org.netxms.client.objects.AbstractObject;
import org.netxms.client.objects.Node;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NetxmsConnectorTest {

    // NetXMS' "Infrastructure Services" root, the hardcoded parent for created containers.
    private static final long INFRASTRUCTURE_SERVICES_ID = 2L;

    private NXCSession session;
    private List<AbstractObject> existingObjects;

    @BeforeEach
    void setUp() throws Exception {
        session = mock(NXCSession.class);
        existingObjects = new ArrayList<>();

        // Mimic NXCSession.findObject: run the supplied filter over the existing objects,
        // returning the first match. This exercises the real ObjectFilter logic in the connector.
        when(session.findObject(any())).thenAnswer(invocation -> {
            ObjectFilter filter = invocation.getArgument(0);
            for (AbstractObject object : existingObjects) {
                if (filter.accept(object)) {
                    return object;
                }
            }
            return null;
        });

        injectSession(session);
    }

    private void injectSession(NXCSession mockSession) throws Exception {
        Field field = NetxmsConnector.class.getDeclaredField("nxcSession");
        field.setAccessible(true);
        field.set(NetxmsConnector.getInstance(), mockSession);
    }

    private Node existingNode(String name, String primaryName) {
        Node node = mock(Node.class);
        when(node.getObjectClass()).thenReturn(AbstractObject.OBJECT_NODE);
        when(node.getObjectName()).thenReturn(name);
        when(node.getPrimaryName()).thenReturn(primaryName);
        return node;
    }

    private AbstractObject existingContainer(String name, long id) {
        AbstractObject container = mock(AbstractObject.class);
        when(container.getObjectClass()).thenReturn(AbstractObject.OBJECT_CONTAINER);
        when(container.getObjectName()).thenReturn(name);
        when(container.getObjectId()).thenReturn(id);
        return container;
    }

    private List<CsvNode> oneNode() {
        return Collections.singletonList(new CsvNode("web1", "10.0.0.1", "DataCenter"));
    }

    // --- positive: things that should happen ---

    @Test
    void createsNodeUnderExistingContainer() throws Exception {
        existingObjects.add(existingContainer("DataCenter", 50L));

        NetxmsConnector.getInstance().addNodes(oneNode(), false);

        // container already exists, so it must not be (re)created
        verify(session, never()).createObjectSync(any());

        ArgumentCaptor<NXCObjectCreationData> captor = ArgumentCaptor.forClass(NXCObjectCreationData.class);
        verify(session, times(1)).createObject(captor.capture());

        NXCObjectCreationData created = captor.getValue();
        assertEquals(AbstractObject.OBJECT_NODE, created.getObjectClass());
        assertEquals("web1", created.getName());
        assertEquals("10.0.0.1", created.getPrimaryName());
        assertEquals(50L, created.getParentId());
    }

    @Test
    void createsContainerThenNodeWhenContainerMissingAndCreationEnabled() throws Exception {
        // no existing container; the connector should create it and parent the node under it
        AbstractObject createdContainer = existingContainer("DataCenter", 100L);
        when(session.createObjectSync(any())).thenReturn(createdContainer);

        NetxmsConnector.getInstance().addNodes(oneNode(), true);

        ArgumentCaptor<NXCObjectCreationData> containerCaptor = ArgumentCaptor.forClass(NXCObjectCreationData.class);
        verify(session, times(1)).createObjectSync(containerCaptor.capture());
        NXCObjectCreationData container = containerCaptor.getValue();
        assertEquals(AbstractObject.OBJECT_CONTAINER, container.getObjectClass());
        assertEquals("DataCenter", container.getName());
        assertEquals(INFRASTRUCTURE_SERVICES_ID, container.getParentId());

        ArgumentCaptor<NXCObjectCreationData> nodeCaptor = ArgumentCaptor.forClass(NXCObjectCreationData.class);
        verify(session, times(1)).createObject(nodeCaptor.capture());
        NXCObjectCreationData node = nodeCaptor.getValue();
        assertEquals(AbstractObject.OBJECT_NODE, node.getObjectClass());
        assertEquals("web1", node.getName());
        assertEquals("10.0.0.1", node.getPrimaryName());
        assertEquals(100L, node.getParentId());
    }

    // --- negative: things that should NOT happen ---

    @Test
    void skipsNodeWhenDuplicateByName() throws Exception {
        existingObjects.add(existingContainer("DataCenter", 50L));
        existingObjects.add(existingNode("web1", "192.168.0.1")); // same name, different address

        NetxmsConnector.getInstance().addNodes(oneNode(), true);

        verify(session, never()).createObject(any(NXCObjectCreationData.class));
        verify(session, never()).createObjectSync(any());
    }

    @Test
    void skipsNodeWhenDuplicateByAddress() throws Exception {
        existingObjects.add(existingContainer("DataCenter", 50L));
        existingObjects.add(existingNode("other-name", "10.0.0.1")); // different name, same address

        NetxmsConnector.getInstance().addNodes(oneNode(), true);

        verify(session, never()).createObject(any(NXCObjectCreationData.class));
        verify(session, never()).createObjectSync(any());
    }

    @Test
    void skipsNodeWhenContainerMissingAndCreationDisabled() throws Exception {
        // no existing container, and createContainers = false

        NetxmsConnector.getInstance().addNodes(oneNode(), false);

        verify(session, never()).createObjectSync(any());
        verify(session, never()).createObject(any(NXCObjectCreationData.class));
    }

    @Test
    void disconnectDelegatesToSession() {
        NetxmsConnector.getInstance().disconnect();

        verify(session, times(1)).disconnect();
    }
}
