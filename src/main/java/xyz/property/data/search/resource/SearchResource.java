package xyz.property.data.search.resource;


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

@Path("/search")
public class SearchResource {

    @Inject
    RestHighLevelClient client;

    final private SearchSourceBuilder searchSourceBuilder;
    final private SearchRequest searchRequest;

    public SearchResource() {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("property.published", true));
        searchSourceBuilder.sort(SortBuilders.fieldSort("property_key.keyword").order(SortOrder.DESC));
        this.searchSourceBuilder =  searchSourceBuilder;
        this.searchRequest = new SearchRequest("properties_for_sale");
    }

    @GET
    @Path("/properties-for-sale")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponse getAvailable(@QueryParam("search_after") String searchIndex) throws IOException {
        if(searchIndex != null ){
            searchSourceBuilder.searchAfter(new Object[]{searchIndex});
        }
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

}
