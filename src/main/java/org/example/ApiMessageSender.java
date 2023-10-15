package com.journaldev.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ApiMessageSender {

  private static String logs = "";

  public static String getLogs() {
    return logs;
  }

  public static String getTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Date now = new Date();
    String formattedTimestamp = sdf.format(now);
    return "[" + formattedTimestamp + "] ";
  }

  private static String getUploadLink(String token, String domain) throws IOException {
    URL obj = new URL(
        "https://" + domain + "/method/video.getVideoMessageUploadInfo?lang=ru&shape_id=1&v=5.207");
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestProperty("authorization", "Bearer " + token);
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    JSONObject responseJSON = new JSONObject(response.toString());
    logs += getTimeStamp() + responseJSON + "\n\n";
    return responseJSON.getJSONObject("response").getString("upload_url");
  }

  private static String sendVideoToServer(String token, String filepath, String domain) {
    String videoMsg = null;
    String responseBody = null;
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      File file = new File(filepath);
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.addPart("file", new FileBody(file));
      HttpUriRequest request = RequestBuilder.post()
          .setUri(getUploadLink(token, domain))
          .setEntity(builder.build()).build();
      CloseableHttpResponse response = httpClient.execute(request);

      responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
      JSONObject responseJSON = new JSONObject(responseBody);
      videoMsg = responseJSON.getInt("owner_id") + "_" + responseJSON.getInt("video_id");
    } catch (Exception e) {
      System.out.println("Что-то пошло не так. Были записаны логи.");
    }
    logs += getTimeStamp() + responseBody + "\n\n";
    return videoMsg;
  }

  public static String sendVideoMsg(String peerId, String token, String filepath, String domain)
      throws IOException {
    URL obj = new URL(
        "https://" + domain + "/method/messages.send?attachment=video_message" + sendVideoToServer(
            token, filepath, domain) + "&peer_id=" + peerId + "&v=5.207&random_id=0");
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
    con.setRequestProperty("authorization", "Bearer " + token);
    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    JSONObject responseJSON = new JSONObject(response.toString());
    logs += getTimeStamp() + responseJSON + "\n\n";
    return String.valueOf(responseJSON.getInt("response"));
  }
}