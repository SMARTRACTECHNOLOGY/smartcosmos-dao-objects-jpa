package net.smartcosmos.dao.things.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.runners.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageImpl;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.util.ThingPersistenceUtil;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.PageInformation;
import net.smartcosmos.dto.things.ThingResponse;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpringDataPageToThingResponsePageConverterTest {

    @Mock
    ConversionService conversionService;

    @InjectMocks
    SpringDataPageToThingResponsePageConverter converter;

    @Test
    public void testConversionService() throws Exception {

        ThingEntity entity = ThingEntity.builder()
            .id(UuidUtil.getNewUuid())
            .tenantId(UuidUtil.getNewUuid())
            .active(true)
            .build();
        List<ThingEntity> content = new ArrayList<>();
        content.add(entity);

        when(conversionService.convert(eq(entity), eq(ThingResponse.class))).thenReturn(mock(ThingResponse.class));

        org.springframework.data.domain.Page<ThingEntity> entityPage = new PageImpl<>(content);
        Page<ThingResponse> convertedPage = converter.convert(entityPage);

        List<ThingResponse> data = convertedPage.getData();
        assertNotNull(data);
        assertFalse(data.isEmpty());
        ThingResponse response = data.get(0);
        assertEquals(1, data.size());

        PageInformation page = convertedPage.getPage();
        assertNotNull(page);
        assertEquals(1, page.getNumber());
        assertEquals(1, page.getSize());
        assertEquals(1, page.getTotalPages());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void thatEmptyPageConversionSucceeds() {

        List<ThingEntity> content = new ArrayList<>();
        org.springframework.data.domain.Page<ThingEntity> emptyEntityPage = new PageImpl<>(content);

        Page<ThingResponse> convertedPage = converter.convert(emptyEntityPage);

        Collection<ThingResponse> data = convertedPage.getData();
        assertNotNull(data);
        assertTrue(data.isEmpty());

        PageInformation page = convertedPage.getPage();
        assertNotNull(page);
        assertEquals(0, page.getSize());
        assertEquals(0, page.getNumber());
        assertEquals(0, page.getTotalPages());
        assertEquals(0, page.getTotalElements());

        Page<ThingResponse> emptyPage = ThingPersistenceUtil.emptyPage();
        assertEquals(emptyPage, convertedPage);
    }

}
