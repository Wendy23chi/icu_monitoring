package com.icumonitoring;

import com.icumonitoring.model.BloodPressures;
import com.icumonitoring.model.MOScalarFactory;
import com.icumonitoring.model.MOTableBuilder;
import com.icumonitoring.model.SnmpAgent;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTable;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;

import java.io.IOException;

public class Agent {
    static SnmpAgent agent;
    private static final String MY_OID = "1.3.6.1.4.1.1122";
    private static Integer HEART_RATE = 63;
    private static final BloodPressures bloodPressures = new BloodPressures(108, 68);
    private static Integer OXYGEN_SAT = 68;
    private static Double TEMP = 37.0;
    private static Integer RESP = 22;

    private static final MOScalar icuID = MOScalarFactory.createReadOnly(new OID(MY_OID + ".1.0"), "ICU-01A");
    private static MOScalar heartRate = MOScalarFactory.createReadOnly(new OID(MY_OID + ".2.0"), HEART_RATE.toString());
    private static MOScalar bloodPressureSys = MOScalarFactory.createReadOnly(new OID(MY_OID + ".3.1.0"), bloodPressures.getSystolic().toString());
    private static MOScalar bloodPressureDia = MOScalarFactory.createReadOnly(new OID(MY_OID + ".3.2.0"), bloodPressures.getDiastolic().toString());
    private static MOScalar oxygenSaturation = MOScalarFactory.createReadOnly(new OID(MY_OID + ".4.0"), OXYGEN_SAT.toString());
    private static MOScalar temperature = MOScalarFactory.createReadOnly(new OID(MY_OID + ".5.0"), TEMP.toString());
    private static MOScalar respiration = MOScalarFactory.createReadOnly(new OID(MY_OID + ".6.0"), RESP.toString());
    private static MOScalar icuSwitch = MOScalarFactory.createReadWrite(new OID(MY_OID + ".7.0"), 0);
    private static MOTable icuDevice = new MOTableBuilder(new OID(MY_OID + ".8.1"))
            .addColumnType(SMIConstants.SYNTAX_INTEGER, MOAccessImpl.ACCESS_READ_ONLY)
            .addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY)
            .addColumnType(SMIConstants.SYNTAX_OCTET_STRING, MOAccessImpl.ACCESS_READ_ONLY)
            .addRowValue(new Integer32(1))
            .addRowValue(new OctetString("Jon Doe"))
            .addRowValue(new OctetString("123456789"))
            .addRowValue(new Integer32(2))
            .addRowValue(new OctetString("Dodo Joe"))
            .addRowValue(new OctetString("987654321")).build();

    public static void main(String[] args) throws IOException {
        agent = new SnmpAgent("udp:127.0.0.1/1234");
        System.out.println("AGENT IS RUNNING");
        agent.start();

        agent.unregisterManagedObject(agent.getSnmpv2MIB());

        agent.registerManagedObject(icuID);
        agent.registerManagedObject(heartRate);
        agent.registerManagedObject(bloodPressureSys);
        agent.registerManagedObject(bloodPressureDia);
        agent.registerManagedObject(oxygenSaturation);
        agent.registerManagedObject(temperature);
        agent.registerManagedObject(respiration);
        agent.registerManagedObject(icuSwitch);
        agent.registerManagedObject(icuDevice);

        Threading t = new Threading();
        t.start();

        while (true) {

        }
    }

    private static void simulateHeartRate() {
        Integer newHeartRate = Randomizer.getBiasedRandomInteger(60,80, 65, 1.0);

        agent.unregisterManagedObject(heartRate);

        heartRate = MOScalarFactory.createReadOnly(new OID(MY_OID + ".2.0"), String.valueOf(newHeartRate));
        agent.registerManagedObject(heartRate);
    }

    private static void simulateBloodPres(BloodPressures bloodPressures) {
        int newSystolic;
        int newDiastolic;

        newSystolic = Randomizer.getBiasedRandomInteger(90, 120, 100, 1.0);
        newDiastolic = Randomizer.getBiasedRandomInteger(60, 80, 70, 1.0);;

        agent.unregisterManagedObject(bloodPressureSys);
        agent.unregisterManagedObject(bloodPressureDia);

        bloodPressureSys = MOScalarFactory.createReadOnly(new OID(MY_OID + ".3.1.0"), String.valueOf(newSystolic));
        bloodPressureDia = MOScalarFactory.createReadOnly(new OID(MY_OID + ".3.2.0"), String.valueOf(newDiastolic));
        agent.registerManagedObject(bloodPressureSys);
        agent.registerManagedObject(bloodPressureDia);
    }

    private static BloodPressures calculateBloodPressures() {
        return new BloodPressures(bloodPressures.getSystolic(), bloodPressures.getDiastolic());
    }

    private static void simulateOxygenSat() {
        Integer newOxygenSatu = Randomizer.getBiasedRandomInteger(80,100, 87, 1.0);

        agent.unregisterManagedObject(oxygenSaturation);

        oxygenSaturation = MOScalarFactory.createReadOnly(new OID(MY_OID + ".4.0"), String.valueOf(newOxygenSatu));
        agent.registerManagedObject(oxygenSaturation);
    }

    private static void simulateTemp() {
        Double newTemp = Randomizer.getBiasedRandomDouble(36.5,37.2, 36.9, 1.0);

        agent.unregisterManagedObject(temperature);

        temperature = MOScalarFactory.createReadOnly(new OID(MY_OID + ".5.0"), String.valueOf(newTemp));
        agent.registerManagedObject(temperature);
    }

    private static void simulateRespiration() {
        Integer newResp = Randomizer.getBiasedRandomInteger(12,20, 15, 1.0);

        agent.unregisterManagedObject(respiration);

        respiration = MOScalarFactory.createReadOnly(new OID(MY_OID + ".6.0"), String.valueOf(newResp));
        agent.registerManagedObject(respiration);
    }

    private static class Threading extends Thread {

        private final BloodPressures bloodPressures = calculateBloodPressures();

        @Override
        public void run() {
            super.run();
            int refreshInterval = 500;

            while (true) {
                try {
                    simulateHeartRate();
                    simulateBloodPres(bloodPressures);
                    simulateOxygenSat();
                    simulateTemp();
                    simulateRespiration();

                    Thread.sleep(refreshInterval);

                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
            }
        }
    }
}
