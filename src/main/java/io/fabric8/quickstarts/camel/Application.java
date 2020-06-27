/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.http.common.HttpMessage;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.apache.camel.impl.DefaultAttachment;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spi.Registry;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.fabric8.quickstarts.camel.instructor.Params;
import io.fabric8.quickstarts.camel.pojo.Root;
import io.fabric8.quickstarts.camel.pojo.UnAuthorize;
import io.fabric8.quickstarts.camel.voice.Result;
import io.fabric8.quickstarts.camel.voice.VoiceSession;

import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

@SpringBootApplication
@ImportResource({ "classpath:spring/camel-context.xml" })
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(),
                "/camel-rest-sql/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Component
    class RestApi extends RouteBuilder {

        @Override
        public void configure() {

            onException(HttpOperationFailedException.class).handled(true).process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                    // exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
                    UnAuthorize test = new UnAuthorize();
                    test.setResult("You are UnAuthrized to access such API");
                    exchange.getIn().setBody(test);
                }
            });

            restConfiguration()

                    .contextPath("/camel-rest-sql").apiContextPath("/api-doc")
                    .apiProperty("api.title", "Camel REST API").apiProperty("api.version", "1.0")
                    .apiProperty("cors", "true").apiContextRouteId("doc-api").enableCORS(true)
                    .corsHeaderProperty("Access-Control-Allow-Headers",
                            "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers,CustomHeader1, CustomHeader2")
                    .component("servlet").bindingMode(RestBindingMode.json);

            rest("/").description("Exhange Rate REST service").get("drools/")
                    .description("The drools for specified currency").route().routeId("drools-rate-api")
                    .to("log:DEBUG?showBody=true&showHeaders=true").process(new Processor() {
                        public void process(Exchange exchange) throws Exception {

                            p test = new p();

                            // test.setName("Omar");
                            // test.setAge(80);
                            // test.setPrevious(false);

                            test.setBilledAmount(170);
                            test.setCreditHistory("Poor");
                            test.setMemberType("Silver");

                            System.out.println("Arrived from Space jBPM");

                            // exchange.getIn().setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"
                            // standalone=\"yes\"?>" +
                            // "<person><id>1</id><lastName>Atieh</lastName><firstName>Omar</firstName></person>");

                            // exchange.getIn().setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"
                            // standalone=\"yes\"?>" +
                            // "<person><age>25</age><name>Post john</name></person>");
                            exchange.getIn().setBody(test);

                            // Person : {
                            // "id": 10,
                            // "firstName": "Omar",
                            // "lastName": "Atieh"
                            // }

                        }
                    }).endRest()
                    
                    .get("elearningapp/").description("The e-learning for specified currency").param()
                    .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("elearning")
                    .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
                    .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")

                    // .marshal().json(JsonLibrary.Jackson,Params.class)

                    // .log("${body}")

                    .process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            String results = exchange.getIn().getBody(String.class);
                            JSONArray jsonObject = new JSONArray(results);
                            // Map out=(Map) results.get(0);
                            System.out.println(jsonObject.getJSONObject(0).getString("username"));
                            exchange.getIn().setHeader("password", jsonObject.getJSONObject(0).getString("password"));
                            exchange.getIn().setHeader("username", jsonObject.getJSONObject(0).getString("username"));

                            // exchange.getIn().setHeader("password", out.get("password"));

                        }
                    }).log("${header.username}").log("${header.password}");
                   
            //         .to("sql:select u.id as user_id, u.name as user_name, u.username as user_username, u.status as user_status, u.logo, u.type as user_type, c.class_name, c.class_section, u.foreign_class_id from user u, classes c where u.username=:#${header.username} and password=:#${header.password} and u.foreign_class_id=c.id?"
            //                 + "dataSource=dataSource")

            //         .log("${body}").process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //                 System.out.println(results);
            //                 io.fabric8.quickstarts.camel.login.Result output = new io.fabric8.quickstarts.camel.login.Result();
            //                 ArrayList<io.fabric8.quickstarts.camel.login.Params>  resulted = new ArrayList<io.fabric8.quickstarts.camel.login.Params>();
                            
            //                 String response = null;
            //                 if (!results.isEmpty()) {
            //                     System.out.println("size"+results.size());

            //                     for (int i = 0; i < results.size(); i++) {
            //                         Map out = (Map) results.get(i);
            //                         io.fabric8.quickstarts.camel.login.Params params = new io.fabric8.quickstarts.camel.login.Params();
                                  
            //                         params.setUser_id((Integer)out.get("user_id"));
            //                         exchange.getIn().setHeader("user_name", out.get("user_name"));
            //                         params.setUser_name((String)out.get("user_name"));
            //                         params.setUser_username((String)out.get("user_username"));
            //                         params.setUser_status((String)out.get("user_status"));
            //                         params.setUser_type((String)out.get("user_type"));
                                    
            //                         params.setClass_name((String)out.get("class_name"));
            //                         params.setClass_section((String)out.get("class_section"));
            //                         params.setLogo((String)out.get("logo"));
            //                         params.setUser_class_id((Integer)out.get("foreign_class_id"));


    
    
            //                         exchange.getIn().setHeader("status", "success");
                                  
    

                                    
            //                         resulted.add(params);


            //                     }
            //                     output.setResult(resulted);

            //                     output.setStatus("success");
            //                 }
            //                 exchange.getIn().setBody(output);

                        

            //             }
            //         }).log("${body}").choice().when(body().isNotNull())
            //         .marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.login.Result.class).log("${body}")
            //         .unmarshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.login.Result.class)

                    
                    
                    
                    
                    
            //         .end()


            //         .choice().when(simple("${header.status} != 'success'")).process(new Processor() {

            //             @Override
            //             public void process(Exchange exchange) throws Exception {
            //                 // TODO Auto-generated method stub
            //                 io.fabric8.quickstarts.camel.login.Result result= new io.fabric8.quickstarts.camel.login.Result();
            //               //  io.fabric8.quickstarts.camel.login.Params out = new io.fabric8.quickstarts.camel.login.Params();
            //                 result.setResult(null);
            //                 result.setStatus("failed");
            //                 // JSONArray out1= new JSONArray();
            //                 // out1.put(test);
            //                 // exchange.getIn().setBody(out1.toString());
            //                 exchange.getIn().setBody(result);

            //             }
            //         }).marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.login.Result.class)
            //         .unmarshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.login.Result.class)

            //         .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")
            //         .end()

            //         /*
            //          * .process(new Processor() { public void process(Exchange exchange) throws
            //          * Exception {
            //          * 
            //          * Points test= (Points) exchange.getIn().getBody(Points.class);
            //          * 
            //          */

            //         // System.out.println("number of Points"+test.getPoints());

            //         // exchange.getIn().setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"
            //         // standalone=\"yes\"?>" +
            //         // "<person><id>1</id><lastName>Atieh</lastName><firstName>Omar</firstName></person>");

            //         // exchange.getIn().setBody("<?xml version=\"1.0\" encoding=\"UTF-8\"
            //         // standalone=\"yes\"?>" +
            //         // "<person><age>25</age><name>Post john</name></person>");
            //         // exchange.getIn().setBody("number of loyalty Points added to Subscriber
            //         // 011-60-3-2168-5000 is "+test.getPoints());

            //         // Person : {
            //         // "id": 10,
            //         // "firstName": "Omar",
            //         // "lastName": "Atieh"
            //         // }

            //         // }
            //         // })
            //         .endRest()
                    
                    
                    
            //         .get("getimagesforchapter/").description("The e-learning for specified currency").param()
            //         .name("data").type(RestParamType.query).dataType("String").endParam().route()
            //         .routeId("Getimagesforchapter ").to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //         .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")

            //         // .marshal().json(JsonLibrary.Jackson,Params.class)

            //         // .log("${body}")

            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 String results = exchange.getIn().getBody(String.class);
            //                 JSONArray jsonObject = new JSONArray(results);
            //                 // Map out=(Map) results.get(0);
            //                 System.out.println(jsonObject.getJSONObject(0).getInt("class_id"));
            //                 exchange.getIn().setHeader("class_id",
            //                         jsonObject.getJSONObject(0).getInt("class_id"));
            //                         exchange.getIn().setHeader("material_id",
            //                         jsonObject.getJSONObject(0).getInt("material_id"));
            //                         exchange.getIn().setHeader("chapter_id",
            //                         jsonObject.getJSONObject(0).getInt("chapter_id"));
            //                 // exchange.getIn().setHeader("username",
            //                 // jsonObject.getJSONObject(0).getString("username"));

            //                 // exchange.getIn().setHeader("password", out.get("password"));

            //             }
            //         }).log("${header.session_title}")
            //         .to("sql:select * from session_content_images where foreign_class_id=:#${header.class_id} and foreign_material_id=:#${header.material_id} and foreign_chapter_id=:#${header.chapter_id} order by id desc?"
            //                 + "dataSource=dataSource")
            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //                 System.out.println(results);
            //                 io.fabric8.quickstarts.camel.Images.Result output = new io.fabric8.quickstarts.camel.Images.Result();
            //                 ArrayList<io.fabric8.quickstarts.camel.Images.Params>  resulted = new ArrayList<io.fabric8.quickstarts.camel.Images.Params>();
                            
            //                 String response = null;
            //                 if (!results.isEmpty()) {
            //                     System.out.println("size"+results.size());

            //                     for (int i = 0; i < results.size(); i++) {
            //                         Map out = (Map) results.get(i);
            //                         io.fabric8.quickstarts.camel.Images.Params params = new io.fabric8.quickstarts.camel.Images.Params();
            //                         params.setImage_id((Integer)out.get("id"));
            //                         params.setCreation_date((String)out.get("creation_date").toString());
            //                         params.setImage_title((String)out.get("image_title"));
            //                         params.setSession_image((String)out.get("session_image"));


                                    
            //                         resulted.add(params);


            //                     }
            //                     output.setResult(resulted);

            //                     output.setStatus("success");
            //                 }
            //                 exchange.getIn().setBody(output);

            //             }

                        
            //         })
            //         .log("${body}").marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.Images.Result.class)
            //         .unmarshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.Images.Result.class)
            //         // .log("${header.password}")
            //         .endRest();
            // rest("/uploadimage").post().bindingMode(RestBindingMode.off).consumes("multipart/form-data").route()
            //         .routeId("upload_info")

            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {

            //                 InputStream iso = exchange.getIn().getBody(InputStream.class);
            //                 InputStream cloned = clone_omar(iso);
            //                 InputStream clone1 = clone_omar(iso);
            //                 String theString = IOUtils.toString(cloned, "UTF-8");

            //                 MultiPartStringParser test = new MultiPartStringParser(theString);
            //                 MimeBodyPart mimeMessage1 = new MimeBodyPart(clone1);
            //                 DataHandler dro = mimeMessage1.getDataHandler();

            //                 System.out.println("filename " + dro.getName());
            //                 exchange.getIn().setHeader(Exchange.FILE_NAME, dro.getName());
            //                 exchange.getIn().setBody(dro.getInputStream());

            //                 String class_id = test.getParameters().get("class_id");
            //                 String material_id = test.getParameters().get("material_id");
            //                 String chapter_id = test.getParameters().get("chapter_id");
            //                 String image_title = test.getParameters().get("image_title");
            //                 System.out.println(String.format("Body Content-Type: \n  session_title %s\n image_title %s",
            //                 class_id, material_id));
            //                 exchange.getIn().setHeader("filename", dro.getName());
            //                 exchange.getIn().setHeader("class_id", class_id);
            //                 exchange.getIn().setHeader("image_title", image_title);
            //                 exchange.getIn().setHeader("chapter_id", chapter_id);
            //                 exchange.getIn().setHeader("material_id", material_id);
                            

            //                 exchange.getIn().setBody(dro.getInputStream());

            //             }
            //         })

            //         .to("file:/Users/omar/Downloads/elearn")
            //         .to("sql:insert into session_content_images (foreign_class_id,foreign_material_id,foreign_chapter_id,image_title,session_image) values "
            //                 + "(:#${header.class_id} , :#${header.material_id},:#${header.chapter_id},:#${header.image_title},:#${header.filename})?"
            //                 + "dataSource=dataSource")
            //         .to("direct:start")

            //         .endRest();

            // rest("/uploadvoice").post().bindingMode(RestBindingMode.off).consumes("multipart/form-data").route()
            //         .routeId("upload_voice")

            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {

            //                 InputStream iso = exchange.getIn().getBody(InputStream.class);
            //                 InputStream cloned = clone_omar(iso);
            //                 InputStream clone1 = clone_omar(iso);
            //                 String theString = IOUtils.toString(cloned, "UTF-8");

            //                 MimeBodyPart mimeMessage1 = new MimeBodyPart(clone1);
            //                 DataHandler dro = mimeMessage1.getDataHandler();
                         
            //                 InputStream myInputStream = new ByteArrayInputStream(
            //                     Base64.decodeBase64("" + dro.getContent()));
            //                    System.out.println("base64 " + dro.getContent());
            //                    exchange.getIn().setBody(myInputStream);
                              
                        
                         
            //                 MultiPartStringParser test = new MultiPartStringParser(theString);
                         

            //                String class_name = test.getParameters().get("class_name");
            //                 String class_section = test.getParameters().get("class_section");
            //                 String class_id = test.getParameters().get("class_id");
            //                 String material_id = test.getParameters().get("material_id");
            //                 String chapter_id = test.getParameters().get("chapter_id");
            //                 String material_name = test.getParameters().get("material_name");
            //                 String chapter_subject = test.getParameters().get("chapter_subject");
            //                 String chapter_number = test.getParameters().get("chapter_number");
            //                 String session_image = test.getParameters().get("session_image");
            //               //  String filename = dro.getName();
                         
            //               System.out.println("voice64ttttt " + class_name+class_section+class_id+material_id+chapter_id+material_name+chapter_subject+chapter_number+session_image);
            //               String voice64 = test.getParameters().get("voice64");

                         
            //                 String image_id = test.getParameters().get("image_id");
            //                 String image_title = test.getParameters().get("image_title");
            //                 String voice_id = test.getParameters().get("voice_id");

            //                 String duration = test.getParameters().get("duration");

            //                 //voice_id


            //                 String[] filename_split = voice_id.split("\\$");
            //                 for (int i = 0; i < filename_split.length; i++)
            //                     System.out.println("mambo\n " + i + " try " + filename_split[i]);






                          

            //                 exchange.getIn().setHeader("filename", voice_id);
            //                 exchange.getIn().setHeader(Exchange.FILE_NAME, filename_split[1]);

            //                 exchange.getIn().setHeader("class_name", class_name);
            //                 exchange.getIn().setHeader("class_section", class_section);
            //                 exchange.getIn().setHeader("class_id", class_id);

            //                 exchange.getIn().setHeader("material_id", material_id);
            //                 exchange.getIn().setHeader("chapter_id", chapter_id);
            //                 exchange.getIn().setHeader("material_name", material_name);

            //                 exchange.getIn().setHeader("chapter_subject", chapter_subject);
            //                 exchange.getIn().setHeader("chapter_number", chapter_number);
            //                 exchange.getIn().setHeader("session_image", session_image);

            //                 exchange.getIn().setHeader("image_id", image_id);
            //                 exchange.getIn().setHeader("image_title", image_title);
            //                 exchange.getIn().setHeader("duration", duration);
            //                 exchange.getIn().setHeader("voice_id", voice_id);

            //                 // InputStream myInputStream = new
            //                 // ByteArrayInputStream(Base64.decodeBase64(theString));

            //                 // exchange.getIn().setBody(iso);//dro.getInputStream());

            //             }
            //         })

            //         .to("file:/Users/omar/Downloads/elearn")
            //         .to("sql:insert into session_content_voices (foreign_class_id,foreign_material_id,foreign_chapter_id,foreign_image_id,session_voice,duration) values "
            //                 + "(:#${header.class_id} , :#${header.material_id},:#${header.chapter_id},:#${header.image_id},:#${header.filename},:#${header.duration})?"
            //                 + "dataSource=dataSource")

            //         .to("direct:start").endRest();

            // rest("/getvoicesidforimage").get().bindingMode(RestBindingMode.off).produces("application/json").param()
            //         .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("Getvoicesidforimage")
            //         .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //         .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")
            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 String results = exchange.getIn().getBody(String.class);
            //                 JSONArray jsonObject = new JSONArray(results);
            //                 // Map out=(Map) results.get(0);
            //                 System.out.println(jsonObject.getJSONObject(0).getInt("class_id"));
            //                 exchange.getIn().setHeader("class_id",
            //                         jsonObject.getJSONObject(0).getInt("class_id"));
            //                 exchange.getIn().setHeader("material_id", jsonObject.getJSONObject(0).getInt("material_id"));
            //                 exchange.getIn().setHeader("chapter_id", jsonObject.getJSONObject(0).getInt("chapter_id"));
            //                 exchange.getIn().setHeader("image_id", jsonObject.getJSONObject(0).getInt("image_id"));

            //                 // exchange.getIn().setHeader("password", out.get("password"));

            //             }
            //         }).log("${header.session_title}")
            //         .to("sql:select * from session_content_voices where foreign_class_id=:#${header.class_id} and foreign_material_id=:#${header.material_id} and foreign_chapter_id=:#${header.chapter_id} and foreign_image_id=:#${header.image_id}?"
            //                 + "dataSource=dataSource")

            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //                 System.out.println(results);
            //                 io.fabric8.quickstarts.camel.voice.Result output = new io.fabric8.quickstarts.camel.voice.Result();
            //                 ArrayList<VoiceSession>  resulted = new ArrayList<VoiceSession>();
                            
            //                 String response = null;
            //                 if (!results.isEmpty()) {
            //                     System.out.println("size"+results.size());

            //                     for (int i = 0; i < results.size(); i++) {
            //                         Map out = (Map) results.get(i);
            //                         VoiceSession params = new VoiceSession();
            //                         params.setVoiceid((String)out.get("session_voice"));
            //                         params.setCreation_date((String)out.get("creation_date").toString());
            //                         params.setDuration((Integer)out.get("duration"));

            //                         System.out.println(""+params.getVoiceid()+params.getCreation_date());
                                    
            //                         resulted.add(params);


            //                     }
            //                     output.setResult(resulted);

            //                     output.setStatus("success");
            //                 }
            //                 exchange.getIn().setBody(output);

            //             }
            //         })
            //         .marshal().json(JsonLibrary.Jackson, Result.class)

            //         .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")

            //       .endRest();

            //     rest("/getintructorclassesandmaterials").get().bindingMode(RestBindingMode.off).produces("application/json").param()
            //       .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("getintructorclassesandmaterials")
            //       .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //       .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")
            //       .process(new Processor() {
            //           public void process(Exchange exchange) throws Exception {
            //               String results = exchange.getIn().getBody(String.class);
            //               JSONArray jsonObject = new JSONArray(results);
            //               // Map out=(Map) results.get(0);
            //               System.out.println(jsonObject.getJSONObject(0).getInt("instructor_id"));
            //               exchange.getIn().setHeader("instructor_id",
            //                       jsonObject.getJSONObject(0).getInt("instructor_id"));

            //               // exchange.getIn().setHeader("password", out.get("password"));

            //           }
            //       }).log("${header.instructor_id}")
            //       .to("sql:select m.id as material_id, m.material_name, m.lang, m.cover_image, c.id as class_id, c.class_name, c.class_section from materials m,classes c where m.id in (select foreign_material_id from materials_instructors where foreign_instructor_id=:#${header.instructor_id}) and c.id in (select foreign_class_id from materials_instructors where foreign_instructor_id=:#${header.instructor_id} and foreign_material_id=m.id)?"
            //               + "dataSource=dataSource")

            //       .process(new Processor() {
            //           public void process(Exchange exchange) throws Exception {
            //             ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //             System.out.println(results);
            //             io.fabric8.quickstarts.camel.instructor.Result output = new io.fabric8.quickstarts.camel.instructor.Result();
            //             ArrayList<io.fabric8.quickstarts.camel.instructor.Params>  resulted = new ArrayList<io.fabric8.quickstarts.camel.instructor.Params>();
                        
            //             String response = null;
            //             if (!results.isEmpty()) {
            //                 System.out.println("size"+results.size());

            //                 for (int i = 0; i < results.size(); i++) {
            //                     Map out = (Map) results.get(i);
            //                     io.fabric8.quickstarts.camel.instructor.Params params = new io.fabric8.quickstarts.camel.instructor.Params();
            //                     params.setMaterial_id((Integer)out.get("material_id"));
            //                     params.setMaterial_name((String)out.get("material_name"));
            //                     params.setLang((String)out.get("lang"));
            //                     params.setCover_image((String)out.get("cover_image"));
            //                     params.setClass_id((Integer)out.get("class_id"));

            //                     params.setClass_name((String)out.get("class_name"));
            //                     params.setClass_section((String)out.get("class_section"));

            //                     System.out.println(""+params.getClass_name()+params.getMaterial_name());
                                
            //                     resulted.add(params);


            //                 }
            //                 output.setResult(resulted);

            //                 output.setStatus("success");
            //             }
            //             exchange.getIn().setBody(output);

            //           }
            //       })
            //       .log("${body}").marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.instructor.Result.class)

            //       .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")

            //     .endRest();

            //           rest("/getstudentmaterials").get().bindingMode(RestBindingMode.off).produces("application/json").param()
            //       .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("Getstudentmaterials")
            //       .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //       .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")
            //       .process(new Processor() {
            //           public void process(Exchange exchange) throws Exception {
            //               String results = exchange.getIn().getBody(String.class);
            //               JSONArray jsonObject = new JSONArray(results);
            //               // Map out=(Map) results.get(0);
            //               System.out.println(jsonObject.getJSONObject(0).getInt("class_id"));
            //               exchange.getIn().setHeader("class_id",
            //                       jsonObject.getJSONObject(0).getInt("class_id"));

            //               // exchange.getIn().setHeader("password", out.get("password"));

            //           }
            //       }).log("${header.instructor_id}")
            //       .to("sql:select m.id as material_id, m.material_name, m.lang, m.cover_image, u.id as instructor_id, u.name as instructor_name from materials m, user u, materials_instructors mi where m.id = mi.foreign_material_id and mi.foreign_class_id=:#${header.class_id} and u.id in (select foreign_instructor_id from materials_instructors where foreign_class_id=:#${header.class_id} and foreign_material_id=m.id)?"
            //               + "dataSource=dataSource")

            //       .process(new Processor() {
            //           public void process(Exchange exchange) throws Exception {
            //             ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //             System.out.println(results);
            //             io.fabric8.quickstarts.camel.materials.Result output = new io.fabric8.quickstarts.camel.materials.Result();
            //             ArrayList<io.fabric8.quickstarts.camel.materials.Params>  resulted = new ArrayList<io.fabric8.quickstarts.camel.materials.Params>();
                        
            //             String response = null;
            //             if (!results.isEmpty()) {
            //                 System.out.println("size"+results.size());

            //                 for (int i = 0; i < results.size(); i++) {
            //                     Map out = (Map) results.get(i);
            //                     io.fabric8.quickstarts.camel.materials.Params params = new io.fabric8.quickstarts.camel.materials.Params();
            //                     params.setMaterial_id((Integer)out.get("material_id"));
            //                     params.setMaterial_name((String)out.get("material_name"));
            //                     params.setLang((String)out.get("lang"));
            //                     params.setCover_image((String)out.get("cover_image"));
            //                     params.setInstructor_id((Integer)out.get("instructor_id"));

            //                     params.setInstructor_name((String)out.get("instructor_name"));

            //                     System.out.println(""+params.getInstructor_name()+params.getMaterial_name());
                                
            //                     resulted.add(params);


            //                 }
            //                 output.setResult(resulted);

            //                 output.setStatus("success");
            //             }
            //             exchange.getIn().setBody(output);

            //           }
            //       })
            //       .log("${body}").marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.materials.Result.class)

            //       .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")

            //     .endRest();



            //     rest("/getchapters").get().bindingMode(RestBindingMode.off).produces("application/json").param()
            //     .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("Getchapters")
            //     .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //     .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")
            //     .process(new Processor() {
            //         public void process(Exchange exchange) throws Exception {
            //             String results = exchange.getIn().getBody(String.class);
            //             JSONArray jsonObject = new JSONArray(results);
            //             // Map out=(Map) results.get(0);
            //             System.out.println(jsonObject.getJSONObject(0).getInt("class_id"));
            //             exchange.getIn().setHeader("class_id",
            //                     jsonObject.getJSONObject(0).getInt("class_id"));
            //                     exchange.getIn().setHeader("material_id",
            //                     jsonObject.getJSONObject(0).getInt("material_id"));

            //             // exchange.getIn().setHeader("password", out.get("password"));

            //         }
            //     }).log("${header.class_id}")
            //     .to("sql:select * from chapters where foreign_material_id=:#${header.material_id}  and foreign_class_id=:#${header.class_id}  order by id asc?"
            //             + "dataSource=dataSource")

            //     .process(new Processor() {
            //         public void process(Exchange exchange) throws Exception {
            //           ArrayList results = exchange.getIn().getBody(ArrayList.class);
            //           System.out.println(results);
            //           io.fabric8.quickstarts.camel.chapters.Result output = new io.fabric8.quickstarts.camel.chapters.Result();
            //           ArrayList<io.fabric8.quickstarts.camel.chapters.Params>  resulted = new ArrayList<io.fabric8.quickstarts.camel.chapters.Params>();
                      
            //           String response = null;
            //           if (!results.isEmpty()) {
            //               System.out.println("size"+results.size());

            //               for (int i = 0; i < results.size(); i++) {
            //                   Map out = (Map) results.get(i);
            //                   io.fabric8.quickstarts.camel.chapters.Params params = new io.fabric8.quickstarts.camel.chapters.Params();
            //                   params.setChapter_id((Integer)out.get("id"));
            //                   params.setChapter_number((String)out.get("chapter_number"));
            //            //       params.setLang((String)out.get("lang"));
            //                   params.setChapter_subject((String)out.get("chapter_subject"));
            //                   params.setInstructor_id((Integer)out.get("foreign_instructor_id"));
            //                   params.setCreation_date((String)out.get("creation_date").toString());




            //                   System.out.println(""+params.getCreation_date());
                              
            //                   resulted.add(params);


            //               }
            //               output.setResult(resulted);

            //               output.setStatus("success");
            //           }
            //           exchange.getIn().setBody(output);

            //         }
            //     })
            //     .log("${body}").marshal().json(JsonLibrary.Jackson, io.fabric8.quickstarts.camel.chapters.Result.class)

            //     .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")

            //   .endRest();

            //   rest("/createchapter").get().bindingMode(RestBindingMode.off).produces("application/json").param()
            //     .name("data").type(RestParamType.query).dataType("String").endParam().route().routeId("Createchapter")
            //     .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //     .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")
            //     .process(new Processor() {
            //         public void process(Exchange exchange) throws Exception {
            //             String results = exchange.getIn().getBody(String.class);
            //             JSONArray jsonObject = new JSONArray(results);
            //             // Map out=(Map) results.get(0);
            //             System.out.println(jsonObject.getJSONObject(0).getInt("foreign_instructor_id"));
            //             exchange.getIn().setHeader("foreign_instructor_id",
            //                     jsonObject.getJSONObject(0).getInt("foreign_instructor_id"));
            //                     exchange.getIn().setHeader("foreign_material_id",
            //                     jsonObject.getJSONObject(0).getInt("foreign_material_id"));
            //                     exchange.getIn().setHeader("foreign_class_id",
            //                     jsonObject.getJSONObject(0).getInt("foreign_class_id")); 
            //                      exchange.getIn().setHeader("chapter_number",
            //                     jsonObject.getJSONObject(0).getString("chapter_number")); 
            //                      exchange.getIn().setHeader("chapter_subject",
            //                     jsonObject.getJSONObject(0).getString("chapter_subject")); 
                             

            //             // exchange.getIn().setHeader("password", out.get("password"));

            //         }
            //     })
            //     .to("sql:INSERT INTO chapters (foreign_instructor_id , foreign_material_id, foreign_class_id, chapter_number, chapter_subject) VALUES  "
            //                 + "(:#${header.foreign_instructor_id} , :#${header.foreign_material_id},:#${header.foreign_class_id},:#${header.chapter_number},:#${header.chapter_subject} )?"
            //                 + "dataSource=dataSource")

            //     .process(new Processor() {
            //         public void process(Exchange exchange) throws Exception {

            //             String state = "{" + "\"status\":\"success\"}";

            //           exchange.getIn().setBody(state);
            //         }
            //     })
            //     .log("${body}").unmarshal().json(JsonLibrary.Jackson, Status.class)
            //     .marshal().json(JsonLibrary.Jackson, Status.class)

            //  //   .log("${body}").end().to("log:DEBUG?showBody=true&showHeaders=true")

            //   .endRest();



            // rest("download/{filename}").get().produces(MediaType.APPLICATION_OCTET_STREAM_VALUE).route()
            //         .routeId("downloadFile")
            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                  File file = new File("/Users/omar/Downloads/elearn/"+exchange.getIn().getHeader("filename"));
            //                 byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);

            //                  exchange.getIn().setBody(data);
            //             }
            //         })
            //       //  .to("file:/Users/omar/Downloads/elearn?fileName=${header.filename}&noop=true")
            //         .setHeader("Content-Disposition", simple("attachment;filename=${header.filename}")).endRest();
            // rest("getcontentvoice/").get().produces("application/json").param().name("data").type(RestParamType.query)
            //         .dataType("String").endParam().route().routeId("getvoicecontent")
            //         .to("log:DEBUG?showBody=true&showHeaders=true").log("${header.data}")
            //         .setBody(simple("${header.data}")).convertBodyTo(String.class).log("${body}")

            //         // .marshal().json(JsonLibrary.Jackson,Params.class)

            //         // .log("${body}")

            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 String results = exchange.getIn().getBody(String.class);
            //                 JSONArray jsonObject = new JSONArray(results);
            //                 // Map out=(Map) results.get(0);
            //                 // System.out.println(jsonObject.getJSONObject(0).getString("username"));
            //                 String [] filename_split = jsonObject.getJSONObject(0).getString("voice_id").split("\\$");
            //                 // for (int i = 0; i < filename_split.length; i++)
            //                 //     System.out.println("mambo\n " + i + " try " + filename_split[i]);
            //                  exchange.getIn().setHeader("filename",filename_split[1]
            //                         );
            //            //     exchange.getIn().setHeader("image_id", jsonObject.getJSONObject(0).getString("image_id"));

            //                 // exchange.getIn().setHeader("password", out.get("password"));

            //             }
            //         }).log("${header.voice_id}")//.log("${header.session_title}")
            //       //  .to("sql:select session_voice from session_content_voice where session_title =:#${header.session_title} and foreign_session_image=:#${header.image_id}?"
            //       //          + "dataSource=dataSource")
            //     .log("${body}")//.pollEnrich()
            //     //    .simple("file:/Users/omar/Downloads/elearn?fileName=${header.filename}&noop=true").timeout(1000000)
            //         // .marshal().base64()
            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                  File file = new File("/Users/omar/Downloads/elearn/"+exchange.getIn().getHeader("filename"));
            //                 byte[] data = org.apache.commons.io.FileUtils.readFileToByteArray(file);

            //                  exchange.getIn().setBody(data);
            //             }
            //         })
            //         .process(new Processor() {
            //             public void process(Exchange exchange) throws Exception {
            //                 InputStream Inputstream = exchange.getIn().getBody(InputStream.class);
            //                 byte[] bytes = IOUtils.toByteArray(Inputstream);
            //                 String results = Base64.encodeBase64String(bytes);
            //                 System.out.println(results);

            //                 String response = "{" + "\"result\":\"" + results + "\"," + "\"status\":\"" + "success"
            //                         + "\"" + "}";

            //                 exchange.getIn().setBody(response);

            //             }
            //         }).log("${body}").unmarshal().json(JsonLibrary.Jackson, DownloadVoice.class).endRest();
            // rest("/multi/")
            //         // .produces("text/plain")
            //         .post().bindingMode(RestBindingMode.off).consumes("multipart/form-data")

            //         .route().unmarshal().mimeMultipart().process((exchange) -> {
            //             // the body is now the first form field
            //             String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
            //             System.out.println(String.format("Body Content-Type: %s", contentType));
            //             // System.out.println(String.format("Body:\n%s",
            //             // exchange.getIn().getBody(String.class)));

            //             // the first attachment is the second form field
            //             Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
            //             System.out.println(String.format("Attachments: %s", attachments.size()));
            //             exchange.getIn().getAttachments().forEach((id, dh) -> {
            //                 try {
            //                     Object content = dh.getContent();
            //                     System.out.println(String.format("%s %s %s %s\n%s", id, dh.getInputStream(),
            //                             dh.getContentType(), dh.getName(), content.toString()));
            //                 } catch (IOException e) {
            //                     e.printStackTrace();
            //                 }

            //             });
            //         }).transform(constant("OK\n")).endRest();

            // from("direct:start").routeId("test").process(new Processor() {
            //     public void process(Exchange exchange) throws Exception {

            //         Upload test = new Upload();
            //         test.setMessage("Upload and move success");
            //         test.setSuccess(true);

            //         exchange.getIn().setBody(test);

            //     }
            // }).marshal().json(JsonLibrary.Jackson, Upload.class).log("${body}");

            // // .get("sumrevenue").description("The sum revenu for specified currency")
            // .route().routeId("get-revenue-month")
            // .to("direct:Auth")
            // .to("sql:SELECT SERVICE_TYPE, CALL_DATE, REVENUE_MONTH, CALL_CLASS,
            // sum(CHARGED_AMOUNT) as CHARGED_AMOUNT FROM factin_operator1d WHERE CALL_CLASS
            // IN('INTERNATIONAL', 'LOCAL', 'ROAMING' ) group by CALL_CLASS,SERVICE_TYPE?" +
            // "dataSource=dataSource&" +
            // "outputClass=io.fabric8.quickstarts.camel.RevenueMonth")
            // .endRest()
            // //
            // .post("exchangerate/insert/{currency}/{exchange_rate}/{from_date}/{to_date}/{description}").description("insert
            // The exchange for specified currency")
            // .get("testprop/")
            // .description("insert The exchange for specified currency")
            // .route().routeId("insert-exchange-rate-api")
            // .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))
            // .to("direct:try")
            // // .to("sql:insert into Currency
            // (id,Currency,Exchange_rate,from_date,to_date,description) values " +
            // // "(:#${header.id} , :#${header.currency},:#${header.exchange_rate},
            // :#${header.from_date},:#${header.to_date}, :#${header.description})?" +
            // // "dataSource=dataSource")
            // // .log("all
            // headers"+"${header.CamelSqlGeneratedKeyRows}"+"again"+"${header.CamelSqlGeneratedKeysRowCount}")

            // // .log("Inserted new order ${header.CamelSqlGeneratedKeyRows}")
            // // .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
            // .endRest()
            // // .post("payment/").description("insert The payment for specified invoice")
            // .route().routeId("insert-payment-rate-api")
            // .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))

            // .log("${body}")
            // .marshal().json(JsonLibrary.Jackson,Payment.class)
            // .log("${body}")
            // .unmarshal().json(JsonLibrary.Jackson,Payment.class)
            // .log("${body}")
            // .log("${body.getPayment_amount}")

            // .to("sql:insert into payment
            // (operator_id,invoice_record,ref_invoice,currency,payment_amount,status,payment_type_id,payment_date,log_date,username)
            // values " +
            // "(:#${body.getOperator_id} ,
            // :#${body.getInvoice_record},:#${body.getRef_invoice},:#${body.getCurrency},:#${body.getPayment_amount},:#${body.getStatus},
            // :#${body.getPayment_type_id},:#${body.getPayment_date},:#${body.getLog_date},:#${body.getUsername})?"
            // +
            // "dataSource=dataSource")

            // .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
            // .endRest()
            // .get("QueryPayment/{operator_id}/{ref_invoice}").description("Query The
            // payment for specified operator & invoice")
            // .route().routeId("Query-payment-rate-api-opperinvoice")
            // .to("sql:select * from Payment where operator_id =:#${header.operator_id} and
            // ref_invoice=:#${header.ref_invoice}?" +
            // "dataSource=dataSource&" +
            // "outputClass=io.fabric8.quickstarts.camel.Payment")
            // .endRest()
            // .get("QueryPayments/{op_code}/").description("Query The payment for specified
            // operator")
            // .route().routeId("Query-payment-rate-api-opcode")
            // .to("sql:select * from Payment where op_code =:#${header.op_code}?" +
            // "dataSource=dataSource&" +
            // "outputClass=io.fabric8.quickstarts.camel.Payment")
            // .endRest()
            // .post("sendemail").description("send file by email")
            // .route().routeId("send-file-by-email")
            // .setHeader("json",simple("${body}"))
            // // .to("direct:Auth")
            // // .choice()
            // // .when(simple("${body.getResult} != 'You are UnAuthrized to access such
            // API'"))
            // // .log("${body.getResult}")
            // .setBody(simple("${header.json}"))
            // .process(new Processor() {
            // public void process(Exchange exchange) throws Exception {

            // DefaultAttachment att = new DefaultAttachment(new
            // FileDataSource("/data/backup/omar.pdf"));

            // att.addHeader("Content-Description", "splunkattached");
            // exchange.getIn().addAttachmentObject("omar.pdf", att);

            // }
            // })
            // .setBody(constant(""))
            // .setHeader("subject", constant("Invoice to operator"))
            // .setHeader("To",
            // constant("omar.atia@immovate.net;ahmad.muslimany@immovate.net"))
            // // .setBody(simple("Diode status : ${header.Status}"))
            // // .setHeader("Body",simple("Diode status : ${header.Status}"))
            // .log("Before sending email : ${body}")
            // .to("smtps://smtp.gmail.com:465?password=Oa03216287@&username=atiaomar1978@gmail.com&From=acs@immovate.com")
            // .log("Inserted values ${body}")
            // .log("Inserted values ${body}")
            // .endRest()
            // .post("bankpayment").description("Notification from Bank")
            // .route().routeId("Bank")
            // .to("direct:Bank_Notification")
            // // .choice()
            // // .when(simple("${body.getResult} != 'You are UnAuthrized to access such
            // API'"))
            // // .log("${body.getResult}")

            // .endRest();

            // from("smpp://smppclient@localhost:8000?password=password&enquireLinkTimer=3000&transactionTimer=5000&systemType=consumer")
            // .to("log:DEBUG?showBody=true&showHeaders=true");

            // .post("op-configuration").description("Query The payment for specified
            // operator")
            // .route().routeId("operator-configuration-insertion--api-opcode")
            // .setHeader("json",simple("${body}"))
            // .to("direct:Auth")
            // // .choice()
            // // .when(simple("${body.getResult} != 'You are UnAuthrized to access such
            // API'"))
            // // .log("${body.getResult}")
            // .setBody(simple("${header.json}"))
            // .to("direct:start")
            // .endRest()
            // .get("QueryOpConfig/{op_code}/").description("Query The operator config for
            // specified operator")
            // .route().routeId("operator-config-rate-api-opcode")
            // .to("sql:select * from op_config where op_code =:#${header.op_code}?" +
            // "dataSource=dataSource&" +
            // "outputClass=io.fabric8.quickstarts.camel.Opconfig")
            // .endRest();

            // from("direct:start").routeId("rollback2")
            // .transacted("PROPAGATION_REQUIRED")
            // .setHeader("CamelSqlRetrieveGeneratedKeys",simple("true", Boolean.class))
            // .doTry()
            // .marshal().json(JsonLibrary.Jackson,Opconfig.class)
            // .unmarshal().json(JsonLibrary.Jackson,Opconfig.class)
            // .setHeader("ops",simple("${body}"))
            // .to("sql:INSERT INTO op_config ( operator_code, operator_name, phone, email,
            // cmc_coe_ratio_local, "
            // + "cmc_coe_ratio_roaming, operator_tax, payment_type_id, username, note) "
            // +"values(:#${body.getOperator_code},:#${body.getOperator_name},:#${body.getPhone},:#${body.getEmail},:#${body.getCmc_coe_ratio_local},:#${body.getCmc_coe_ratio_roaming},:#${body.getOperator_tax}"+
            // ",:#${body.getPayment_type_id},:#${body.getUsername},:#${body.getNote})?" +
            // "dataSource=dataSource1")
            // .setBody(simple("${header.ops}"))
            // .setHeader("op_id",simple("${header.CamelSqlGeneratedKeyRows}"))
            // .process(new Processor() {
            // public void process(Exchange exchange) throws Exception {
            // ArrayList results = exchange.getIn().getHeader("op_id",ArrayList.class);
            // Map test = (Map)results.get(0);
            // exchange.getIn().setHeader("op_id_decode", test.get("GENERATED_KEY"));

            // }
            // })
            // .log("ya salam"+"${header.op_id_decode}")
            // .to("sql:INSERT INTO bank_details ( bank_code, bank_name, branch, phone,
            // email, username)"
            // +"values(:#${body.getBank_code},:#${body.getBank_name},:#${body.getBranch},:#${body.getPhone},:#${body.getEmail},:#${body.getUsername}"+
            // ")?" +
            // "dataSource=dataSource1")
            // .setHeader("bank_id",simple("${header.CamelSqlGeneratedKeyRows}"))
            // .setBody(simple("${header.ops}"))
            // .process(new Processor() {
            // public void process(Exchange exchange) throws Exception {
            // ArrayList results = exchange.getIn().getHeader("bank_id",ArrayList.class);
            // Map test = (Map)results.get(0);
            // exchange.getIn().setHeader("bank_id_decode", test.get("GENERATED_KEY"));

            // }
            // })

            // .to("sql:insert into operator_banking_accounts (operator_id, bank_id,
            // account_number,currency, username)"
            // +"values(:#${header.op_id_decode},:#${header.bank_id_decode},:#${body.getAccount_number},:#${body.getCurrency},:#${body.getUsername}"+
            // ")?" +
            // "dataSource=dataSource1")

            // .setBody(simple("${header.CamelSqlGeneratedKeyRows}"))
            // .doCatch(Exception.class)
            // .process(new Processor() {
            // @Override
            // public void process(Exchange exchange) throws Exception {
            // final Throwable ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
            // Throwable.class);
            // // exchange.getIn().setBody("{Exception occured :"+ex.getMessage()+"}");
            // throw new Exception("{Exception occured :"+ex.getMessage()+"}");

            // }
            // })
            // .end();

            // from("direct:Auth").routeId("Auth")
            // .to("log:DEBUG?showBody=true&showHeaders=true")
            // .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            // .setHeader("Content-Type", constant("application/json"))
            // .setHeader("Accept", constant("application/json"))
            // .setHeader("CamelHttpUrl",constant("http://localhost:5000/api/user"))
            // .setHeader("CamelServletContextPath",constant(""))
            // .setHeader("host",constant("localhost"))
            // .setHeader("proto",constant("http"))
            // .setHeader("Host",constant("localhost"))
            // .setBody(constant(""))

            // .to("http://localhost:5000/api/user"+"?bridgeEndpoint=true");

            // from("direct:try").setHeader("SSID",(simple("${properties:SSID}")))
            // .to("velocity:io/fabric8/quickstarts/camel/properties/velojson.vm?contentCache=true")

            // .to("log:DEBUG?showBody=true&showHeaders=true");

            // .log("atiato"+"${body}")
            // .log("log:DEBUG?showBody=true&showHeaders=true");

        }
    }

    // @Bean(name = "OracledataSource")
    // @ConfigurationProperties(prefix="test.datasource")
    // public DataSource dataSource() {
    // return DataSourceBuilder.create().build();
    // }

    // @Bean(name = "mySQLdataSource")
    // @Primary
    // @ConfigurationProperties(prefix="spring.datasource")
    // public DataSource testdataSource() {
    // return DataSourceBuilder.create().build();
    // }

    private void printRequest(HttpServletRequest httpRequest) {
        System.out.println(" \n\n Headers");

        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            System.out.println(headerName + " = " + httpRequest.getHeader(headerName));
        }

        System.out.println("\n\nParameters");

        Enumeration params = httpRequest.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String) params.nextElement();
            System.out.println(paramName + " = " + httpRequest.getParameter(paramName));
        }

        System.out.println("\n\n Row data");
        System.out.println(extractPostRequestBody(httpRequest));
    }

    static String extractPostRequestBody(HttpServletRequest request) {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            Scanner s = null;
            try {
                s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return s.hasNext() ? s.next() : "";
        }
        return "";
    }

    // @Bean(name = "dataSource")
    // @ConfigurationProperties(prefix = "spring.datasource")
    // public DataSource dataSource2() {

    //     return DataSourceBuilder.create().build();
    // }

    public InputStream clone_omar(final InputStream inputStream) {
        try {
            inputStream.mark(0);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int readLength = 0;
            while ((readLength = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readLength);
            }
            inputStream.reset();
            outputStream.flush();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // public void process(Exchange exchange) throws Exception { // the API is a bit
    // clunky so we need to loop
    // Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
    // if (attachments.size() > 0) {
    // for (String name : attachments.keySet())
    // { DataHandler dh = attachments.get(name);
    // // get the file name
    // String filename = dh.getName(); // get the content and convert it to byte[]
    // byte[] data = exchange.getContext().getTypeConverter()
    // .convertTo(byte[].class, dh.getInputStream()); // write the data to a file
    // FileOutputStream out = new FileOutputStream(filename);
    // out.write(data); out.flush(); out.close();
    // }
    // }
    // }

    // @Component
    // class Backend extends RouteBuilder {

    // @Override
    // public void configure() {
    // JacksonDataFormat df = new JacksonDataFormat(Root.class);
    // df.disableFeature(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // df.disableFeature(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

    // df.setUseList(true);

    // from("sql:select * from bill_cycle where is_predicted = 'no'?" +
    // "consumer.onConsume=update bill_cycle set is_predicted = 'yes' where id =
    // :#tid&" +
    // "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
    // "dataSource=dataSource&"+"outputClass=io.fabric8.quickstarts.camel.BillCycle")
    // .routeId("processing-prediction-for-billcycle").log("${body.id}")
    // .setHeader("tid",simple("${body.id}"))
    // .setHeader("QueryResult",simple("${body}"))
    // .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
    // .setHeader("Accept", constant("application/x-www-form-urlencoded"))
    // .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    //
    //
    // .to("log:DEBUG?showBody=true&showHeaders=true")
    // .setBody(constant("username=admin&password=Oa03216287@"))
    // .log("${body}")
    // .to("http://localhost:8089/services/auth/login")
    // .split().xpath("//response/sessionKey/text()")
    // .log("Inserted new order ${body}")
    // .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
    // .setHeader("Accept", constant("application/x-www-form-urlencoded"))
    // .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    // .setHeader("Authorization",simple("Splunk ${body}"))
    // .log("${header.Authorization}")
    // .setBody(constant("search=search%20index%3Dinvoices%20%7C%20eval%20_time%3Dstrptime(from_date%2C%20%22%25Y-%25m-%25d%22)%20%7C%20timechart%20span%3D1mon%20values(invoice_amount)%20as%20invoice_amount%20%7C%20predict%20%22invoice_amount%22%20as%20prediction%20algorithm%3DLLP5%20holdback%3D0%20future_timespan%3D5%20upper0%3Dupper0%20lower0%3Dlower0%20%7C%20%60forecastviz(5%2C%200%2C%20%22invoice_amount%22%2C%200)%60"))
    // .log("${body}")
    // .to("http://localhost:8089/servicesNS/admin/Splunk_ML_Toolkit/search/jobs")
    // .split().xpath("//response/sid/text()")
    // .log("Inserted new sid ${body}")
    // .setHeader("sid",simple("${body}"))
    // .setHeader("Content-Type", constant("application/x-www-form-urlencoded"))
    // .setHeader("Accept", constant("application/x-www-form-urlencoded"))
    // .setHeader(Exchange.HTTP_METHOD, constant("GET"))
    // .delay(10000)
    // .toD("http://localhost:8089/servicesNS/admin/Splunk_ML_Toolkit/search/jobs/${header.sid}/results/?output_mode=json")
    // .unmarshal(df)
    //
    // .process(new Processor() {
    // public void process(Exchange exchange) throws Exception {
    // Root output = (Root)exchange.getIn().getBody(Root.class);
    // BillCycle predictdate = (BillCycle)exchange.getIn().getHeader("QueryResult");
    //
    // for (int i=0;i<output.getResults().size();i++) {
    //
    // if(output.getResults().get(i).get_time().toString().equals(predictdate.getPredict_start_date().toString()))
    // {
    // log.info("result"+output.getResults().get(i).get_time().toString()+"prediction"+output.getResults().get(i).getPrediction()+"span"+output.getResults().get(i).get_spandays());
    // exchange.getIn().setHeader("_time",predictdate.getPredict_start_date());
    // exchange.getIn().setHeader("prediction",Double.parseDouble(output.getResults().get(i).getPrediction()));
    // if(output.getResults().get(i).get_spandays()!=null)
    // {
    // exchange.getIn().setHeader("future_span",
    // Integer.parseInt(output.getResults().get(i).get_spandays()));
    //
    //
    // }else exchange.getIn().setHeader("future_span", Integer.parseInt("0"));
    //
    //
    // break;
    // }
    //
    //
    //
    // }
    //
    // exchange.getIn().setHeader("op_code",predictdate.getOperator_id());
    //
    //
    // }
    // })
    // .to("sql:insert into predict_revenue
    // (id,operator_id,from_date,to_date,future_span,predicted_amount,username)
    // values " +
    // "(1,:#${header.op_code} , :#${header._time},sysdate, :#${header.future_span},
    // :#${header.prediction},'atiato')?" +
    // "dataSource=dataSource")
    //
    // .log("inserted time ${header._time}")
    //
    // ;
    //

    // from("sql:select rowid ,CALL_DATE ,ADJUSTMENT_IND ,REVENUE_MONTH ,SUB_TYPE
    // ,CALL_TYPE ,SERVICE_TYPE ,CALL_CLASS ,CNT ,DURATION ,DATA_VOLUME
    // ,CHARGED_AMOUNT , PARTNUM ,DTM_DATE ,DTM_DAY_NO , IS_APPROVED from
    // FACT$IN_OPERATOR1$D where revenue_month='201908' and is_approved = 'no'?" +
    // "consumer.onConsume=update FACT$IN_OPERATOR1$D set is_approved = 'yes' where
    // ROWID = :#oraclerowid&" +
    // "consumer.delay={{quickstart.processOrderPeriod:5s}}&" +
    // "dataSource=OracledataSource")//&"+"outputClass=io.fabric8.quickstarts.camel.RevenueMonth")
    // .routeId("generate-csv-for-indexing-in-splunk")
    // .process(new Processor() {
    // public void process(Exchange exchange) throws Exception {
    // Map results = exchange.getIn().getBody(Map.class);
    // exchange.getIn().setHeader("oraclerowid", results.get("ROWID"));
    // exchange.getIn().setHeader("CALL_DATE", results.get("CALL_DATE"));
    // exchange.getIn().setHeader("ADJUSTMENT_IND", results.get("ADJUSTMENT_IND"));
    // exchange.getIn().setHeader("REVENUE_MONTH", results.get("REVENUE_MONTH"));
    // exchange.getIn().setHeader("SUB_TYPE", results.get("SUB_TYPE"));
    // exchange.getIn().setHeader("CALL_TYPE", results.get("CALL_TYPE"));
    // exchange.getIn().setHeader("SERVICE_TYPE", results.get("SERVICE_TYPE"));
    // exchange.getIn().setHeader("CALL_CLASS", results.get("CALL_CLASS"));
    // exchange.getIn().setHeader("CNT", results.get("CNT"));
    // exchange.getIn().setHeader("DURATION", results.get("DURATION"));
    // exchange.getIn().setHeader("DATA_VOLUME", results.get("DATA_VOLUME"));
    // exchange.getIn().setHeader("CHARGED_AMOUNT", results.get("CHARGED_AMOUNT"));
    // exchange.getIn().setHeader("PARTNUM", results.get("PARTNUM"));
    // exchange.getIn().setHeader("DTM_DAY_NO", results.get("DTM_DAY_NO"));
    // //DTM_DATE
    // exchange.getIn().setHeader("DTM_DATE", results.get("DTM_DATE"));
    // }
    // })
    // .to("sql:insert into factin_operator1d
    // (CALL_DATE,ADJUSTMENT_IND,REVENUE_MONTH,SUB_TYPE,CALL_TYPE,SERVICE_TYPE,CALL_CLASS,CNT,DURATION
    // ,DATA_VOLUME ,CHARGED_AMOUNT , PARTNUM ,DTM_DATE ,DTM_DAY_NO) values " +
    // "(:#${header.CALL_DATE} ,
    // :#${header.ADJUSTMENT_IND},:#${header.REVENUE_MONTH}, :#${header.SUB_TYPE},
    // :#${header.CALL_TYPE},:#${header.SERVICE_TYPE},:#${header.CALL_CLASS},:#${header.CNT},:#${header.DURATION},:#${header.DATA_VOLUME},:#${header.CHARGED_AMOUNT},:#${header.PARTNUM},:#${header.DTM_DATE},:#${header.DTM_DAY_NO})?"
    // +
    // "dataSource=mySQLdataSource")

    // .to("log:DEBUG?showBody=true&showHeaders=true");

    // .setHeader("rowid",simple("${header.oraclerowid}"))
    // .log("Processed order #id ${body.id}")
    // .marshal().csv()
    // .to("file:target/reports/?fileName=oracle.txt&fileExist=Append");

    // from("timer:new-order?delay=1s&period={{quickstart.generateOrderPeriod:2s}}")
    // .routeId("atiato-proocedure")
    // .to("sql-stored:tryitfromcamel(OUT DOUBLE span_out)")
    // .to("log:DEBUG?showBody=true&showHeaders=true")
    // .process(new Processor() {
    // public void process(Exchange exchange) throws Exception {
    // Map results = exchange.getIn().getBody(Map.class);
    // exchange.getIn().setHeader("span_out", results.get("span_out"));
    //
    // }
    // })
    //
    //
    // .log("Inserted out of proecedure order ${header.span_out}");

    // from("file:target/reports/?delete=true")
    // from("imaps://imap.gmail.com?username=atiaomarj@gmail.com&password=Oa03001294@"
    // + "&delete=false&unseen=true&consumer.delay=60000")
    // .process(new Processor() { // the API is a bit clunky so we need to loop
    // public void process(Exchange exchange) throws Exception {

    // Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
    // if (attachments.size() > 0) {
    // for (String name : attachments.keySet())
    // { DataHandler dh = attachments.get(name);
    // // get the file name
    // // get the content and convert it to byte[]
    // byte[] data =
    // exchange.getContext().getTypeConverter().convertTo(byte[].class,
    // dh.getInputStream()); // write the data to a file
    // InputStream out = new ByteArrayInputStream(data);
    // exchange.getIn().setBody(out);
    // }
    // }
    // }
    // })
    // //.convertBodyTo(ArrayList.class)

    // .to("log:newmail")

    // .unmarshal().csv()
    // .process(new Processor() {
    // public void process(Exchange exchange) throws Exception {
    // List<List<String>> results = (List<List<String>>) exchange.getIn().getBody();
    // for (List<String> line : results) {
    // exchange.getIn().setHeader("operator_id", line.get(0));
    // exchange.getIn().setHeader("invoice_record", line.get(1));
    // exchange.getIn().setHeader("invoice_number_ref", line.get(2));
    // exchange.getIn().setHeader("currency", line.get(3));
    // exchange.getIn().setHeader("payment_amount", line.get(4));
    // exchange.getIn().setHeader("status", line.get(5));
    // exchange.getIn().setHeader("payment_type_id", line.get(6));
    // exchange.getIn().setHeader("payment_date", line.get(7));
    // exchange.getIn().setHeader("log_date", line.get(8));
    // exchange.getIn().setHeader("username", line.get(9));
    // }

    // }
    // })
    // .to("sql:insert into payment
    // (operator_id,invoice_record,invoice_number_ref,currency,payment_amount,status,payment_type_id,payment_date,log_date,username)
    // values " +
    // "(:#${header.operator_id} ,
    // :#${header.invoice_record},:#${header.invoice_number_ref},:#${header.currency},:#${header.payment_amount},:#${header.status},
    // :#${header.payment_type_id},:#${header.payment_date},:#${header.log_date},:#${header.username})?"
    // +
    // "dataSource=dataSource")
    // .to("sql:select sum(a.payment_amount) TOTAL_PAY, a.invoice_number_ref
    // PAY_REF,a.payment_amount PAY_AMOUNT,b.ref_number PAY_REF_INV,
    // b.final_invoice_amount PAY_AMOUNT_INV from invoices b , payment a where
    // a.invoice_number_ref = b.ref_number and a.invoice_number_ref =
    // :#${header.invoice_number_ref} group by
    // a.invoice_number_ref?dataSource=dataSource")
    // .process(new Processor() {
    // public void process(Exchange exchange) throws Exception {
    // ArrayList results = exchange.getIn().getBody(ArrayList.class);
    // Map out=(Map) results.get(0);
    // exchange.getIn().setHeader("TOTAL_PAY", out.get("TOTAL_PAY"));

    // exchange.getIn().setHeader("PAY_REF", out.get("PAY_REF"));
    // exchange.getIn().setHeader("PAY_AMOUNT", out.get("PAY_AMOUNT"));
    // exchange.getIn().setHeader("PAY_REF_INV", out.get("PAY_REF_INV"));
    // exchange.getIn().setHeader("PAY_AMOUNT_INV", out.get("PAY_AMOUNT_INV"));

    // }
    // })
    // .choice()
    // .when(simple("${header.TOTAL_PAY} == ${header.PAY_AMOUNT_INV}"))
    // .log("amount is fully paid")
    // .to("sql:update invoices set status='paid' where ref_number =
    // :#${header.invoice_number_ref}?dataSource=dataSource")
    // .when(simple("${header.TOTAL_PAY} < ${header.PAY_AMOUNT_INV}"))
    // .to("sql:update invoices set status='partiallypaid' where ref_number =
    // :#${header.invoice_number_ref}?dataSource=dataSource")
    // .log("amount is partially paid")
    // .end()
    // .to("log:DEBUG?showBody=true&showHeaders=true");

    // from("direct:Bank_Notification")
    // .to("smpp://smppclient@localhost:2775?password=password&enquireLinkTimer=3000&transactionTimer=5000&systemType=producer");

    // }
    // }
}
