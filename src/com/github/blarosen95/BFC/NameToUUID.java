package com.github.blarosen95.BFC;

import org.apache.commons.io.IOUtils;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;

public class NameToUUID {
    @Deprecated
    public String getUUID(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;

        try {
            String uuidJson = IOUtils.toString(new URL(url), "UTF-8");
            if (uuidJson.isEmpty()) return "invalid name";
            JSONObject uuidObject = (JSONObject) JSONValue.parseWithException(uuidJson);
            String uuidDirty = uuidObject.get("id").toString(); //This uuid string does not have the dashes
            return uuidDirty.replaceAll(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5");
        } catch (IOException | ParseException e) {
            if (e.getMessage().contains("Server returned HTTP response code: 429 for URL")) {
                System.out.println(ChatColor.RED + "Rate Limit Reached. Informing Player");
                return "Rate Limited";
            }
            e.printStackTrace();
        }
        return null;
    }
}
