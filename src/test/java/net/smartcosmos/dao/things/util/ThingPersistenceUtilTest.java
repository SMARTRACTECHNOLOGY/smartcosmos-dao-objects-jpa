package net.smartcosmos.dao.things.util;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class ThingPersistenceUtilTest {

    @Test
    public void isEntityFieldValid() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertTrue(ThingPersistenceUtil.isThingEntityField("id"));
        assertTrue(ThingPersistenceUtil.isThingEntityField("tenantId"));
        assertTrue(ThingPersistenceUtil.isThingEntityField("type"));
        assertTrue(ThingPersistenceUtil.isThingEntityField("active"));
        assertTrue(ThingPersistenceUtil.isThingEntityField("created"));
        assertTrue(ThingPersistenceUtil.isThingEntityField("lastModified"));
    }

    @Test
    public void isEntityFieldInValid() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        assertFalse(ThingPersistenceUtil.isThingEntityField("name"));
        assertFalse(ThingPersistenceUtil.isThingEntityField("description"));
        assertFalse(ThingPersistenceUtil.isThingEntityField("moniker"));
    }

    @Test
    public void normalizeFieldNameUrn() {
        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("urn"));
        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("Urn"));
        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("URN"));

        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("id"));
        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("Id"));
        assertEquals("id", ThingPersistenceUtil.normalizeFieldName("ID"));
    }

    @Test
    public void normalizeFieldNameTenantUrn() {
        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("tenanturn"));
        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("TenantUrn"));
        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("TENANTURN"));

        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("tenantid"));
        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("TenantId"));
        assertEquals("tenantId", ThingPersistenceUtil.normalizeFieldName("TENANTID"));
    }

}
