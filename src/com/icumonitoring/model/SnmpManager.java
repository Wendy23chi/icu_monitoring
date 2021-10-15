package com.icumonitoring.model;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnmpManager {

    private Snmp snmp = null;
    private String address = null;

    public SnmpManager(String address) {
        this.address = address;
    }

    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous and the
     * listen() method listens for answers.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    /**
     * Method which takes a single OID and returns the response from the agent
     * as a String.
     *
     * @param oid
     * @return
     * @throws IOException
     */
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[]{oid});
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
     * This method is capable of handling multiple OIDs
     *
     * @param oids
     * @return
     * @throws IOException
     */
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);

        ResponseEvent event = snmp.send(pdu, getTarget(), null);
        if (event != null) {
            PDU responsePDU = event.getResponse();
            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();
                if (errorStatus != PDU.noError) {
                    System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
                    System.out.println("Snmp Set Request = " + event.getRequest().getVariableBindings());
                    System.out.println("\nresponsePDU = " + responsePDU);
                    System.out.println("errorStatus = " + responsePDU);
                    System.out.println("Error: Request Failed");
                    System.out.println("Error Status = " + errorStatus);
                    System.out.println("Error Index = " + errorIndex);
                    System.out.println("Error Status Text = " + errorStatusText);
                }
            }
            return event;
        }
        throw new RuntimeException("GET timed out");
    }

    /**
     * Method which returns the sub tree of the OID.
     *
     * @param rootOid
     * @return
     * @throws IOException
     */
    public List<TreeEvent> getSubTree(OID rootOid) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(rootOid));
        pdu.setType(PDU.GETBULK);
        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
        return treeUtils.getSubtree(getTarget(), rootOid);
    }

    public List<Map<String, String>> getTableAsList(OID rootOid, int columns) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 1; i <= columns; i++) {
            List<TreeEvent> tempList = getSubTree(new OID(rootOid.toString() + "." + i));
            for (TreeEvent t : tempList) {
                VariableBinding[] vbs = t.getVariableBindings();
                for (int j = 0; (vbs != null) && j < vbs.length; j++) {
                    if (i == 1) {
                        Map<String, String> map = new HashMap<>();
                        map.put("column" + i, vbs[j].getVariable().toString());
                        list.add(map);
                    } else {
                        Map<String, String> map = list.get(j);
                        map.put("column" + i, vbs[j].getVariable().toString());
                        list.set(j, map);
                    }
                }
            }

        }
        return list;
    }

    /**
     * Method which sets the OID with given value.
     *
     * @param oid
     * @param val
     * @return
     * @throws IOException
     */
    public ResponseEvent set(OID oid, String val) throws IOException {
        PDU pdu = new PDU();
        VariableBinding varBind = new VariableBinding(oid, new OctetString(val));
        pdu.add(varBind);
        pdu.setType(PDU.SET);
        pdu.setRequestID(new Integer32(1));

        ResponseEvent event = snmp.set(pdu, getTarget());
        if (event != null) {
            PDU responsePDU = event.getResponse();
            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();
                if (errorStatus != PDU.noError) {
                    System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
                    System.out.println("Snmp Set Request = " + event.getRequest().getVariableBindings());
                    System.out.println("\nresponsePDU = " + responsePDU);
                    System.out.println("errorStatus = " + responsePDU);
                    System.out.println("Error: Request Failed");
                    System.out.println("Error Status = " + errorStatus);
                    System.out.println("Error Index = " + errorIndex);
                    System.out.println("Error Status Text = " + errorStatusText);
                }
            }
            return event;
        }
        throw new RuntimeException("SET timed out");
    }

    /**
     * Method which sets the OID with given value.
     *
     * @param oid
     * @param val
     * @return
     * @throws IOException
     */
    public ResponseEvent set(OID oid, Integer val) throws IOException {
        PDU pdu = new PDU();
        VariableBinding varBind = new VariableBinding(oid, new Integer32(val));
        pdu.add(varBind);
        pdu.setType(PDU.SET);
        pdu.setRequestID(new Integer32(1));

        ResponseEvent event = snmp.set(pdu, getTarget());
        if (event != null) {
            PDU responsePDU = event.getResponse();
            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();
                if (errorStatus != PDU.noError) {
                    System.out.println("\nResponse:\nGot Snmp Set Response from Agent");
                    System.out.println("Snmp Set Request = " + event.getRequest().getVariableBindings());
                    System.out.println("\nresponsePDU = " + responsePDU);
                    System.out.println("errorStatus = " + responsePDU);
                    System.out.println("Error: Request Failed");
                    System.out.println("Error Status = " + errorStatus);
                    System.out.println("Error Index = " + errorIndex);
                    System.out.println("Error Status Text = " + errorStatusText);
                }
            }
            return event;
        }
        throw new RuntimeException("SET timed out");
    }

    /**
     * This method returns a Target, which contains information about where the
     * data should be fetched and how.
     *
     * @return
     */
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
}
