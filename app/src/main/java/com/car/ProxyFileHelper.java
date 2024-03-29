package com.car;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProxyFileHelper {
    private static final String SINGLE_CONFIG_TAG = "Config";
    private static final String CONFIG_LIST_TAG = "ConfigList";


    public static boolean writeNaiveServerConfigList(List<ProxyConfig> configList, String ConfigListPath) {
        JSONArray jsonArray = new JSONArray();
        for (ProxyConfig config : configList) {
            try {
                JSONObject jsonObject = new JSONObject(config.generateProxyConfigJSON());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        String configStr = jsonArray.toString();
        File file = new File(ConfigListPath);
        if (file.exists()) {
            file.delete();
        }
        try (OutputStream fos = new FileOutputStream(file)) {
            fos.write(configStr.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @NonNull
    public static List<ProxyConfig> readNaiveServerConfigList(String ConfigListPath) {
        File file = new File(ConfigListPath);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        try (InputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            String json = new String(data);
            JSONArray jsonArr = new JSONArray(json);
            int len = jsonArr.length();
            List<ProxyConfig> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(parseConfigFromJSON(jsonArr.getJSONObject(i).toString()));
            }
            return list;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static void ShowNaiveConfigList(String ConfigListPath) {
        File file = new File(ConfigListPath);

        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] content = new byte[(int) file.length()];
                fis.read(content);
                LogHelper.v(CONFIG_LIST_TAG, new String(content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String parseProxyConfigToJSON(ProxyConfig config) {
        try {
            /*JSONObject json = new JSONObject();
            json.put("local_addr", config.getLocalAddr());
            json.put("local_port", config.getLocalPort());
            json.put("remote_addr", config.getRemoteAddr());
            json.put("remote_port", config.getRemotePort());
            json.put("password", config.getPassword());
            json.put("verify_cert", config.getVerifyCert());
            json.put("ca_cert_path", config.getCaCertPath());
            json.put("enable_ipv6", config.getEnableIpv6());
            json.put("cipher_list", config.getCipherList());
            json.put("tls13_cipher_list", config.getTls13CipherList());
            return json.toString();*/
            return config.generateProxyConfigJSON();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Nullable
    public static ProxyConfig readConfig(String ConfigPath) {
        File file = new File(ConfigPath);
        if (!file.exists()) {
            Log.e("ReadConfig_error:","File not exist: "+ConfigPath);
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            ProxyConfig Config = new ProxyConfig();
            Config.fromJSON(sb.toString());
            return Config;
        } catch (IOException e) {
            Log.e("ReadConfig_error:",e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    private static ProxyConfig parseConfigFromJSON(String json) {
        ProxyConfig config = new ProxyConfig();
        config.fromJSON(json);
        return config;
    }


    public static void ChangeListenPort(String ConfigPath, long port) {
        File file = new File(ConfigPath);
        if (file.exists()) {
            try {
                String str;
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] content = new byte[(int) file.length()];
                    fis.read(content);
                    str = new String(content);

                }
                JSONObject json = new JSONObject(str);
                String listen = json.getString("listen");
                String listen_ = listen.substring(0, listen.lastIndexOf(":"));
                Log.e("TrojanCat",listen_);
                json.put("listen", listen_+":"+port);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(json.toString().getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static long GetProxySocksPort(String ConfigPath){
        File file = new File(ConfigPath);
        if (file.exists()) {
            try {
                String str;
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] content = new byte[(int) file.length()];
                    fis.read(content);
                    str = new String(content);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
                JSONObject json = new JSONObject(str);
                /*String listen_ = listen.substring(0, listen.lastIndexOf(":"));*/
                return json.getInt("local_port");
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }
    public static void WriteProxyConfig(ProxyConfig Config, String ConfigPath) {
        String config = Config.generateProxyConfigJSON();
        File file = new File(ConfigPath);
        try {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(config.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void ShowConfig(String ConfigPath) {
        File file = new File(ConfigPath);

        try {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] content = new byte[(int) file.length()];
                fis.read(content);
                LogHelper.v(SINGLE_CONFIG_TAG, new String(content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
