package com.google.cloud.reseller.subscription.samples;

import static com.google.cloud.reseller.subscription.samples.Constants.ELIGIBILITY_ID;
import static com.google.cloud.reseller.subscription.samples.Constants.NFT_PRODUCT_ID;
import static com.google.cloud.reseller.subscription.samples.Constants.PARTNER_ID;
import static com.google.cloud.reseller.subscription.samples.Constants.PLAN_TYPE;
import static com.google.cloud.reseller.subscription.samples.Constants.PRODUCT_ID;
import static com.google.cloud.reseller.subscription.samples.Constants.PROMOTION;
import static com.google.cloud.reseller.subscription.samples.Constants.PROMOTION_SUB_LEVEL;
import static com.google.cloud.reseller.subscription.samples.Constants.REGION_CODE;
import static com.google.cloud.reseller.subscription.samples.Constants.SUB_ID;
import static com.google.cloud.reseller.subscription.samples.Constants.TARGET_SERVICE_ACCOUNT_EMAIL;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.paymentsresellersubscription.v1.PaymentsResellerSubscription;
import com.google.api.services.paymentsresellersubscription.v1.model.CreateSubscriptionIntent;
import com.google.api.services.paymentsresellersubscription.v1.model.GenerateUserSessionRequest;
import com.google.api.services.paymentsresellersubscription.v1.model.GenerateUserSessionResponse;
import com.google.api.services.paymentsresellersubscription.v1.model.IntentPayload;
import com.google.api.services.paymentsresellersubscription.v1.model.Location;
import com.google.api.services.paymentsresellersubscription.v1.model.ProductPayload;
import com.google.api.services.paymentsresellersubscription.v1.model.Subscription;
import com.google.api.services.paymentsresellersubscription.v1.model.SubscriptionLineItem;
import com.google.api.services.paymentsresellersubscription.v1.model.SubscriptionPromotionSpec;
import com.google.api.services.paymentsresellersubscription.v1.model.YoutubePayload;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class GenerateUserSession {

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public static void main(String[] args) throws IOException {
    List<String> scopes = Collections.singletonList(
        "https://www.googleapis.com/auth/youtube.commerce.partnership.integrated-billing");

    ImpersonateServiceAccount impersonateServiceAccount = new ImpersonateServiceAccount(scopes, TARGET_SERVICE_ACCOUNT_EMAIL);
    impersonateServiceAccount.refreshCredentials();

    // call provision API using service account credentials
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
        impersonateServiceAccount.getImpersonatedCredentials());
      PaymentsResellerSubscription client = new PaymentsResellerSubscription.Builder(
          HTTP_TRANSPORT, GsonFactory.getDefaultInstance(),
          requestInitializer
      ).setRootUrl("https://preprod-paymentsresellersubscription.googleapis.com").build();
      System.out.println("partners/" + PARTNER_ID + "/products/" + NFT_PRODUCT_ID);
      DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(
          ZoneOffset.UTC); // RFC 3339, with offset
      String subscriptionId = SUB_ID + Instant.now().toEpochMilli();
      GenerateUserSessionResponse generateUserSessionResponse = client.partners()
          .userSessions().generate("partners/" + PARTNER_ID,
              new GenerateUserSessionRequest().setIntentPayload(
                  new IntentPayload().setCreateIntent(
                      new CreateSubscriptionIntent().setParent(
                              "partners/" + PARTNER_ID).setSubscriptionId(subscriptionId)
                          .setSubscription(
                              new Subscription()
                                  .setLineItems(
                                      ImmutableList
                                          .of(new SubscriptionLineItem()
                                              .setProduct("partners/" + PARTNER_ID + "/products/"
                                                  + NFT_PRODUCT_ID)
                                              .setProductPayload(
                                                  new ProductPayload()
                                                      .setYoutubePayload(
                                                          new YoutubePayload()
                                                              .setPartnerEligibilityIds(
                                                                  List.of(ELIGIBILITY_ID))
                                                              .setPartnerPlanType(PLAN_TYPE)
                                                      ))
                                          //    .setLineItemPromotionSpecs(ImmutableList.of(new SubscriptionPromotionSpec().setPromotion("partners/" + PARTNER_ID + "/promotions/" + PROMOTION)))
                                          ))
                                  .setPartnerUserToken("g1.tvc.test2@gmail.com")
                                  .setServiceLocation(
                                      new Location()
                                          .setPostalCode("94043")
                                          .setRegionCode(REGION_CODE))
                                  .setPurchaseTime(formatter.format(Instant.now()))
                               //   .setPromotionSpecs(ImmutableList.of(new SubscriptionPromotionSpec().setPromotion("partners/" + PARTNER_ID + "/promotions/" + PROMOTION_SUB_LEVEL)))
                          )))).execute();
      System.out.println("Subscription Id::" + subscriptionId);
      System.out.printf("Sandbox URL to create subscription:: https://serviceactivation.sandbox.google.com/subscription/new/%s \n", generateUserSessionResponse.getUserSession().getToken());

  }
}
// mvn clean compile
// mvn exec:java -Dexec.mainClass="com.google.cloud.reseller.subscription.samples.GenerateUserSession"

