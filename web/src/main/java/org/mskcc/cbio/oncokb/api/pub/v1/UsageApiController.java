package org.mskcc.cbio.oncokb.api.pub.v1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import org.springframework.http.HttpStatus;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mskcc.cbio.oncokb.config.annotation.PremiumPublicApi;
import org.mskcc.cbio.oncokb.model.usageAnalysis.UsageSummary;
import org.mskcc.cbio.oncokb.model.usageAnalysis.UserOverviewUsage;
import org.mskcc.cbio.oncokb.model.usageAnalysis.UserUsage;
import org.mskcc.cbio.oncokb.util.PropertiesUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.*;

/**
 * Created by Yifu Yao on 2020-10-28
 */
@Api(tags = "Usage", description = "Oncokb Usage Analysis")
@Controller
public class UsageApiController {
    
    final String s3AccessKey = PropertiesUtils.getProperties("aws.s3.accessKey");
    final String s3SecretKey = PropertiesUtils.getProperties("aws.s3.secretKey");
    final String s3Region = PropertiesUtils.getProperties("aws.s3.region");

    @PremiumPublicApi
    @ApiOperation("Return user id based usage summary")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = UserUsage.class),
        @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value="/usage/user", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<UserUsage> userUsageGet(
        @ApiParam(value = "", required = true) @RequestParam(value = "", required = true) String userId)
        throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Region).build();

        S3Object s3object = s3client.getObject("oncokb", "usage-analysis/userSummary.json");
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

        if (jsonObject.containsKey(userId)){
            JSONObject usageObject = (JSONObject)jsonObject.get(userId);
            Gson gson = new Gson();
            UserUsage userUsage = gson.fromJson(usageObject.toString(), UserUsage.class);
            return new ResponseEntity<UserUsage>(userUsage, status);
        }

        return new ResponseEntity<UserUsage>(new UserUsage(), status);
        
    }

    @PremiumPublicApi
    @ApiOperation("Return user id based usage summary")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", responseContainer = "List"),
        @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value="/usage/user/overview", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<UserOverviewUsage>> userOverviewUsageGet()
        throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Region).build();

        S3Object s3object = s3client.getObject("oncokb", "usage-analysis/userSummary.json");
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        
        List<UserOverviewUsage> result = new ArrayList<>();
        for (Object item: jsonObject.keySet()){
            String id = (String) item;
            JSONObject usageObject = (JSONObject)jsonObject.get(id);
            Gson gson = new Gson();
            UserUsage userUsage = gson.fromJson(usageObject.toString(), UserUsage.class); 
            UserOverviewUsage cur = new UserOverviewUsage();
            cur.setUserId(id);
            cur.setUserEmail(userUsage.getUserEmail());
            
            String endpoint = "";
            int maxUsage = 0;
            int totalUsage = 0;
            Map<String,Integer> summary = userUsage.getSummary().getYear();
            for (String resource: summary.keySet()){
                totalUsage += summary.get(resource);
                if (summary.get(resource) > maxUsage){
                    endpoint = resource;
                    maxUsage = summary.get(resource);
                }
            }
            cur.setTotalUsage(totalUsage);
            cur.setEndpoint(endpoint);
            cur.setMaxUsage(maxUsage);

            result.add(cur);
        }

        return new ResponseEntity<List<UserOverviewUsage>>(result, status);
        
    }

    @PremiumPublicApi
    @ApiOperation("Return resource based usage summary")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = UsageSummary.class),
        @ApiResponse(code = 400, message = "Error", response = String.class) })
    @RequestMapping(value="/usage/resource", produces = { "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<UsageSummary> resourceUsageGet()
        throws IOException, ParseException {
        HttpStatus status = HttpStatus.OK;

        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey, s3SecretKey);
        AmazonS3 s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(s3Region).build();

        S3Object s3object = s3client.getObject("oncokb", "usage-analysis/resourceSummary.json");
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

        Gson gson = new Gson();
        UsageSummary summary = gson.fromJson(jsonObject.toString(), UsageSummary.class);

        return new ResponseEntity<UsageSummary>(summary, status);
    }
}