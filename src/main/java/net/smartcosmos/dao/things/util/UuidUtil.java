package net.smartcosmos.dao.things.util;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UuidUtil {

    private static final String URN_PREFIX = "urn";
    private static final String URN_SEPARATOR = ":";

    private static final String UUID_TYPE = "uuid";

    private static final String TENANT_PREFIX = "tenant";
    private static final String THING_PREFIX = "thing";

    public static UUID getUuidFromUrn(String urn) throws IllegalArgumentException {

        String urnScheme="urn:.*:uuid:([A-Z0-9]{8}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{12})";

        Pattern p = Pattern.compile(urnScheme, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(urn);
        if (m.find()) {
            return UUID.fromString(m.group(1));
        }

        throw new IllegalArgumentException(String.format("Provided URN '%s' does not match the required URN scheme '%s'", urn, "urn:{prefix}:uuid:{uuid}"));
    }

    public static String getThingUrnFromUuid(UUID uuid) {
        return getPrefixUrnFromUuid(THING_PREFIX, uuid);
    }

    public static String getTenantUrnFromUuid(UUID uuid) {
        return getPrefixUrnFromUuid(TENANT_PREFIX, uuid);
    }

    static String getPrefixUrnFromUuid(String prefix, UUID uuid) {
        return new StringBuilder(URN_PREFIX)
            .append(URN_SEPARATOR)
            .append(prefix)
            .append(URN_SEPARATOR)
            .append(UUID_TYPE)
            .append(URN_SEPARATOR)
            .append(uuid.toString())
            .toString()
            .toLowerCase();
    }

    public static UUID getNewUuid() {
        return UUID.randomUUID();
    }
}
