package com.example.apap;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import am.ik.aws.apa.jaxws.BrowseNode;
import am.ik.aws.apa.jaxws.Item;
import am.ik.aws.apa.jaxws.ItemAttributes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BookInformationCollector {

    public static void main(String[] args) throws IOException {

        Path inputFilePath = Paths.get(args[0]);
        Path outputFilePath = Paths.get(args[1]);

        BookInformationCollector collector = new BookInformationCollector();

        try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {

            List<String> isbns = Files.readAllLines(inputFilePath);

            log.debug("取得処理開始  対象件数 : {}", isbns.size());

            int count = 0;
            for (String isbn : isbns) {
                writer.write(collector.getInformation(isbn));
                writer.write(System.lineSeparator());
                count++;

                if (count % 100 == 0) {
                    writer.flush();
                    log.debug("途中経過 : {} / {} ", count, isbns.size());
                }
            }
        }

        log.debug("終了");
    }

    public String getInformation(String isbn) {

        StringJoiner stringJoiner = new StringJoiner("\t");

        stringJoiner.add(isbn);

        Optional<Item> item = new BookLookupClient().lookup(isbn);

        item.ifPresent(x -> {

            ItemAttributes itemAttributes = x.getItemAttributes();
            stringJoiner.add(itemAttributes.getTitle());

            List<String> browseNodeInfomations = toBrowseNodeInfomations(item.get().getBrowseNodes().getBrowseNode());

            List<String> categories = collectCategories(browseNodeInfomations);

            String topCategory = categories.stream().findFirst().orElse("");

            stringJoiner
                    .add(topCategory)
                    .add(String.join(";", categories));

            browseNodeInfomations.forEach(b -> {
                stringJoiner.add(b);
            });

        });

        return stringJoiner.toString();
    }

    private List<String> collectCategories(List<String> browseNodeInfomations) {

        return browseNodeInfomations.stream()
                .map(b -> {
                    return collectCategory(b);
                })
                .filter(b -> {
                    return !b.isEmpty();
                })
                .collect(Collectors.toList());
    }

    private final Pattern categoryPattern = Pattern.compile("(本/ジャンル別|Kindleストア/カテゴリー別/Kindle本)/([^/(]+)");

    private String collectCategory(String browseNodeInfomation) {

        Matcher matcher = categoryPattern.matcher(browseNodeInfomation);

        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }

    private List<String> toBrowseNodeInfomations(List<BrowseNode> browseNodes) {

        return browseNodes.stream()
                .map(b -> {
                    return getBrowseNodePath(b) + "(" + b.getBrowseNodeId() + ")";
                })
                .collect(Collectors.toList());
    }

    private String getBrowseNodePath(BrowseNode browseNode) {

        StringJoiner stringJoiner = new StringJoiner("/");

        if (browseNode.getAncestors() != null) {
            stringJoiner.add(getBrowseNodePath(browseNode.getAncestors().getBrowseNode().get(0)));
        }
        stringJoiner.add(browseNode.getName());

        return stringJoiner.toString();
    }

}
