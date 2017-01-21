package com.example.apap;

import java.util.Optional;

import am.ik.aws.apa.AwsApaRequester;
import am.ik.aws.apa.AwsApaRequesterImpl;
import am.ik.aws.apa.jaxws.Item;
import am.ik.aws.apa.jaxws.ItemLookupRequest;
import am.ik.aws.apa.jaxws.ItemLookupResponse;

public class BookLookupClient {

    public Optional<Item> lookup(String isbn) {

        ItemLookupRequest request = new ItemLookupRequest();

        request.setSearchIndex("Books");
        request.getResponseGroup().add("Large");
        request.setIdType("ISBN");
        request.getItemId().add(isbn);

        AwsApaRequester requester = new AwsApaRequesterImpl();
        ItemLookupResponse response = requester.itemLookup(request);

        if (response.getItems().isEmpty()) {
            return Optional.empty();
        }

        return response.getItems().get(0).getItem().stream().findFirst();
    }
}
