package com.icumonitoring;

import com.icumonitoring.model.BloodPressures;
import com.icumonitoring.model.ICUDevice;
import com.icumonitoring.model.SnmpManager;
import okhttp3.*;
import org.snmp4j.smi.OID;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;


public class Manager {

    private static final String MY_OID = "1.3.6.1.4.1.1122";
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static SnmpManager client;

    public static void main(String[] args) throws IOException {
        client = new SnmpManager("udp:127.0.0.1/1234");
        client.start();

        sendToDBICUPatient(getIcuID(client), getICUPatient(client));
        sendToDBICUSwitch(getIcuID(client), getICUSwitch(client));

        HeartRateThread client_hr = new HeartRateThread(client);
        BloodPressureThread client_blood = new BloodPressureThread(client);
        OxygenSaturationThread client_oxy = new OxygenSaturationThread(client);
        TempThread client_temp = new TempThread(client);
        RespirationThread client_resp = new RespirationThread(client);
        DisableIgnitionThread client_dis = new DisableIgnitionThread(client);
        ICUDeviceThread client_icudev = new ICUDeviceThread(client);

        client_hr.start();
        client_blood.start();
        client_oxy.start();
        client_temp.start();
        client_resp.start();
        client_dis.start();
        client_icudev.start();

    }

    private static String getIcuID(SnmpManager client) throws IOException {
        return client.getAsString(new OID(MY_OID + ".1.0"));
    }

    private static Integer getHeartRate(SnmpManager client) throws IOException {
        return Integer.valueOf(client.getAsString(new OID(MY_OID + ".2.0")));
    }

    private static BloodPressures getBloodPressures(SnmpManager client) throws IOException {
        return new BloodPressures(
            Integer.valueOf(client.getAsString(new OID(MY_OID + ".3.1.0"))),
            Integer.valueOf(client.getAsString(new OID(MY_OID + ".3.2.0")))
        );
    }

    private static Integer getOxygenSaturation(SnmpManager client) throws IOException {
        return Integer.valueOf(client.getAsString(new OID(MY_OID + ".4.0")));
    }

    private static Double getTemperature(SnmpManager client) throws IOException {
        return Double.valueOf(client.getAsString(new OID(MY_OID + ".5.0")));
    }

    private static Integer getRespiration(SnmpManager client) throws IOException {
        return Integer.valueOf(client.getAsString(new OID(MY_OID + ".6.0")));
    }

    private static Integer getICUSwitch(SnmpManager client) throws IOException {
        return Integer.valueOf(client.getAsString(new OID(MY_OID + ".7.0")));
    }

    private static Integer getICUSwitchFromDB(SnmpManager client) throws IOException {
        String db = "icumonitordb";
        String q = "SELECT \"value\" FROM \"ICUSwitch\" WHERE \"icu\"='" + getIcuID(client) + "'";
        Request request = new Request.Builder()
                .url("http://localhost:8086/query?db=" + db + "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONArray arr = new JSONObject(new JSONObject(new JSONObject(response.body().string()).getJSONArray("results").get(0).toString()).getJSONArray("series").get(0).toString()).getJSONArray("values");
                return Integer.valueOf(new JSONArray(arr.get(arr.length() - 1).toString()).get(1).toString());
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void setICUSwitch(SnmpManager client, Integer value) throws IOException {
        client.set(new OID(MY_OID + ".7.0"), value);
    }

    private static List<ICUDevice> getICUPatient(SnmpManager client) throws IOException {
        List<ICUDevice> list = new ArrayList<>();
        List<Map<String, String>> patient = client.getTableAsList(new OID(MY_OID + ".8.1"), 3);
        patient.forEach((m) -> list.add(new ICUDevice(m.get("column2"), m.get("column3"))));
        return list;
    }

    private static void sendToDBHeartRate(String icuID, Integer heartRate) throws IOException {
        RequestBody formBody = RequestBody.create("HeartRate," + "icu=" + icuID + " value=" + heartRate, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("HeartRate data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBBloodPress(String icuID, BloodPressures bloodPressures) throws IOException {
        RequestBody formBody = RequestBody.create("BloodPressures," + "icu=" + icuID + " systolic=" + bloodPressures.getSystolic() + ",diastolic=" + bloodPressures.getDiastolic(), MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("BloodPressure data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBOxySat(String icuID, Integer oxySat) throws IOException {
        RequestBody formBody = RequestBody.create("OxygenSaturation," + "icu=" + icuID + " value=" + oxySat, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("OxygenSaturation data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBTemp(String icuID, Double temp) throws IOException {
        RequestBody formBody = RequestBody.create("Temperature," + "icu=" + icuID + " value=" + temp, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Temperature data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBResp(String icuID, Integer resp) throws IOException {
        RequestBody formBody = RequestBody.create("Respiration," + "icu=" + icuID + " value=" + resp, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Respiration data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBICUSwitch(String icuID, Integer icuSwitch) throws IOException {
        RequestBody formBody = RequestBody.create(
                "ICUSwitch,"
                        + "icu=" + icuID
                        + " value=" + icuSwitch, MediaType.parse("text/plain"));
        Request request = new Request.Builder()
                .url("http://localhost:8086/write?db=icumonitordb")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Disable ignition data saved to DB.");
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    private static void sendToDBICUPatient(String icuID, List<ICUDevice> icuDevices) {
        icuDevices.forEach((d) -> {
            RequestBody formBody = RequestBody.create(
                    "ICUDevices,"
                            + "icu=" + icuID
                            + " name=" + "\"" + d.getName() + "\""
                            + ",idNumber=" + d.getIdNumber(), MediaType.parse("text/plain"));
            Request request = new Request.Builder()
                    .url("http://localhost:8086/write?db=icumonitordb")
                    .post(formBody)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("ICU Device data saved to DB.");
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            } catch (IOException ex) {
                System.out.println("ICU Patient Error saving data to DB.");
                System.out.println(ex);
            }
        });
    }

    private static class HeartRateThread extends Thread {

        SnmpManager client;

        public HeartRateThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendToDBHeartRate(getIcuID(client), getHeartRate(client));

                    Thread.sleep(10000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }

    private static class BloodPressureThread extends Thread {

        SnmpManager client;

        public BloodPressureThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendToDBBloodPress(getIcuID(client), getBloodPressures(client));

                    Thread.sleep(5000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }

    private static class OxygenSaturationThread extends Thread {

        SnmpManager client;

        public OxygenSaturationThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendToDBOxySat(getIcuID(client), getOxygenSaturation(client));

                    Thread.sleep(10000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }

    private static class TempThread extends Thread {

        SnmpManager client;

        public TempThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendToDBTemp(getIcuID(client), getTemperature(client));

                    Thread.sleep(10000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }


    private static class RespirationThread extends Thread {

        SnmpManager client;

        public RespirationThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sendToDBResp(getIcuID(client), getRespiration(client));

                    Thread.sleep(10000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }


    public static class DisableIgnitionThread extends Thread {

        SnmpManager client;

        public DisableIgnitionThread(SnmpManager client) {
            this.client = client;
        }

        private final Integer REFRESH_INTERVAL = 3 * 1000;

        @Override
        public void run() {
            while (true) {
                try {
                    Integer fromDB = getICUSwitchFromDB(client);
                    Integer fromAgent = getICUSwitch(client);

                    if (!Objects.equals(fromDB, fromAgent)) {
                        setICUSwitch(client, fromDB);
                    }

                    Thread.sleep(REFRESH_INTERVAL);
                } catch (InterruptedException | IOException e) {
                    System.out.println(e);
                }
            }

        }
    }

    private static class ICUDeviceThread extends Thread {

        SnmpManager client;

        public ICUDeviceThread(SnmpManager client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Integer fromDB = getICUSwitchFromDB(client);
                    Integer fromAgent = getICUSwitch(client);

                    if (!Objects.equals(fromDB, fromAgent)) {
                        setICUSwitch(client, fromDB);
                    }

                    Thread.sleep(5000);
                } catch (InterruptedException | IOException e) {
                }
            }

        }
    }

}
