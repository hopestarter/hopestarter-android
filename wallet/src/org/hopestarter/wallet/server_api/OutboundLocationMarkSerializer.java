package org.hopestarter.wallet.server_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Adrian on 14/04/2016.
 */
public class OutboundLocationMarkSerializer implements JsonSerializer<OutboundLocationMark> {
    @Override
    public JsonElement serialize(OutboundLocationMark src, Type typeOfSrc, JsonSerializationContext context) {

        JsonElement jsonPoint = context.serialize(src.getPoint());
        JsonElement jsonText = context.serialize(src.getText());

        SimpleDateFormat dateFormat = ISODateFormatFactory.getDateFormat();
        String createISODateString = dateFormat.format(src.getCreated());

        JsonObject jsonMain = new JsonObject();

        JsonArray jsonPictures = new JsonArray();
        for(URI uri : src.getPictures()) {
            JsonObject pictureObject = new JsonObject();
            pictureObject.add("url", context.serialize(uri));
            jsonPictures.add(pictureObject);
        }

        jsonMain.add("picture", jsonPictures);
        jsonMain.add("point", jsonPoint);
        jsonMain.addProperty("created", createISODateString);
        jsonMain.add("text", jsonText);

        return jsonMain;
    }
}
