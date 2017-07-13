package com.example;

import static org.springframework.http.RequestEntity.*;

import java.net.URI;
import java.util.Collections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @see https://github.com/Pivotal-Japan/spring-security-oauth-workshop/blob/master/cli-application.md
 */
@SpringBootApplication
@ConfigurationProperties(prefix = "cli")
public class TweeterCliApplication implements CommandLineRunner
{
  private URI accessTokenUri;
  private String clientId;
  private String clientSecret;
  private URI tweetApiUri;
  private String username;
  private String password;
  private String text;

  public static void main(String[] args)
  {
    SpringApplication.run(TweeterCliApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception
  {
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    if (getUsername() == null) {
      System.out.print("Enter username : ");
      setUsername(System.console().readLine());
    }
    if (getPassword() == null) {
      System.out.print("Enter password : ");
      setPassword(String.valueOf(System.console().readPassword()));
    }
    if (getText() == null) {
      System.out.print("Enter text : ");
      setText(System.console().readLine());
    }

    // Get Access Token
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("username", getUsername());
    body.add("password", getPassword());
    body.add("scope", "tweet.read tweet.write");

    JsonNode token = restTemplate.exchange(post(getAccessTokenUri())
        .header("Authorization",
            "Basic " + Base64Utils.encodeToString(
                (getClientId() + ":" + getClientSecret()).getBytes()))
        .body(body), JsonNode.class).getBody();
    String accessToken = token.get("access_token").asText();

    if (!StringUtils.isEmpty(getText())) {
      // Post Tweet
      restTemplate
          .exchange(
              post(UriComponentsBuilder.fromUri(getTweetApiUri())
                  .pathSegment("tweets").build().toUri())
                      .header("Authorization",
                          "Bearer " + accessToken)
                      .body(Collections.singletonMap("text",
                          getText())),
              JsonNode.class);
    }

    // Get Timelines
    JsonNode timelines = restTemplate.exchange(
        get(UriComponentsBuilder.fromUri(getTweetApiUri())
            .pathSegment("timelines").build().toUri())
                .header("Authorization", "Bearer " + accessToken).build(),
        JsonNode.class).getBody();

    timelines.forEach(tweet -> {
      System.out.println("=========");
      System.out.println("Name: " + tweet.get("username").asText());
      System.out.println("Text: " + tweet.get("text").asText());
      System.out.println("Date: " + tweet.get("createdAt").asText());
    });
  }

  public URI getAccessTokenUri()
  {
    return accessTokenUri;
  }

  public void setAccessTokenUri(URI accessTokenUri)
  {
    this.accessTokenUri = accessTokenUri;
  }

  public String getClientId()
  {
    return clientId;
  }

  public void setClientId(String clientId)
  {
    this.clientId = clientId;
  }

  public String getClientSecret()
  {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret)
  {
    this.clientSecret = clientSecret;
  }

  public URI getTweetApiUri()
  {
    return tweetApiUri;
  }

  public void setTweetApiUri(URI tweetApiUri)
  {
    this.tweetApiUri = tweetApiUri;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }
}
