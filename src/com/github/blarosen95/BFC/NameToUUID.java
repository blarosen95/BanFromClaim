package com.github.blarosen95.BFC;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;

public class NameToUUID {
    public String getUUID(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

        try {
            String uuidJson = IOUtils.toString(new URL(url), "UTF-8");
            if (uuidJson.isEmpty()) return "invalid name";
            JSONObject uuidObject = (JSONObject) JSONValue.parseWithException(uuidJson);
            String uuidDirty = uuidObject.get("id").toString(); // TODO: 10/11/2018 Note: this uuid string does not have the dashes!
            return uuidDirty.replaceAll(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
