package org.hopestarter.wallet.server_api;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Adrian on 15/04/2016.
 */
public class OutboundLocationMarkSerializerTest {
    @Test
    public void serializingTest() throws JSONException {
        Date currentDate = new Date(System.currentTimeMillis());

        SimpleDateFormat dateFormat = ISODateFormatFactory.getDateFormat();

        JsonParser jsonParser = new JsonParser();

        String expectedJsonStr = "{\n" +
                "    \"created\": \"" + dateFormat.format(currentDate) + "\",\n" +
                "    \"point\": {\n" +
                "        \"type\": \"point\",\n" +
                "        \"coordinates\": [1000, 1000]\n" +
                "    }," +
                "   \"text\":\"This is some text\"," +
                "   \"picture\": [{\"url\": \"s3://staginghopestarterimageupload/uploads/4d45d9f3-956f-4939-972c-cd33b0bd945c/profile.jpg\"}]" +
                "}";

        JsonElement expectedJson = jsonParser.parse(expectedJsonStr);

        OutboundLocationMarkSerializer serializer = new OutboundLocationMarkSerializer();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(OutboundLocationMark.class, serializer)
                .create();

        Point point = new Point("point", new float[] {1000, 1000});

        ArrayList<URI> pictures = new ArrayList<>();
        pictures.add(URI.create("s3://staginghopestarterimageupload/uploads/4d45d9f3-956f-4939-972c-cd33b0bd945c/profile.jpg"));

        OutboundLocationMark locationMark = new OutboundLocationMark(currentDate, point, pictures, "This is some text");

        String jsonText = gson.toJson(locationMark);

        JsonElement result = jsonParser.parse(jsonText);

        assertEquals("Serialized string doesn't match", expectedJson, result);
    }
}
