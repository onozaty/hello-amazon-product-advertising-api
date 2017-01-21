package com.example.apap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import am.ik.aws.apa.jaxws.Item;
import am.ik.aws.apa.jaxws.ItemAttributes;

public class BookLookupClientTest {

    @Test
    public void testLookup() {

        BookLookupClient client = new BookLookupClient();

        Optional<Item> item = client.lookup("489471499X");

        assertTrue(item.isPresent());

        ItemAttributes itemAttributes = item.get().getItemAttributes();
        assertEquals("489471499X", itemAttributes.getISBN());
        assertEquals("Effective Java 第2版 (The Java Series)", itemAttributes.getTitle());
        assertEquals("ピアソンエデュケーション", itemAttributes.getPublisher());
    }

}
