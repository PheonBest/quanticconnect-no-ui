/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.quanticconnect.no.ui;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class Main {

    public static String CONFIG_FOLDER = "quantic_connect_config";
    public static String CONFIG_FILE = "config";

    public static void main(String[] args) {

        System.out.println("BEGIN");
        System.out.println("[CONFIG] Getting user data ...");

        String email = "";
        String password = "";
        String url = "";

        try {
            File file = new File("./" + CONFIG_FOLDER + "/" + CONFIG_FILE + ".txt");
            if (file.exists() && !file.isDirectory()) {
                System.out.println("[CONFIG] Configuration file found");
                BufferedReader br = new BufferedReader(new FileReader(file));
                int i = 0;
                String st;
                while ((st = br.readLine()) != null) {
                    switch (i) {
                        case 0:
                            email = st.replace("EMAIL=", "");
                            System.out.println("[CONFIG] Email found: " + email);
                            break;
                        case 1:
                            password = st.replace("PASSWORD=", "");
                            System.out.println("[CONFIG] Password found: " + password);
                            break;
                        case 2:
                            url = st.replace("URL=", "");
                            System.out.println("[CONFIG] Url found: " + url);
                            break;
                        default:
                            break;
                    }
                    ++i;
                }
            } else {
                System.out.println("[CONFIG] No config found. Generating configuration file ...");

                File directory = new File(CONFIG_FOLDER);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                FileWriter fw = new FileWriter("./" + CONFIG_FOLDER + "/" + CONFIG_FILE + ".txt");
                fw.write("EMAIL=\n");
                fw.write("PASSWORD=\n");
                fw.write("URL=https://www.quantic-telecom.net/connexion-reseau\n");
                fw.close();

                System.out.println("[CONFIG] Config file generated");
                System.out.println("END");
                return;
            }

            if (email.isEmpty() || password.isEmpty() || url.isEmpty()) {
                System.out.println("[CONFIG] The user data isn't complete.");
                System.out.println("END");
                return;
            }

            System.out.println("[QUERY] Loading " + url + " ...");
            WebClient webClient = new WebClient();
            modifyWebClient(webClient);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = webClient.getPage(url);
            /*
            String xpath = "(//ul[@class='car-monthlisting']/li)[1]/a";
            HtmlAnchor latestPostLink
                    = (HtmlAnchor) page.getByXPath(xpath).get(0);
            HtmlPage postPage = latestPostLink.click();
             */

            List<HtmlInput> inputs = (List<HtmlInput>) (Object) page.getByXPath("//input");

            for (int i = 0; i < inputs.size(); ++i) {
                if (inputs.get(i).getTypeAttribute().equals("email")) {
                    inputs.get(i).setValueAttribute(email);
                    System.out.println("[QUERY] Email input found.");
                } else if (inputs.get(i).getTypeAttribute().equals("password")) {
                    inputs.get(i).setValueAttribute(password);
                    System.out.println("[QUERY] Password input found.");
                }
            }

            HtmlButton button = (HtmlButton) page.getHtmlElementById("form-continue");
            page = (HtmlPage) button.click();
            webClient.waitForBackgroundJavaScript(7000);

            //The print is exactly the same as the BEFORE CLICK print
            System.out.println("");
            System.out.println("[QUERY] AFTER CLICK");
            
            try {
                HtmlInput name = (HtmlInput)page.getElementById("name");
                System.out.println("[QUERY] " + name.getValueAttribute() + " has been successfully connected !");
            } catch (Exception e) {
                System.out.println("[QUERY] Connection failed:");
                System.out.println(page.asNormalizedText());
            }
            
            webClient.close();
            System.out.println("END");

        } catch (IOException | FailingHttpStatusCodeException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static WebClient modifyWebClient(WebClient client) {
        client.getOptions().setThrowExceptionOnScriptError(false);
        client.setCssErrorHandler(new SilentCssErrorHandler());

        return client;
    }
}
