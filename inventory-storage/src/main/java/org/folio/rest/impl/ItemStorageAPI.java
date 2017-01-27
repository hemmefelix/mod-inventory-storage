package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.folio.rest.jaxrs.model.Item;
import org.folio.rest.jaxrs.model.Items;
import org.folio.rest.jaxrs.resource.ItemStorageResource;
import org.folio.rest.persist.Criteria.Limit;
import org.folio.rest.persist.Criteria.Offset;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.tools.utils.OutStream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.tools.utils.TenantTool;
import org.z3950.zing.cql.cql2pgjson.CQL2PgJSON;

public class ItemStorageAPI implements ItemStorageResource {

  // Has to be lowercase because raml-module-builder uses case sensitive
  // lower case headers
  private static final String TENANT_HEADER = "x-okapi-tenant";
  private static final String BLANK_TENANT_MESSAGE = "Tenant Must Be Provided";

  @Override
  public void getItemStorageItems(
    @DefaultValue("0") @Min(0L) @Max(1000L) int offset,
    @DefaultValue("10") @Min(1L) @Max(100L) int limit,
    String query,
    @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    try {
      vertxContext.runOnContext(v -> {
        try {
          PostgresClient postgresClient = PostgresClient.getInstance(
            vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

          String[] fieldList = {"*"};

          CQL2PgJSON cql2pgJson = new CQL2PgJSON("item.jsonb");
          CQLWrapper cql = new CQLWrapper(cql2pgJson, query)
            .setLimit(new Limit(limit))
            .setOffset(new Offset(offset));

          postgresClient.get("item", Item.class, fieldList, cql, true, false,
            reply -> {
              try {
                List<Item> items = (List<Item>) reply.result()[0];

                Items itemList = new Items();
                itemList.setItems(items);
                itemList.setTotalRecords((Integer) reply.result()[1]);
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemsResponse.
                    withJsonOK(itemList)));

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemsResponse.
                    withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.GetItemStorageItemsResponse.
              withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.GetItemStorageItemsResponse.
          withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void postItemStorageItems(
    @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    Item entity,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    try {
      PostgresClient postgresClient =
        PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {
        try {

          postgresClient.save("item", entity,
            reply -> {
              try {
                OutStream stream = new OutStream();
                stream.setData(entity);

                asyncResultHandler.handle(
                  io.vertx.core.Future.succeededFuture(
                    ItemStorageResource.PostItemStorageItemsResponse
                      .withJsonCreated(reply.result(), stream)));

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(
                  io.vertx.core.Future.succeededFuture(
                    ItemStorageResource.PostItemStorageItemsResponse
                      .withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.PostItemStorageItemsResponse
              .withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.PostItemStorageItemsResponse
          .withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void postItemStorageItemsByItemId(
    @PathParam("itemId") @NotNull String itemId,
    @QueryParam("lang") @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    java.util.Map<String, String> okapiHeaders,
    io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

  }

  @Override
  public void getItemStorageItemsByItemId(
    @PathParam("itemId") @NotNull String itemId,
    @QueryParam("lang") @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    java.util.Map<String, String> okapiHeaders,
    io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    Criteria a = new Criteria();

    a.addField("'id'");
    a.setOperation("=");
    a.setValue(itemId);

    Criterion criterion = new Criterion(a);

    try {
      PostgresClient postgresClient = PostgresClient.getInstance(
        vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      vertxContext.runOnContext(v -> {
        try {
          postgresClient.get("item", Item.class, criterion, true, false,
            reply -> {
              try {
                List<Item> itemList = (List<Item>) reply.result()[0];
                if (itemList.size() == 1) {
                  Item item = itemList.get(0);

                  asyncResultHandler.handle(
                    io.vertx.core.Future.succeededFuture(
                      ItemStorageResource.GetItemStorageItemsByItemIdResponse.
                        withJsonOK(item)));
                } else {
                  throw new Exception(itemList.size() + " results returned");
                }

              } catch (Exception e) {
                e.printStackTrace();
                asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
                  ItemStorageResource.GetItemStorageItemsByItemIdResponse.
                    withPlainInternalServerError("Error")));
              }
            });
        } catch (Exception e) {
          e.printStackTrace();
          asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
            ItemStorageResource.GetItemStorageItemsByItemIdResponse.
              withPlainInternalServerError("Error")));
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
        ItemStorageResource.GetItemStorageItemsByItemIdResponse.
          withPlainInternalServerError("Error")));
    }
  }

  @Override
  public void deleteItemStorageItems(
    @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    Map<String, String> okapiHeaders,
    Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext)
    throws Exception {

    String tenantId = okapiHeaders.get(TENANT_HEADER);

    if (blankTenantId(tenantId)) {
      badRequestResult(asyncResultHandler, BLANK_TENANT_MESSAGE);

      return;
    }

    vertxContext.runOnContext(v -> {
        PostgresClient postgresClient = PostgresClient.getInstance(
          vertxContext.owner(), TenantTool.calculateTenantId(tenantId));

      postgresClient.mutate("TRUNCATE TABLE item",
          reply -> {
            asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
              ItemStorageResource.DeleteItemStorageItemsResponse.noContent().build()));
          });
      });
  }

  @Override
  public void putItemStorageItemsByItemId(
    @PathParam("itemId") @NotNull String itemId,
    @QueryParam("lang") @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    Item entity,
    java.util.Map<String, String> okapiHeaders,
    io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

  }

  @Override
  public void deleteItemStorageItemsByItemId(
    @PathParam("itemId") @NotNull String itemId,
    @QueryParam("lang") @DefaultValue("en") @Pattern(regexp = "[a-zA-Z]{2}") String lang,
    java.util.Map<String, String> okapiHeaders,
    io.vertx.core.Handler<io.vertx.core.AsyncResult<Response>> asyncResultHandler,
    Context vertxContext)
    throws Exception {

  }

  private void badRequestResult(
    Handler<AsyncResult<Response>> asyncResultHandler, String message) {
    asyncResultHandler.handle(io.vertx.core.Future.succeededFuture(
      GetItemStorageItemsResponse.withPlainBadRequest(message)));
  }

  private boolean blankTenantId(String tenantId) {
    return tenantId == null || tenantId == "" || tenantId == "folio_shared";
  }
}