package boston.convertdata.utils;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstances {
    public static final Gson DEFAULT = Converters.registerAll(new GsonBuilder()).create();
    public static final Gson API = Converters.registerAll(new GsonBuilder()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    public static final Gson ELASTICSEARCH = Converters.registerAll(new GsonBuilder()).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
}
