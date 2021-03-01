package xyz.property.data.search.resource;


import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
public class SearchResource {

    @Inject
    RestHighLevelClient client;

    final private SearchSourceBuilder searchSourceBuilder;
    final private SearchRequest searchRequest;

    public SearchResource() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("property.published", true));
        searchSourceBuilder.sort(SortBuilders.fieldSort("property_key.keyword").order(SortOrder.DESC));
        this.searchSourceBuilder = searchSourceBuilder;
        this.searchRequest = new SearchRequest("properties_for_sale");
    }

    @GET
    @Path("/properties-for-sale")
    @Retry
    @CircuitBreaker
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponse getAvailable(@QueryParam("search_after") String searchIndex) throws IOException {
        if (searchIndex != null) {
            searchSourceBuilder.searchAfter(new Object[]{searchIndex});          //details at https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html
        }
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }


}
