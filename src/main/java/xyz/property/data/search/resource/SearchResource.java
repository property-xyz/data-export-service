package xyz.property.data.search.resource;


import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jboss.resteasy.reactive.RestSseElementType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/search")
public class SearchResource {

    @Inject
    RestHighLevelClient client;

    final Scroll scroll = new Scroll(TimeValue.timeValueMillis(2000L));

    private String scrollId;
    private SearchResponse searchResponse;


    @SneakyThrows
    private void initSearchScrollContext() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("property.published", true));
        SearchRequest searchRequest = new SearchRequest("properties_for_sale");
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
        searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        scrollId = searchResponse.getScrollId();
    }


    @SneakyThrows
    private Uni<SearchHits> getScrollHits() {
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(scroll);
        SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
        scrollId = scrollResponse.getScrollId();
        return Uni.createFrom().item(scrollResponse.getHits());
    }


    @GET
    @Path("/available")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.APPLICATION_JSON)
    public Multi<SearchHit> getAllAvailable() {

        initSearchScrollContext();

        Multi<SearchHit> scrollResponse = Multi.createBy()
                .repeating()
                .uni(this::getScrollHits)
                .whilst(hits -> hits.getHits() != null && hits.getHits().length > 0)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .onItem()
                .disjoint();

        Multi<SearchHit> initialSearchResponse = Multi.createFrom().items(searchResponse.getHits().getHits());

        return Multi.createBy()
                .merging()
                .streams(initialSearchResponse, scrollResponse);
    }


}
