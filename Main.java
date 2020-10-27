package com.company;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.*;

public class Main {
    public static Link link = new Link("","","","","","","","","");
    public static String start = "<!DOCTYPE html>\n<html", head = "\t<head>\n", body = "\t<body", globalStringKey = "start", oddball ="";
    public static Gson gson = new Gson();
    public static Boolean insideDiv = false, hasAttribute = false;

    public static boolean isNumeric(final String str) { //checks if provided string contains numerics
        // null or empty
        if (str == null || str.length() == 0) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    //handling keys, this function is over-complicated and is in line for a rewrite to a more general solution
    public static void handleJSONObject(JsonObject jsonObject) { //it is made with the provided sample object in mind and most all of it is hardcoded
        for (Map.Entry<?, ?> entry : jsonObject.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();

            if (key.toString().charAt(0) == 'h' && isNumeric(key.toString().split("h")[1]) || key.toString().equals("p")) {
                if(insideDiv) {
                    oddball += "\t";
                }
                if(!hasAttribute) {
                    oddball += ">\n";
                    //body += ">\n";
                    hasAttribute = true;
                }
                oddball += "\t\t<" + key + ">" + value.toString().substring(1, value.toString().length() -1) + "</" + key + ">\n";
            } else {
                switch (key.toString()) {
                    case "body":
                        globalStringKey = "body";
                        handleValue(value);
                        break;
                    case "head":
                        globalStringKey = "head";
                        handleValue(value);
                        break;
                    case "attributes":
                            hasAttribute = true;
                            oddball += " ";
                            //System.out.print();
                            handleValue(value);
                            oddball += ">\n";
                        break;
                    case "meta":
                        for(Map.Entry<?, ?> secondEntry : ((JsonObject) value).entrySet()){
                            if(secondEntry.getKey().toString().equals("viewport")) {
                                String viewportValue = secondEntry.getValue().toString().replace("{", "").replace("}", "").replace(":", "=");
                                char[] cpyStyle = viewportValue.toCharArray();
                                viewportValue = "";
                                for(int x = 0; x<cpyStyle.length; x++){
                                    if (cpyStyle[x] != '\"' || x == 0){
                                        viewportValue += cpyStyle[x];//viewportValue += cpyStyle[x];
                                    }
                                    else if(cpyStyle[x] == ',')
                                        viewportValue += cpyStyle[x] + " ";
                                }
                                head += "\t\t<meta name=\"" + secondEntry.getKey() + "\" content=" + viewportValue + "\">\n";
                            }
                            else if(!secondEntry.getKey().toString().equals("charset"))
                                head += "\t\t<meta name=\"" + secondEntry.getKey() + "\" content=" + secondEntry.getValue() + ">\n";

                            else
                                head += "<meta " + secondEntry.getKey() + "=" + secondEntry.getValue() +  ">\n\t\t";
                        }
                        break;
                    case "link":
                        for(JsonElement objFromArray : (JsonArray) value) {
                            try{
                                for (Map.Entry<?, ?> secondEntry : ((JsonObject)objFromArray).entrySet())  {
                                    switch(secondEntry.getKey().toString()){
                                        case "crossorigin":
                                            if(!secondEntry.getValue().toString().equals("")){
                                                link.setCrossorigin(secondEntry.getValue().toString());
                                            }
                                            break;
                                        case "href":
                                            link.setHref(secondEntry.getValue().toString());
                                            break;

                                        case "hreflang":
                                            link.setHreflang(secondEntry.getValue().toString());
                                            break;
                                        case "media":
                                            link.setMedia(secondEntry.getValue().toString());
                                            break;
                                        case "rel":
                                            link.setRel(secondEntry.getValue().toString());
                                            break;
                                        case "sizes":
                                            link.setSizes(secondEntry.getValue().toString());
                                            break;
                                        case "title":
                                            link.setTitle(secondEntry.getValue().toString());
                                            break;
                                        case "type":
                                            link.setType(secondEntry.getValue().toString());
                                            break;
                                        default:
                                            System.out.println(secondEntry.getKey().toString());
                                            System.out.println("This isn't permited buddy!");
                                    }
                                }
                            }
                            catch(Exception e){
                                handleJSONArray((JsonArray) objFromArray);
                            }
                            head += link.getLinkElement();
                            link.everythingClear();
                        }
                        break;
                    case "id":
                        oddball += key + "=" + handleValue(value);
                        break;
                    case "div":
                        insideDiv = true;
                        body += oddball;
                        oddball = "";
                        body += "\n\t\t<div";
                        if(value.toString().contains("attr")){
                                handleValue(value);
                        }

                        else{
                            String modifiedValue = value.toString().replace("\"", "").split(":")[0];
                            modifiedValue = modifiedValue.substring(1);

                            body += ">\n\t\t\t<" + modifiedValue + ">\n\t";
                            insideDiv = true;
                            handleValue(value);
                            oddball += "\t\t\t</" + modifiedValue + ">\n";
                        }
                        oddball += "\n\t\t</div>\n";
                        insideDiv = false;
                        break;
                    case "title":
                        head += "\t\t<" + key + ">" + value.toString().substring(1, value.toString().length()-1) + "</" + key + ">\n";
                        break;
                    case "class":
                        oddball += key + "=" + value;
                        break;
                    case "style":
                        String styleValue = handleValue(value).replace(',',';').replace("{", "").replace("}", "");
                        char[] cpyStyle = styleValue.toCharArray();
                        styleValue = "";
                        for(int x = 0; x<cpyStyle.length; x++){
                            if (cpyStyle[x] != '\"' && x != 0 && x != cpyStyle.length -1){
                                styleValue += cpyStyle[x];
                            }
                        }
                        oddball += " " + key + "=\"" + styleValue + "\"";

                        break;
                    default:
                        if (globalStringKey.equals("start") && !key.equals("doctype")){
                            start += " " + key.toString().substring(0, 4) + "=" + value;
                            globalStringKey = "";
                            break;
                        }
                        else {
                            handleValue(value);
                        }
                        break;
                }
            }
        }
    }

        public static String handleValue (Object value){ //value type sorter
            if (value instanceof JsonObject) {

                handleJSONObject((JsonObject) value);
            }
            else if (value instanceof JsonArray) {

                handleJSONArray((JsonArray) value);
            }

            return value.toString();
        }
        
        public static void handleJSONArray (JsonArray jsonArray){
            jsonArray.iterator().forEachRemaining(element -> {
                handleValue(element);
            });
        }

        public static void main (String[]args){

        String[] fileNameArray = new String[] {}; //add filenames if you need to loop over them
        for(String file : fileNameArray) {
            try (Reader reader = new FileReader("Change to file path" + file)) { //add file path

                JsonObject gobj = gson.fromJson(reader, JsonObject.class);
                reader.close();

                handleJSONObject(gobj); //the function that starts the sorting
                FileWriter myWriter = new FileWriter("Change this to output file path" + file.replace("json", "html")); //add file path
                myWriter.write(start + ">" + head + "\t</head>" + body + oddball + "\n\t</body>\n</html>"); //write to file
                myWriter.close(); //close file

            } catch (Exception e) {
                System.out.println(e);
            }
            //prepaires all values for the next iteration
            insideDiv = false; hasAttribute = false;
            start = "<!DOCTYPE html>\n<html"; head = "\t<head>\n"; body = "\t<body"; globalStringKey = "start"; oddball = "";
        }
        }
    }

    class Link {
        public String crossorigin;
        public String href;
        public String hreflang;
        public String media;
        public String referrerpolicy;
        public String rel;
        public String sizes;
        public String title;
        public String type;

        public Link(){

        }

        public Link(String crossorigin, String href, String hreflang, String media, String referrerpolicy,String sizes, String rel, String title, String type){
            this.crossorigin = crossorigin;
            this.href = href; this.hreflang=hreflang; this.media=media; this.referrerpolicy=referrerpolicy; this.sizes=sizes; this.rel=rel; this.title = title; this.type = type;
        }

        public void everythingClear(){ // clear all values on call
            this.setCrossorigin("");
            this.setHref("");
            this.setHreflang("");
            this.setMedia("");
            this.setReferrerpolicy("");
            this.setRel("");
            this.setSizes("");
            this.setTitle("");
            this.setType("");
        }
        
        public String getLinkElement(){ //builds the link element and returns it
            String linkString = "";
            for(int i = 0; i < 9; i++) {
                if (this.getCrossorigin() != "" && i == 0)
                    linkString += " crossorigin=" + this.getCrossorigin();
                else if (this.getHref() != "" && i == 1)
                    linkString += "href=" + this.getHref();
                else if (this.getHreflang() != "" && i == 2)
                    linkString += " hreflang=" + this.getHreflang();
                else if (this.getMedia() != "" && i == 3)
                    linkString += "media=" + this.getMedia();
                else if (this.getReferrerpolicy() != "" && i == 4)
                    linkString += "referrerpolicy=" + this.getReferrerpolicy();
                else if (this.getRel() != "" && i == 5)
                    linkString += " rel=" + this.getRel();
                else if (this.getSizes() != "" && i == 6)
                    linkString += " sizes=" + this.getSizes();
                else if (this.getTitle() != "" && i == 7)
                    bigString += " title=" + this.getTitle();
                else if (this.getType() != "" && i == 8)
                    linkString += " type=" + this.getType();
                else {
                    //System.out.println("This attribute is empty!"); //uncomment for CBT in console
                }
            }
            return "\t\t<link " + linkString + ">\n";
        }

        //setters && getters
        
        public String getCrossorigin() {
            return this.crossorigin;
        }
        
        public void setCrossorigin(String crossorigin) {
            this.crossorigin = crossorigin;
        }
        
        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getHreflang() {
            return hreflang;
        }

        public void setHreflang(String hreflang) {
            this.hreflang = hreflang;
        }

        public String getMedia() {
            return media;
        }

        public void setMedia(String media) {
            this.media = media;
        }

        public String getReferrerpolicy() {
            return referrerpolicy;
        }

        public void setReferrerpolicy(String referrerpolicy) {
            this.referrerpolicy = referrerpolicy;
        }

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getSizes() {
            return sizes;
        }

        public void setSizes(String sizes) {
            this.sizes = sizes;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }




    }
